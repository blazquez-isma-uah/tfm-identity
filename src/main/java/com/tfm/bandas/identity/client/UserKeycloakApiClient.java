package com.tfm.bandas.identity.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfm.bandas.identity.config.KeycloakProperties;
import com.tfm.bandas.identity.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserKeycloakApiClient {
    private final WebClient webClient;
    private final KeycloakProperties properties;

    public String getAdminToken() {
        try {
            return webClient.post()
                    .uri(properties.tokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                            .with("client_id", properties.adminClientId())
                            .with("client_secret", properties.adminClientSecret()))
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .map(TokenResponse::accessToken)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain admin token from Keycloak. Check credentials and server availability.", e);
        }
    }

    public String createUser(String token, UserRegisterDTO req) {
        Map<String, Object> user = Map.of(
                "username", req.username(),
                "email", req.email(),
                "emailVerified", true,
                "enabled", true,
                "firstName", req.firstName(),
                "lastName", req.lastName(),
                "credentials", new Object[] {
                        Map.of("type","password","value", req.password(), "temporary", false)
                }
        );
        try {
            var location = webClient.post()
                    .uri(properties.adminBase() + "/users")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(user)
                    .exchangeToMono(resp -> {
                        if (resp.statusCode().is2xxSuccessful()) {
                            String loc = resp.headers().asHttpHeaders().getFirst(HttpHeaders.LOCATION);
                            if (loc != null) {
                                return resp.bodyToMono(Void.class).thenReturn(loc);
                            }
                        }
                        return resp.createException().flatMap(e ->
                                reactor.core.publisher.Mono.error(
                                        new RuntimeException("Keycloak createUser failed: " + e.getMessage(), e)));
                    })
                    .block();
            if (location == null) {
                throw new IllegalStateException("Keycloak did not return Location header");
            }
            return URI.create(location).getPath().replaceAll(".*/users/", "");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user in Keycloak: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getUserById(String token, String userId) {
        try {
            var user = webClient.get()
                    .uri(properties.adminBase() + "/users/{id}", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            return user != null ? user : Map.of();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve user by ID from Keycloak: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getUserByUsername(String token, String username) {
        try {
            List<Map<String, Object>> users = webClient.get()
                    .uri(properties.adminBase() + "/users?username={username}", username)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
            if (users != null && !users.isEmpty()) {
                return users.getFirst();
            }
            return Map.of();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve user by username from Keycloak: " + e.getMessage(), e);
        }
    }

    public void deleteUser(String token, String userId) {
        try {
            webClient.delete()
                    .uri(properties.adminBase() + "/users/{id}", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete user in Keycloak: " + e.getMessage(), e);
        }
    }

    public void deleteUserByUsername(String token, String username) {
        Map<String, Object> user = getUserByUsername(token, username);
        if (user != null && user.get("id") != null) {
            deleteUser(token, (String) user.get("id"));
        }
    }

    public void updateUserPassword(String token, String userId, String newPassword) {
        var payload = List.of(Map.of(
                "type", "password",
                "value", newPassword,
                "temporary", false
        ));
        try {
            webClient.put()
                    .uri(properties.adminBase() + "/users/{id}/reset-password", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload.getFirst())
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user password in Keycloak: " + e.getMessage(), e);
        }
    }

    public void updateUserData(String token, String userId, String username, String email, String firstName, String lastName) {
        var payload = Map.of(
                "username", username,
                "email", email,
                "firstName", firstName,
                "lastName", lastName
        );
        try {
            webClient.put()
                    .uri(properties.adminBase() + "/users/{id}", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user data in Keycloak: " + e.getMessage(), e);
        }
    }

    public boolean userExistsByUsername(String token, String username) {
        return getUserByUsername(token, username) != null;
    }

    public boolean userExistsByEmail(String token, String email) {
        try {
            var users = webClient.get()
                    .uri(properties.adminBase() + "/users?email={email}", email)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            return users != null && !users.isEmpty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to check user existence by email in Keycloak: " + e.getMessage(), e);
        }
    }

    public KeycloakUserDetailsResponse getUserDetailsById(String token, String userId) {
        Map<String, Object> user = getUserById(token, userId);
        if (user.isEmpty()) return null;
        return mapToUserDetails(user);
    }

    public KeycloakUserDetailsResponse getUserDetailsByUsername(String token, String username) {
        Map<String, Object> user = getUserByUsername(token, username);
        if (user.isEmpty()) return null;
        return mapToUserDetails(user);
    }

    public List<KeycloakUserResponse> listAllUsers(String token) {
        List<Map<String, Object>> users = webClient.get()
                .uri(properties.adminBase() + "/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
        if (users == null) return List.of();
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private KeycloakUserResponse mapToUserResponse(Map<String, Object> user) {
        return new KeycloakUserResponse(
                (String) user.get("id"),
                (String) user.get("username")
        );
    }

    private KeycloakUserDetailsResponse mapToUserDetails(Map<String, Object> user) {
        return new KeycloakUserDetailsResponse(
                (String) user.get("id"),
                (String) user.get("username"),
                (String) user.get("email"),
                (String) user.get("firstName"),
                (String) user.get("lastName")
        );
    }

    private record TokenResponse(
            @JsonProperty("access_token") String accessToken
    ) {}
}
