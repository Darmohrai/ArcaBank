package com.arcabank.auth.service;

import com.arcabank.auth.dto.RegistrationRequest;
import com.arcabank.auth.exception.AppException;
import com.arcabank.auth.repository.UserRepository;
import com.arcabank.grpc.AccountProvisioningServiceGrpc;
import com.arcabank.grpc.CreateAccountRequest;
import com.arcabank.grpc.CreateAccountResponse;
import com.arcabank.grpc.ProtoCurrency;
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
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private AccountProvisioningServiceGrpc.AccountProvisioningServiceBlockingStub accountProvisioningStub;

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

        ReflectionTestUtils.setField(userRegistrationService, "accountProvisioningStub", accountProvisioningStub);

        validRequest = new RegistrationRequest(
            "AB123456",
            "john.doe@example.com",
            "John",
            "Doe",
            "StrongPassword123!",
            "+380501234567");
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

    private void mockSuccessfulGrpcResponse() {
        CreateAccountResponse grpcResponse = CreateAccountResponse.newBuilder()
            .setSuccess(true)
            .setAccountId("ACC-12345")
            .build();
        when(accountProvisioningStub.createInitialAccount(any(CreateAccountRequest.class)))
            .thenReturn(grpcResponse);
    }

    @Nested
    @DisplayName("Successful Registration")
    class SuccessfulRegistration {

        @Test
        @DisplayName("Should register user successfully and call gRPC to provision account")
        void shouldRegisterUserSuccessfully() {
            mockSuccessfulUserCreation();
            mockSuccessfulGrpcResponse();

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

            ArgumentCaptor<CreateAccountRequest> grpcCaptor = ArgumentCaptor.forClass(CreateAccountRequest.class);
            verify(accountProvisioningStub).createInitialAccount(grpcCaptor.capture());
            assertThat(grpcCaptor.getValue().getUserId()).isEqualTo(USER_ID);
            assertThat(grpcCaptor.getValue().getCurrency()).isEqualTo(ProtoCurrency.CURRENCY_UAH);

            verify(response).close();
        }

        @Test
        @DisplayName("Should build UserRepresentation with correct fields")
        void shouldBuildUserRepresentationCorrectly() {
            mockSuccessfulUserCreation();
            mockSuccessfulGrpcResponse();

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
        @DisplayName("Should call operations in correct order including gRPC")
        void shouldCallOperationsInCorrectOrder() {
            mockSuccessfulUserCreation();
            mockSuccessfulGrpcResponse();

            userRegistrationService.registerUser(validRequest);

            var inOrder = org.mockito.Mockito.inOrder(
                usersResource, response, userResource, roleScopeResource, userRepository, accountProvisioningStub
            );

            inOrder.verify(usersResource).create(any(UserRepresentation.class));
            inOrder.verify(response).close();
            inOrder.verify(userResource).resetPassword(any(CredentialRepresentation.class));
            inOrder.verify(roleScopeResource).add(any(List.class));
            inOrder.verify(userRepository).syncUser(
                any(UUID.class), anyString(), anyString(), anyString(), anyString(), anyString()
            );
            inOrder.verify(accountProvisioningStub).createInitialAccount(any(CreateAccountRequest.class));
        }
    }

    @Nested
    @DisplayName("gRPC Resiliency Handling")
    class GrpcResiliencyHandling {

        @Test
        @DisplayName("Should not throw exception when gRPC returns failure status")
        void shouldNotThrowWhenGrpcReturnsFalse() {
            mockSuccessfulUserCreation();

            CreateAccountResponse failedGrpcResponse = CreateAccountResponse.newBuilder()
                .setSuccess(false)
                .build();
            when(accountProvisioningStub.createInitialAccount(any(CreateAccountRequest.class)))
                .thenReturn(failedGrpcResponse);

            // Registration should succeed even if account provisioning fails internally
            userRegistrationService.registerUser(validRequest);

            verify(userRepository).syncUser(any(), anyString(), anyString(), anyString(), anyString(), anyString());
            verify(accountProvisioningStub).createInitialAccount(any(CreateAccountRequest.class));
        }

        @Test
        @DisplayName("Should not throw exception when gRPC throws runtime exception")
        void shouldNotThrowWhenGrpcThrowsException() {
            mockSuccessfulUserCreation();

            when(accountProvisioningStub.createInitialAccount(any(CreateAccountRequest.class)))
                .thenThrow(new RuntimeException("gRPC Server Unavailable"));

            // Exception is caught and logged, registration completes
            userRegistrationService.registerUser(validRequest);

            verify(userRepository).syncUser(any(), anyString(), anyString(), anyString(), anyString(), anyString());
            verify(accountProvisioningStub).createInitialAccount(any(CreateAccountRequest.class));
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
            verify(userRepository, never()).syncUser(any(), anyString(), anyString(), anyString(), anyString(), anyString());
            verify(accountProvisioningStub, never()).createInitialAccount(any());
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
                .hasMessage("Failed to register user!");

            verify(response).close();
            verify(accountProvisioningStub, never()).createInitialAccount(any());
        }

        @Test
        @DisplayName("Should propagate exception when DB sync fails and not call gRPC")
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
            verify(accountProvisioningStub, never()).createInitialAccount(any());
        }
    }
}
