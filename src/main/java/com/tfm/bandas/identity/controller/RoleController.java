package com.tfm.bandas.identity.controller;

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
    private final RoleKeycloakService service;
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @GetMapping
    public ResponseEntity<List<KeycloakRoleResponse>> listAllRoles() {
        logger.info("Calling listAllRoles");
        List<KeycloakRoleResponse> roles = service.listAllRoles();
        logger.info("listAllRoles returning: {}", roles);
        return ResponseEntity.ok(roles);
    }

    @PostMapping
    public ResponseEntity<KeycloakRoleResponse> createRealmRole(@RequestBody RoleRegisterDTO dto) {
        logger.info("Calling createRealmRole with argument: {}", dto);
        KeycloakRoleResponse role = service.createRealmRole(dto);
        logger.info("createRealmRole returning: {}", role);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeycloakRoleResponse> getRoleById(@PathVariable String id) {
        logger.info("Calling getRoleById with argument: {}", id);
        KeycloakRoleResponse role = service.getRoleById(id);
        logger.info("getRoleById returning: {}", role);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/name/{roleName}")
    public ResponseEntity<KeycloakRoleResponse> getRoleByName(@PathVariable String roleName) {
        logger.info("Calling getRoleByName with argument: {}", roleName);
        KeycloakRoleResponse role = service.getRoleByName(roleName);
        logger.info("getRoleByName returning: {}", role);
        return ResponseEntity.ok(role);
    }

    @DeleteMapping("/{roleName}")
    public ResponseEntity<Void> deleteRealmRole(@PathVariable String roleName) {
        logger.info("Calling deleteRealmRole with argument: {}", roleName);
        service.deleteRealmRole(roleName);
        logger.info("deleteRealmRole completed");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<KeycloakRoleResponse>> listUserRoles(@PathVariable String id) {
        logger.info("Calling listUserRoles with argument: {}", id);
        List<KeycloakRoleResponse> roles = service.listUserRoles(id);
        logger.info("listUserRoles returning: {}", roles);
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/user/{id}/{roleName}")
    public ResponseEntity<Void> assignRoleToUser(@PathVariable String id, @PathVariable String roleName) {
        logger.info("Calling assignRoleToUser with arguments: id={}, roleName={}", id, roleName);
        service.assignRoleToUser(id, roleName);
        logger.info("assignRoleToUser completed");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{id}/{roleName}")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable String id, @PathVariable String roleName) {
        logger.info("Calling removeRoleFromUser with arguments: id={}, roleName={}", id, roleName);
        service.removeRoleFromUser(id, roleName);
        logger.info("removeRoleFromUser completed");
        return ResponseEntity.noContent().build();
    }
}
