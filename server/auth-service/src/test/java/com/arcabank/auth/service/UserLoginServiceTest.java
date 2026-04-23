package com.arcabank.auth.service;

import com.arcabank.auth.dto.LoginRequest;
import com.arcabank.auth.dto.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserLoginService Tests")
class UserLoginServiceTest {

    private static final String SERVER_URL = "http://localhost:8080";
    private static final String REALM = "arcabank";
    private static final String EXPECTED_TOKEN_ENDPOINT =
        SERVER_URL + "/realms/" + REALM + "/protocol/openid-connect/token";

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private UserLoginService userLoginService;

    private LoginRequest validRequest;

    @BeforeEach
    void setUp() {
        userLoginService = new UserLoginService();
        ReflectionTestUtils.setField(userLoginService, "serverUrl", SERVER_URL);
        ReflectionTestUtils.setField(userLoginService, "realm", REALM);
        ReflectionTestUtils.setField(userLoginService, "restClient", restClient);

        validRequest = new LoginRequest("john.doe@example.com", "StrongPassword123!");
    }

    private void mockRestClientChain() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any(MediaType.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(MultiValueMap.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    @Nested
    @DisplayName("Successful Authentication")
    class SuccessfulAuthentication {

        @Test
        @DisplayName("Should return TokenResponse on successful authentication")
        void shouldReturnTokenResponse() {
            TokenResponse expectedResponse = new TokenResponse(
                "access-token-value",
                "Bearer",
                300
            );
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class)).thenReturn(expectedResponse);

            TokenResponse result = userLoginService.authenticate(validRequest);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("Should call correct token endpoint URL")
        void shouldCallCorrectTokenEndpoint() {
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenReturn(new TokenResponse("token", "Bearer", 300));

            userLoginService.authenticate(validRequest);

            ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
            verify(requestBodyUriSpec).uri(uriCaptor.capture());
            assertThat(uriCaptor.getValue()).isEqualTo(EXPECTED_TOKEN_ENDPOINT);
        }

        @Test
        @DisplayName("Should set Content-Type as application/x-www-form-urlencoded")
        void shouldSetCorrectContentType() {
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenReturn(new TokenResponse("token", "Bearer", 300));

            userLoginService.authenticate(validRequest);

            verify(requestBodyUriSpec).contentType(MediaType.APPLICATION_FORM_URLENCODED);
        }

        @Test
        @DisplayName("Should send correct form data with all OAuth2 fields")
        void shouldSendCorrectFormData() {
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenReturn(new TokenResponse("token", "Bearer", 300));

            userLoginService.authenticate(validRequest);

            ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);
            verify(requestBodyUriSpec).body(captor.capture());

            MultiValueMap<String, String> formData = captor.getValue();
            assertThat(formData.getFirst("grant_type")).isEqualTo("password");
            assertThat(formData.getFirst("client_id")).isEqualTo("arcabank-frontend");
            assertThat(formData.getFirst("username")).isEqualTo(validRequest.email());
            assertThat(formData.getFirst("password")).isEqualTo(validRequest.password());
        }

        @Test
        @DisplayName("Should return null when Keycloak returns null body")
        void shouldReturnNullBody() {
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class)).thenReturn(null);

            TokenResponse result = userLoginService.authenticate(validRequest);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Failed Authentication")
    class FailedAuthentication {

        @Test
        @DisplayName("Should throw RuntimeException with 'Invalid email or password' on 401")
        void shouldThrowOnUnauthorized() {
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenThrow(HttpClientErrorException.create(
                    HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null
                ));

            assertThatThrownBy(() -> userLoginService.authenticate(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw RuntimeException on 400 Bad Request")
        void shouldThrowOnBadRequest() {
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenThrow(HttpClientErrorException.create(
                    HttpStatus.BAD_REQUEST, "Bad Request", null, null, null
                ));

            assertThatThrownBy(() -> userLoginService.authenticate(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw RuntimeException on network failure")
        void shouldThrowOnNetworkFailure() {
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenThrow(new ResourceAccessException("Connection refused"));

            assertThatThrownBy(() -> userLoginService.authenticate(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw RuntimeException on 500 Internal Server Error from Keycloak")
        void shouldThrowOnKeycloakServerError() {
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenThrow(HttpClientErrorException.create(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Server Error", null, null, null
                ));

            assertThatThrownBy(() -> userLoginService.authenticate(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw RuntimeException on generic exception")
        void shouldThrowOnGenericException() {
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenThrow(new RuntimeException("Unexpected error"));

            assertThatThrownBy(() -> userLoginService.authenticate(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
        }

        @Test
        @DisplayName("Should throw RuntimeException when retrieve() throws")
        void shouldThrowWhenRetrieveFails() {
            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(any(MediaType.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(MultiValueMap.class))).thenReturn(requestBodySpec);
            when(requestBodySpec.retrieve()).thenThrow(new RuntimeException("Retrieve failed"));

            assertThatThrownBy(() -> userLoginService.authenticate(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid email or password");
        }
    }

    @Nested
    @DisplayName("Endpoint URL Construction")
    class EndpointConstruction {

        @Test
        @DisplayName("Should construct URL correctly with different realm")
        void shouldConstructUrlWithDifferentRealm() {
            ReflectionTestUtils.setField(userLoginService, "realm", "another-realm");
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenReturn(new TokenResponse("token", "Bearer", 300));

            userLoginService.authenticate(validRequest);

            ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
            verify(requestBodyUriSpec).uri(uriCaptor.capture());
            assertThat(uriCaptor.getValue())
                .isEqualTo(SERVER_URL + "/realms/another-realm/protocol/openid-connect/token");
        }

        @Test
        @DisplayName("Should construct URL correctly with different server URL")
        void shouldConstructUrlWithDifferentServerUrl() {
            String customServerUrl = "https://auth.example.com";
            ReflectionTestUtils.setField(userLoginService, "serverUrl", customServerUrl);
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenReturn(new TokenResponse("token", "Bearer", 300));

            userLoginService.authenticate(validRequest);

            ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
            verify(requestBodyUriSpec).uri(uriCaptor.capture());
            assertThat(uriCaptor.getValue())
                .isEqualTo(customServerUrl + "/realms/" + REALM + "/protocol/openid-connect/token");
        }
    }

    @Nested
    @DisplayName("Input Handling")
    class InputHandling {

        @Test
        @DisplayName("Should handle request with different valid credentials")
        void shouldHandleDifferentCredentials() {
            LoginRequest anotherRequest = new LoginRequest("jane.smith@example.com", "AnotherPass456!");
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenReturn(new TokenResponse("token", "Bearer", 300));

            userLoginService.authenticate(anotherRequest);

            ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);
            verify(requestBodyUriSpec).body(captor.capture());

            MultiValueMap<String, String> formData = captor.getValue();
            assertThat(formData.getFirst("username")).isEqualTo("jane.smith@example.com");
            assertThat(formData.getFirst("password")).isEqualTo("AnotherPass456!");
        }

        @Test
        @DisplayName("Should always use 'password' grant_type")
        void shouldAlwaysUsePasswordGrantType() {
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenReturn(new TokenResponse("token", "Bearer", 300));

            userLoginService.authenticate(validRequest);

            ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);
            verify(requestBodyUriSpec).body(captor.capture());
            assertThat(captor.getValue().getFirst("grant_type")).isEqualTo("password");
        }

        @Test
        @DisplayName("Should always use 'arcabank-frontend' as client_id")
        void shouldAlwaysUseCorrectClientId() {
            mockRestClientChain();
            when(responseSpec.body(TokenResponse.class))
                .thenReturn(new TokenResponse("token", "Bearer", 300));

            userLoginService.authenticate(validRequest);

            ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);
            verify(requestBodyUriSpec).body(captor.capture());
            assertThat(captor.getValue().getFirst("client_id")).isEqualTo("arcabank-frontend");
        }
    }
}
