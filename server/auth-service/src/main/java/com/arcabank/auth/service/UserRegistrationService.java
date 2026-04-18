package com.arcabank.auth.service;

import com.arcabank.auth.dto.RegistrationRequest;
import com.arcabank.auth.repository.UserRepository;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final Keycloak keycloak;
    private final UserRepository userRepository;

    @Value("${keycloak.realm}")
    private String realm;

    public void registerUser(RegistrationRequest request) {
        log.info("A new squire approaches the gates. Preparing to forge identity for: {}", request.username());

        UsersResource usersResource = keycloak.realm(realm).users();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);

        Response response = null;
        try {
            response = usersResource.create(user);

            if (response.getStatus() == 201) {
                String userIdStr = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                UUID userId = UUID.fromString(userIdStr);

                CredentialRepresentation passwordCred = new CredentialRepresentation();
                passwordCred.setTemporary(false);
                passwordCred.setType(CredentialRepresentation.PASSWORD);
                passwordCred.setValue(request.password());
                usersResource.get(userIdStr).resetPassword(passwordCred);

                RoleRepresentation userRole = keycloak.realm(realm).roles().get("USER").toRepresentation();
                usersResource.get(userIdStr).roles().realmLevel().add(List.of(userRole));

                log.info("Identity forged in the citadel and recorded in local archives. Squire {} is now a Knight.", request.username());

            } else if (response.getStatus() == 409) {
                log.warn("A knight with the crest {} already exists in the archives.", request.username());
                throw new RuntimeException("User already exists!");
            } else {
                log.error("Failed to forge identity. Citadel responded with code: {}", response.getStatus());
                throw new RuntimeException("Failed to register user");
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
