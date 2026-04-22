package com.arcabank.auth.service;

import com.arcabank.auth.dto.RegistrationRequest;
import com.arcabank.auth.exception.AppException;
import com.arcabank.auth.repository.UserRepository;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // annotation for ignoring unused stabs
@DisplayName("UserRegistrationService Tests")
class UserRegistrationServiceTest {

    private static final String REALM = "arcabank";
    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String LOCATION_PATH = "/admin/realms/arcabank/users/" + USER_ID;

    @Mock
    private Keycloak keycloak;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private RoleResource roleResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private RoleScopeResource roleScopeResource;

    @Mock
    private Response response;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    private RegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userRegistrationService, "realm", REALM);
        validRequest = new RegistrationRequest(
            "AB123456",
            "john.doe@example.com",
            "John",
            "Doe",
            "StrongPassword123!",
            "+380501234567"
        );
    }

    private void mockSuccessfulKeycloakFlow() {
        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
    }

    private void mockSuccessfulUserCreation() {
        mockSuccessfulKeycloakFlow();
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(URI.create(LOCATION_PATH));

        when(usersResource.get(USER_ID)).thenReturn(userResource);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(rolesResource.get("USER")).thenReturn(roleResource);

        RoleRepresentation userRole = new RoleRepresentation();
        userRole.setName("USER");
        when(roleResource.toRepresentation()).thenReturn(userRole);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    }

    @Nested
    @DisplayName("Successful Registration")
    class SuccessfulRegistration {

        @Test
        @DisplayName("Should register user successfully when Keycloak returns 201")
        void shouldRegisterUserSuccessfully() {
            mockSuccessfulUserCreation();

            userRegistrationService.registerUser(validRequest);

            verify(usersResource).create(any(UserRepresentation.class));
            verify(userResource).resetPassword(any(CredentialRepresentation.class));
            verify(roleScopeResource).add(any(List.class));
            verify(userRepository).syncUser(
                UUID.fromString(USER_ID),
                validRequest.email(),
                validRequest.firstName(),
                validRequest.lastName(),
                validRequest.passport_id(),
                validRequest.phoneNumber()
            );
            verify(response).close();
        }

        @Test
        @DisplayName("Should build UserRepresentation with correct fields")
        void shouldBuildUserRepresentationCorrectly() {
            mockSuccessfulUserCreation();

            userRegistrationService.registerUser(validRequest);

            ArgumentCaptor<UserRepresentation> captor = ArgumentCaptor.forClass(UserRepresentation.class);
            verify(usersResource).create(captor.capture());

            UserRepresentation captured = captor.getValue();
            assertThat(captured.getUsername()).isEqualTo(validRequest.passport_id());
            assertThat(captured.getEmail()).isEqualTo(validRequest.email());
            assertThat(captured.getFirstName()).isEqualTo(validRequest.firstName());
            assertThat(captured.getLastName()).isEqualTo(validRequest.lastName());
            assertThat(captured.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("Should set password as non-temporary with correct value")
        void shouldSetPasswordCorrectly() {
            mockSuccessfulUserCreation();

            userRegistrationService.registerUser(validRequest);

            ArgumentCaptor<CredentialRepresentation> captor = ArgumentCaptor.forClass(CredentialRepresentation.class);
            verify(userResource).resetPassword(captor.capture());

            CredentialRepresentation credential = captor.getValue();
            assertThat(credential.isTemporary()).isFalse();
            assertThat(credential.getType()).isEqualTo(CredentialRepresentation.PASSWORD);
            assertThat(credential.getValue()).isEqualTo(validRequest.password());
        }

        @Test
        @DisplayName("Should assign USER role to the new user")
        void shouldAssignUserRole() {
            mockSuccessfulUserCreation();

            userRegistrationService.registerUser(validRequest);

            ArgumentCaptor<List<RoleRepresentation>> captor = ArgumentCaptor.forClass(List.class);
            verify(roleScopeResource).add(captor.capture());

            List<RoleRepresentation> roles = captor.getValue();
            assertThat(roles).hasSize(1);
            assertThat(roles.get(0).getName()).isEqualTo("USER");
        }

        @Test
        @DisplayName("Should extract userId correctly from complex location path")
        void shouldExtractUserIdFromLocationPath() {
            mockSuccessfulKeycloakFlow();
            when(response.getStatus()).thenReturn(201);
            when(response.getLocation())
                .thenReturn(URI.create("http://keycloak:8080/admin/realms/arcabank/users/" + USER_ID));

            when(usersResource.get(USER_ID)).thenReturn(userResource);
            when(realmResource.roles()).thenReturn(rolesResource);
            when(rolesResource.get("USER")).thenReturn(roleResource);
            when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());
            when(userResource.roles()).thenReturn(roleMappingResource);
            when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

            userRegistrationService.registerUser(validRequest);

            verify(usersResource, times(2)).get(USER_ID);
            verify(userRepository).syncUser(
                eq(UUID.fromString(USER_ID)),
                anyString(), anyString(), anyString(), anyString(), anyString()
            );
        }

        @Test
        @DisplayName("Should call operations in correct order")
        void shouldCallOperationsInCorrectOrder() {
            mockSuccessfulUserCreation();

            userRegistrationService.registerUser(validRequest);

            var inOrder = org.mockito.Mockito.inOrder(
                usersResource, userResource, roleScopeResource, userRepository, response
            );
            inOrder.verify(usersResource).create(any(UserRepresentation.class));
            inOrder.verify(userResource).resetPassword(any(CredentialRepresentation.class));
            inOrder.verify(roleScopeResource).add(any(List.class));
            inOrder.verify(userRepository).syncUser(
                any(UUID.class), anyString(), anyString(), anyString(), anyString(), anyString()
            );
            inOrder.verify(response).close();
        }
    }

    @Nested
    @DisplayName("Conflict Handling")
    class ConflictHandling {

        @Test
        @DisplayName("Should throw AppException with CONFLICT status when user already exists (409)")
        void shouldThrowConflictWhenUserExists() {
            mockSuccessfulKeycloakFlow();
            when(response.getStatus()).thenReturn(409);

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(AppException.class)
                .hasMessage("User already exists!")
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("USER_ALREADY_EXISTS");
                });

            verify(response).close();
            verify(userRepository, never()).syncUser(
                any(), anyString(), anyString(), anyString(), anyString(), anyString()
            );
            verify(userResource, never()).resetPassword(any());
        }

        @Test
        @DisplayName("Should not set password or role when user creation conflicts")
        void shouldNotProceedOnConflict() {
            mockSuccessfulKeycloakFlow();
            when(response.getStatus()).thenReturn(409);

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(AppException.class);

            verify(usersResource, never()).get(anyString());
            verify(realmResource, never()).roles();
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should throw AppException with INTERNAL_SERVER_ERROR on 500")
        void shouldThrowInternalServerErrorOn500() {
            mockSuccessfulKeycloakFlow();
            when(response.getStatus()).thenReturn(500);

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(AppException.class)
                .hasMessage("Failed to register user!")
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(appEx.getErrorCode()).isEqualTo("KEYCLOAK_REGISTRATION_FAILED");
                });

            verify(response).close();
        }

        @Test
        @DisplayName("Should throw AppException on 400 Bad Request")
        void shouldThrowOnBadRequest() {
            mockSuccessfulKeycloakFlow();
            when(response.getStatus()).thenReturn(400);

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(AppException.class)
                .hasMessage("Failed to register user!");

            verify(response).close();
        }

        @Test
        @DisplayName("Should throw AppException on 401 Unauthorized")
        void shouldThrowOnUnauthorized() {
            mockSuccessfulKeycloakFlow();
            when(response.getStatus()).thenReturn(401);

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(AppException.class);

            verify(response).close();
        }

        @Test
        @DisplayName("Should throw AppException on 403 Forbidden")
        void shouldThrowOnForbidden() {
            mockSuccessfulKeycloakFlow();
            when(response.getStatus()).thenReturn(403);

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(AppException.class);

            verify(response).close();
        }

        @Test
        @DisplayName("Should propagate exception when Keycloak throws during create")
        void shouldPropagateKeycloakException() {
            when(keycloak.realm(REALM)).thenReturn(realmResource);
            when(realmResource.users()).thenReturn(usersResource);
            when(usersResource.create(any(UserRepresentation.class)))
                .thenThrow(new RuntimeException("Keycloak is down"));

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Keycloak is down");

            verify(userRepository, never()).syncUser(
                any(), anyString(), anyString(), anyString(), anyString(), anyString()
            );
        }

        @Test
        @DisplayName("Should propagate exception when password reset fails")
        void shouldPropagatePasswordResetException() {
            mockSuccessfulUserCreation();
            org.mockito.Mockito.doThrow(new RuntimeException("Password reset failed"))
                .when(userResource).resetPassword(any(CredentialRepresentation.class));

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Password reset failed");

            verify(response).close();
            verify(userRepository, never()).syncUser(
                any(), anyString(), anyString(), anyString(), anyString(), anyString()
            );
        }

        @Test
        @DisplayName("Should propagate exception when role assignment fails")
        void shouldPropagateRoleAssignmentException() {
            mockSuccessfulUserCreation();
            org.mockito.Mockito.doThrow(new RuntimeException("Role assignment failed"))
                .when(roleScopeResource).add(any(List.class));

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role assignment failed");

            verify(response).close();
            verify(userRepository, never()).syncUser(
                any(), anyString(), anyString(), anyString(), anyString(), anyString()
            );
        }

        @Test
        @DisplayName("Should propagate exception when repository sync fails")
        void shouldPropagateRepositorySyncException() {
            mockSuccessfulUserCreation();
            org.mockito.Mockito.doThrow(new RuntimeException("DB sync failed"))
                .when(userRepository).syncUser(
                    any(UUID.class), anyString(), anyString(), anyString(), anyString(), anyString()
                );

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB sync failed");

            verify(response).close();
        }
    }

    @Nested
    @DisplayName("Resource Management")
    class ResourceManagement {

        @Test
        @DisplayName("Should close response even when exception is thrown on 409")
        void shouldCloseResponseOnConflict() {
            mockSuccessfulKeycloakFlow();
            when(response.getStatus()).thenReturn(409);

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(AppException.class);

            verify(response).close();
        }

        @Test
        @DisplayName("Should close response even when exception is thrown on 500")
        void shouldCloseResponseOnServerError() {
            mockSuccessfulKeycloakFlow();
            when(response.getStatus()).thenReturn(500);

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(AppException.class);

            verify(response).close();
        }

        @Test
        @DisplayName("Should close response after successful registration")
        void shouldCloseResponseAfterSuccess() {
            mockSuccessfulUserCreation();

            userRegistrationService.registerUser(validRequest);

            verify(response).close();
        }

        @Test
        @DisplayName("Should not throw NPE when response is null (Keycloak threw before returning)")
        void shouldHandleNullResponseGracefully() {
            when(keycloak.realm(REALM)).thenReturn(realmResource);
            when(realmResource.users()).thenReturn(usersResource);
            when(usersResource.create(any(UserRepresentation.class)))
                .thenThrow(new RuntimeException("Network failure"));

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Network failure");
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        @DisplayName("Should throw IllegalArgumentException for invalid UUID in Keycloak response")
        void shouldThrowOnInvalidUUID() {
            mockSuccessfulKeycloakFlow();
            when(response.getStatus()).thenReturn(201);
            when(response.getLocation())
                .thenReturn(URI.create("/admin/realms/arcabank/users/not-a-valid-uuid"));

            when(usersResource.get("not-a-valid-uuid")).thenReturn(userResource);
            when(realmResource.roles()).thenReturn(rolesResource);
            when(rolesResource.get("USER")).thenReturn(roleResource);
            when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());
            when(userResource.roles()).thenReturn(roleMappingResource);
            when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);

            assertThatThrownBy(() -> userRegistrationService.registerUser(validRequest))
                .isInstanceOf(IllegalArgumentException.class);

            verify(response).close();
        }

        @Test
        @DisplayName("Should handle request with different valid data")
        void shouldHandleDifferentValidInput() {
            RegistrationRequest anotherRequest = new RegistrationRequest(
                "CD987654",
                "jane.smith@example.com",
                "Jane",
                "Smith",
                "AnotherPass456!",
                "+380671234567"
            );

            mockSuccessfulUserCreation();

            userRegistrationService.registerUser(anotherRequest);

            ArgumentCaptor<UserRepresentation> captor = ArgumentCaptor.forClass(UserRepresentation.class);
            verify(usersResource).create(captor.capture());
            assertThat(captor.getValue().getUsername()).isEqualTo("CD987654");
            assertThat(captor.getValue().getEmail()).isEqualTo("jane.smith@example.com");
        }
    }
}
