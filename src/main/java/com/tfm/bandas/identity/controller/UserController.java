package com.tfm.bandas.identity.controller;

import com.tfm.bandas.identity.dto.*;
import com.tfm.bandas.identity.service.UserKeycloakService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/identity/keycloak/users")
@RequiredArgsConstructor
public class UserController {
    private final UserKeycloakService service;

    @PostMapping
    public ResponseEntity<KeycloakUserResponse> createUser(@Valid @RequestBody UserRegisterDTO dto) {
        return ResponseEntity.ok(service.createUser(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeycloakUserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(service.getUserById(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<KeycloakUserDetailsResponse> getUserDetailsById(@PathVariable String id) {
        return ResponseEntity.ok(service.getUserDetailsById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<KeycloakUserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(service.getUserByUsername(username));
    }

    @GetMapping("/username/{username}/details")
    public ResponseEntity<KeycloakUserDetailsResponse> getUserDetailsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(service.getUserDetailsByUsername(username));
    }

    @GetMapping
    public ResponseEntity<List<KeycloakUserResponse>> listAllUsers() {
        return ResponseEntity.ok(service.listAllUsers());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable String id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/username/{username}")
    public ResponseEntity<Void> deleteUserByUsername(@PathVariable String username) {
        service.deleteUserByUsername(username);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updateUserPassword(@PathVariable String id, @RequestBody UserPasswordUpdateDTO dto) {
        service.updateUserPassword(id, dto.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUserData(@PathVariable String id, @RequestBody UserUpdateDTO dto) {
        service.updateUserData(id, dto.username(), dto.email(), dto.firstName(), dto.lastName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> userExistsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(service.userExistsByUsername(username));
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> userExistsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.userExistsByEmail(email));
    }
}
