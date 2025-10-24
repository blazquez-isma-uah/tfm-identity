package com.tfm.bandas.identity.controller;

import com.tfm.bandas.identity.dto.KeycloakRoleResponse;
import com.tfm.bandas.identity.dto.RoleRegisterDTO;
import com.tfm.bandas.identity.service.RoleKeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/identity/keycloak/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleKeycloakService service;

    @GetMapping
    public ResponseEntity<List<KeycloakRoleResponse>> listAllRoles() {
        return ResponseEntity.ok(service.listAllRoles());
    }

    @PostMapping
    public ResponseEntity<Void> createRealmRole(@RequestBody RoleRegisterDTO dto) {
        service.createRealmRole(dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeycloakRoleResponse> getRoleById(@PathVariable String id) {
        return ResponseEntity.ok(service.getRoleById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<KeycloakRoleResponse> getRoleByName(@PathVariable String name) {
        return ResponseEntity.ok(service.getRoleByName(name));
    }

    @DeleteMapping("/{role}")
    public ResponseEntity<Void> deleteRealmRole(@PathVariable String role) {
        service.deleteRealmRole(role);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<KeycloakRoleResponse>> listUserRoles(@PathVariable String id) {
        return ResponseEntity.ok(service.listUserRoles(id));
    }

    @PostMapping("/user/{id}/{role}")
    public ResponseEntity<Void> assignRealmRole(@PathVariable String id, @PathVariable String role) {
        service.assignRealmRole(id, role);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{id}/{role}")
    public ResponseEntity<Void> removeRealmRole(@PathVariable String id, @PathVariable String role) {
        service.removeRealmRole(id, role);
        return ResponseEntity.noContent().build();
    }
}
