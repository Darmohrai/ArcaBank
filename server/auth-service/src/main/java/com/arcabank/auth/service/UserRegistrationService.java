package com.arcabank.auth.service;

import com.arcabank.auth.dto.RegistrationRequest;
import com.arcabank.auth.exception.AppException;
import com.arcabank.auth.repository.UserRepository;
import com.arcabank.grpc.ForgeVaultRequest;
import com.arcabank.grpc.ForgeVaultResponse;
import com.arcabank.grpc.VaultProvisioningServiceGrpc;
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

import java.util.UUID;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final Keycloak keycloak;
    private final UserRepository userRepository;

    @GrpcClient("core-finance")
    private VaultProvisioningServiceGrpc.VaultProvisioningServiceBlockingStub vaultProvisioningStub;

    @Value("${keycloak.realm}")
    private String realm;

    public void registerUser(RegistrationRequest request) {
        log.info("Initiating user registration process for passport_id: {}", request.passport_id());

        UsersResource usersResource = keycloak.realm(realm).users();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.passport_id());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);

        Response response = null;
        try {
            response = usersResource.create(user);

            if (response.getStatus() == 201) {
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

                CredentialRepresentation passwordCred = new CredentialRepresentation();
                passwordCred.setTemporary(false);
                passwordCred.setType(CredentialRepresentation.PASSWORD);
                passwordCred.setValue(request.password());
                usersResource.get(userId).resetPassword(passwordCred);

                RoleRepresentation userRole = keycloak.realm(realm).roles().get("USER").toRepresentation();
                usersResource.get(userId).roles().realmLevel().add(List.of(userRole));

                userRepository.syncUser(
                    UUID.fromString(userId),
                    request.email(),
                    request.firstName(),
                    request.lastName(),
                    request.passport_id(),
                    request.phoneNumber()
                );

                log.info("User successfully registered in Keycloak and synced to local Database. Passport_id: {}", request.passport_id());

                try {
                    ForgeVaultRequest vaultRequest = ForgeVaultRequest.newBuilder()
                        .setUsername(request.passport_id())
                        .setUserId(userId)
                        .setCurrencyCode("UAH")
                        .build();

                    ForgeVaultResponse vaultResponse = vaultProvisioningStub.forgeInitialVault(vaultRequest);

                    if (vaultResponse.getSuccess()) {
                        log.info("User successfully forged! Vault ID: {}", vaultResponse.getVaultId());
                    } else {
                        log.warn("Vault forging returned failure: {}", vaultResponse.getMessage());
                    }
                } catch (Exception e) {
                    log.error("Failed to connect to core-finance via gRPC. Vault creation skipped.", e);
                }
            } else if (response.getStatus() == 409) {
                log.warn("Registration failed: User with passport_id {} already exists (Conflict 409).", request.passport_id());
                throw new AppException(
                    "User already exists!",
                    "USER_ALREADY_EXISTS",
                    HttpStatus.CONFLICT
                );
            } else {
                log.error("Failed to register user in Keycloak. Received unexpected response code: {}", response.getStatus());
                throw new AppException(
                    "Failed to register user!",
                    "KEYCLOAK_REGISTRATION_FAILED",
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
