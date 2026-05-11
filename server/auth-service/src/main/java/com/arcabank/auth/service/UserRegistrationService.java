package com.arcabank.auth.service;

import com.arcabank.auth.dto.RegistrationRequest;
import com.arcabank.auth.exception.AppException;
import com.arcabank.auth.repository.UserRepository;
import com.arcabank.grpc.AccountProvisioningServiceGrpc;
import com.arcabank.grpc.CreateAccountRequest;
import com.arcabank.grpc.CreateAccountResponse;
import com.arcabank.grpc.ProtoCurrency;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final Keycloak keycloak;
    private final UserRepository userRepository;

    @GrpcClient("core-finance-client")
    private AccountProvisioningServiceGrpc.AccountProvisioningServiceBlockingStub accountProvisioningStub;

    @Value("${keycloak.realm}")
    private String realm;

    public void registerUser(RegistrationRequest request) {
        log.info("Initiating user registration process for passport_id: {}", request.passport_id());

        String userId = createKeycloakUser(request);

        setupKeycloakCredentialsAndRoles(userId, request.password());

        syncUserToLocalDatabase(userId, request);

        provisionInitialBankAccount(userId, request);

        log.info("Registration process completely finished for passport_id: {}", request.passport_id());
    }

    private String createKeycloakUser(RegistrationRequest request) {
        UsersResource usersResource = keycloak.realm(realm).users();
        UserRepresentation user = buildUserRepresentation(request);

        try (Response response = usersResource.create(user)) {
            if (response.getStatus() == 201) {
                return extractUserIdFromLocationHeader(response);
            } else if (response.getStatus() == 409) {
                log.warn("Registration failed: User with passport_id {} already exists.", request.passport_id());
                throw new AppException("User already exists!", "USER_ALREADY_EXISTS", HttpStatus.CONFLICT);
            } else {
                log.error("Failed to register user in Keycloak. Status code: {}", response.getStatus());
                throw new AppException("Failed to register user!", "KEYCLOAK_REGISTRATION_FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private void setupKeycloakCredentialsAndRoles(String userId, String password) {
        UsersResource usersResource = keycloak.realm(realm).users();

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        usersResource.get(userId).resetPassword(passwordCred);

        RoleRepresentation userRole = keycloak.realm(realm).roles().get("USER").toRepresentation();
        usersResource.get(userId).roles().realmLevel().add(List.of(userRole));
    }

    private void syncUserToLocalDatabase(String userId, RegistrationRequest request) {
        userRepository.syncUser(
            UUID.fromString(userId),
            request.email(),
            request.firstName(),
            request.lastName(),
            request.passport_id(),
            request.phoneNumber()
        );
        log.info("User synced to local Database. Passport_id: {}", request.passport_id());
    }

    private void provisionInitialBankAccount(String userId, RegistrationRequest request) {
        try {
            log.info("Requesting Core Finance to create an initial account...");
            CreateAccountRequest accountRequest = CreateAccountRequest.newBuilder()
                .setUserId(userId)
                .setCurrency(ProtoCurrency.CURRENCY_UAH)
                .setFirstName(request.firstName())
                .setLastName(request.lastName())
                .setPin(request.pin())
                .build();

            CreateAccountResponse accountResponse = accountProvisioningStub.createInitialAccount(accountRequest);

            if (accountResponse.getSuccess()) {
                log.info("Initial account created successfully! Account ID: {}", accountResponse.getAccountId());
            } else {
                log.warn("Account creation returned failure from core-finance.");
            }
        } catch (Exception e) {
            log.error("Failed to connect to core-finance via gRPC. Initial account creation skipped.", e);
        }
    }

    private UserRepresentation buildUserRepresentation(RegistrationRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.passport_id());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        return user;
    }

    // Extracts the newly generated user ID (the last path segment) from the Keycloak Location URI.
    private String extractUserIdFromLocationHeader(Response response) {
        return response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
    }
}
