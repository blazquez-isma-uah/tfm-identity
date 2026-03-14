package com.tfm.bandas.identity.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tfm.bandas.identity.config.KeycloakProperties;
import com.tfm.bandas.identity.dto.KeycloakRoleResponse;
import com.tfm.bandas.identity.dto.RoleRegisterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleKeycloakApiClient {
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

    public Map<String, Object> getRealmRoleById(String token, String roleId) {
        try {
            return webClient.get()
                    .uri(properties.adminBase() + "/roles-by-id/{id}", roleId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve realm role by ID from Keycloak: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getRealmRoleByName(String token, String roleName) {
        try {
            return webClient.get()
                    .uri(properties.adminBase() + "/roles/{role}", roleName)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve realm role by name from Keycloak: " + e.getMessage(), e);
        }
    }

    public void assignRoleToUser(String token, String userId, Map<String, Object> roleRep) {
        var payload = new Object[] { Map.of(
                "id", roleRep.get("id"),
                "name", roleRep.get("name")
        )};
        try {
            webClient.post()
                    .uri(properties.adminBase() + "/users/{id}/role-mappings/realm", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign role to user in Keycloak: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> listUserRoles(String token, String userId) {
        try {
            return webClient.get()
                    .uri(properties.adminBase() + "/users/{id}/role-mappings/realm", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list user roles in Keycloak: " + e.getMessage(), e);
        }
    }

    public void removeRoleFromUser(String token, String userId, Map<String, Object> roleRep) {
        var payload = new Object[] { Map.of(
                "id", roleRep.get("id"),
                "name", roleRep.get("name")
        )};
        try {
            webClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri(properties.adminBase() + "/users/{id}/role-mappings/realm", userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove role from user in Keycloak: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> listAllRoles(String token) {
        try {
            return webClient.get()
                    .uri(properties.adminBase() + "/roles")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to list all roles in Keycloak: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> createRealmRole(String token, RoleRegisterDTO dto) {
        var payload = Map.of(
            "name", dto.name(),
            "description", dto.description()
        );
        try {
            return webClient.post()
                    .uri(properties.adminBase() + "/roles")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create realm role in Keycloak: " + e.getMessage(), e);
        }
    }

    public void deleteRealmRole(String token, String roleName) {
        try {
            webClient.delete()
                    .uri(properties.adminBase() + "/roles/{role}", roleName)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete realm role in Keycloak: " + e.getMessage(), e);
        }
    }

    public List<KeycloakRoleResponse> listUserRolesDto(String token, String userId) {
        var roles = listUserRoles(token, userId);
        if (roles == null) return List.of();
        return roles.stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    public List<KeycloakRoleResponse> listAllRolesDto(String token) {
        var roles = listAllRoles(token);
        if (roles == null) return List.of();
        return roles.stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    public KeycloakRoleResponse createRealmRoleDto(String token, RoleRegisterDTO dto) {
        createRealmRole(token, dto);
        var role = getRealmRoleByName(token, dto.name());
        return mapToRoleResponse(role);
    }

    public KeycloakRoleResponse getRealmRoleByIdDto(String token, String roleId) {
        var role = getRealmRoleById(token, roleId);
        return role != null ? mapToRoleResponse(role) : null;
    }

    public KeycloakRoleResponse getRealmRoleByNameDto(String token, String roleName) {
        var role = getRealmRoleByName(token, roleName);
        return role != null ? mapToRoleResponse(role) : null;
    }

    private KeycloakRoleResponse mapToRoleResponse(Map<String, Object> role) {
        return new KeycloakRoleResponse(
                (String) role.get("id"),
                (String) role.get("name"),
                (String) role.getOrDefault("description", null)
        );
    }

    private record TokenResponse(
            @JsonProperty("access_token") String accessToken
    ) {}
}
