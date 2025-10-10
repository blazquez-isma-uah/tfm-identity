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
    private final KeycloakProperties props;

    public String getAdminToken() {
        return webClient.post()
                .uri(props.tokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", props.adminClientId())
                        .with("client_secret", props.adminClientSecret()))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::accessToken)
                .block();
    }

    public String createUser(String token, UserRegisterDTO req) {
        Map<String, Object> user = Map.of(
                "username", req.username(),
                "email", req.email(),
                "enabled", true,
                "firstName", req.firstName(),
                "lastName", req.lastName(),
                "credentials", new Object[] {
                        Map.of("type","password","value", req.password(), "temporary", false)
                }
        );
        var location = webClient.post()
                .uri(props.adminBase() + "/users")
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
    }

    public Map<String, Object> getUserById(String token, String userId) {
        return webClient.get()
                .uri(props.adminBase() + "/users/{id}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    public Map<String, Object> getUserByUsername(String token, String username) {
        List<Map<String, Object>> users = webClient.get()
                .uri(props.adminBase() + "/users?username={username}", username)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
        if (users != null && !users.isEmpty()) {
            return (Map<String, Object>) users.getFirst();
        }
        return null;
    }

    public void deleteUser(String token, String userId) {
        webClient.delete()
                .uri(props.adminBase() + "/users/{id}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .block();
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
        webClient.put()
                .uri(props.adminBase() + "/users/{id}/reset-password", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload.getFirst())
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void updateUserData(String token, String userId, String username, String email, String firstName, String lastName) {
        var payload = Map.of(
                "username", username,
                "email", email,
                "firstName", firstName,
                "lastName", lastName
        );
        webClient.put()
                .uri(props.adminBase() + "/users/{id}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public boolean userExistsByUsername(String token, String username) {
        return getUserByUsername(token, username) != null;
    }

    public boolean userExistsByEmail(String token, String email) {
        var users = webClient.get()
                .uri(props.adminBase() + "/users?email={email}", email)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(List.class)
                .block();
        return users != null && !users.isEmpty();
    }

    public KeycloakUserDetailsResponse getUserDetailsById(String token, String userId) {
        Map<String, Object> user = getUserById(token, userId);
        if (user == null) return null;
        return mapToUserDetails(user);
    }

    public KeycloakUserDetailsResponse getUserDetailsByUsername(String token, String username) {
        Map<String, Object> user = getUserByUsername(token, username);
        if (user == null) return null;
        return mapToUserDetails(user);
    }

    public List<KeycloakUserResponse> listAllUsers(String token) {
        List<Map<String, Object>> users = webClient.get()
                .uri(props.adminBase() + "/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
        if (users == null) return List.of();
        return ((List<Map<String, Object>>) users).stream()
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
