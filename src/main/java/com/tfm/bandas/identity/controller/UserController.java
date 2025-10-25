package com.tfm.bandas.identity.controller;

import com.tfm.bandas.identity.dto.*;
import com.tfm.bandas.identity.service.RoleKeycloakService;
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
    private final UserKeycloakService userService;
    private final RoleKeycloakService roleService;

    @PostMapping
    public ResponseEntity<KeycloakUserResponse> createUser(@Valid @RequestBody UserRegisterDTO dto) {
        KeycloakUserResponse user = userService.createUser(dto);
        // Si el dto incluye roles, asignarlos al usuario creado
        if (dto.roles() != null && !dto.roles().isEmpty()) {
            for (String roleName : dto.roles()) {
                roleService.assignRealmRole(user.id(), roleName);
            }
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeycloakUserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<KeycloakUserDetailsResponse> getUserDetailsById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserDetailsById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<KeycloakUserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @GetMapping("/username/{username}/details")
    public ResponseEntity<KeycloakUserDetailsResponse> getUserDetailsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserDetailsByUsername(username));
    }

    @GetMapping
    public ResponseEntity<List<KeycloakUserResponse>> listAllUsers() {
        return ResponseEntity.ok(userService.listAllUsers());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/username/{username}")
    public ResponseEntity<Void> deleteUserByUsername(@PathVariable String username) {
        userService.deleteUserByUsername(username);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updateUserPassword(@PathVariable String id, @RequestBody UserPasswordUpdateDTO dto) {
        userService.updateUserPassword(id, dto.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<KeycloakUserResponse> updateUserData(@PathVariable String id, @RequestBody UserUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateUserData(id, dto.username(), dto.email(), dto.firstName(), dto.lastName()));
    }

    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> userExistsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.userExistsByUsername(username));
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> userExistsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.userExistsByEmail(email));
    }
}
