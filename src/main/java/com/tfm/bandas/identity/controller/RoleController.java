package com.tfm.bandas.identity.controller;

import com.tfm.bandas.identity.config.KeycloakProperties;
import com.tfm.bandas.identity.dto.KeycloakRoleResponse;
import com.tfm.bandas.identity.dto.RoleRegisterDTO;
import com.tfm.bandas.identity.service.RoleKeycloakService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/identity/keycloak/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleKeycloakService roleService;
    private final KeycloakProperties properties;
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @GetMapping
    public ResponseEntity<List<KeycloakRoleResponse>> listAllRoles() {
        logger.info("Calling listAllRoles");
        List<KeycloakRoleResponse> keycloakRoleResponses = roleService.listAllRoles();
        logger.info("listAllRoles Permitted roles for filtering: {}", properties.permittedRoles());
        List<String> permittedRolesList = List.of(properties.permittedRoles().split(","));
        logger.info("listAllRoles before filtering: {}", keycloakRoleResponses);
        List<KeycloakRoleResponse> roles = keycloakRoleResponses
                // Filtrar solo los roles que pertenecen al sistema, no los roles predeterminados de Keycloak
                .stream()
                .filter(role -> permittedRolesList.stream().anyMatch(prefix -> role.name().startsWith(prefix)))
                .toList()
                ;
        logger.info("listAllRoles returning: {}", roles);
        return ResponseEntity.ok(roles);
    }

    @PostMapping
    public ResponseEntity<KeycloakRoleResponse> createRealmRole(@RequestBody RoleRegisterDTO dto) {
        logger.info("Calling createRealmRole with argument: {}", dto);
        KeycloakRoleResponse role = roleService.createRealmRole(dto);
        logger.info("createRealmRole returning: {}", role);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<KeycloakRoleResponse> getRoleById(@PathVariable String roleId) {
        logger.info("Calling getRoleById with argument: {}", roleId);
        KeycloakRoleResponse role = roleService.getRoleById(roleId);
        logger.info("getRoleById returning: {}", role);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/name/{roleName}")
    public ResponseEntity<KeycloakRoleResponse> getRoleByName(@PathVariable String roleName) {
        logger.info("Calling getRoleByName with argument: {}", roleName);
        KeycloakRoleResponse role = roleService.getRoleByName(roleName);
        logger.info("getRoleByName returning: {}", role);
        return ResponseEntity.ok(role);
    }

    @DeleteMapping("/{roleName}")
    public ResponseEntity<Void> deleteRealmRole(@PathVariable String roleName) {
        logger.info("Calling deleteRealmRole with argument: {}", roleName);
        roleService.deleteRealmRole(roleName);
        logger.info("deleteRealmRole completed");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<KeycloakRoleResponse>> listUserRoles(@PathVariable String userId) {
        logger.info("Calling listUserRoles with argument: {}", userId);
        List<KeycloakRoleResponse> keycloakRoleResponses = roleService.listUserRoles(userId);
        logger.info("listUserRoles Permitted roles for filtering: {}", properties.permittedRoles());
        List<String> permittedRolesList = List.of(properties.permittedRoles().split(","));
        logger.info("listUserRoles before filtering: {}", keycloakRoleResponses);
        List<KeycloakRoleResponse> roles = keycloakRoleResponses
                // Filtrar solo los roles que pertenecen al sistema, no los roles predeterminados de Keycloak
                .stream()
                .filter(role -> permittedRolesList.stream().anyMatch(prefix -> role.name().startsWith(prefix)))
                .toList()
                ;
        logger.info("listUserRoles returning: {}", roles);
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/user/{userId}/{roleName}")
    public ResponseEntity<Void> assignRoleToUser(@PathVariable String userId, @PathVariable String roleName) {
        logger.info("Calling assignRoleToUser with arguments: userId={}, roleName={}", userId, roleName);
        roleService.assignRoleToUser(userId, roleName);
        logger.info("assignRoleToUser completed");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}/{roleName}")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable String userId, @PathVariable String roleName) {
        logger.info("Calling removeRoleFromUser with arguments: userId={}, roleName={}", userId, roleName);
        roleService.removeRoleFromUser(userId, roleName);
        logger.info("removeRoleFromUser completed");
        return ResponseEntity.noContent().build();
    }

}
