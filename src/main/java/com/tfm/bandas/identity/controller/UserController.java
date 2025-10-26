package com.tfm.bandas.identity.controller;

import com.tfm.bandas.identity.dto.*;
import com.tfm.bandas.identity.service.RoleKeycloakService;
import com.tfm.bandas.identity.service.UserKeycloakService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/identity/keycloak/users")
@RequiredArgsConstructor
public class UserController {
    private final UserKeycloakService userService;
    private final RoleKeycloakService roleService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public ResponseEntity<KeycloakUserResponse> createUser(@Valid @RequestBody UserRegisterDTO dto) {
        logger.info("Calling createUser with argument: {}", dto);
        KeycloakUserResponse user = userService.createUser(dto);
        // Si el dto incluye roles, asignarlos al usuario creado
        if (dto.roles() != null && !dto.roles().isEmpty()) {
            for (String roleName : dto.roles()) {
                roleService.assignRealmRole(user.id(), roleName);
            }
        }
        logger.info("createUser returning: {}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeycloakUserResponse> getUserById(@PathVariable String id) {
        logger.info("Calling getUserById with argument: {}", id);
        KeycloakUserResponse user = userService.getUserById(id);
        logger.info("getUserById returning: {}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<KeycloakUserDetailsResponse> getUserDetailsById(@PathVariable String id) {
        logger.info("Calling getUserDetailsById with argument: {}", id);
        KeycloakUserDetailsResponse details = userService.getUserDetailsById(id);
        logger.info("getUserDetailsById returning: {}", details);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<KeycloakUserResponse> getUserByUsername(@PathVariable String username) {
        logger.info("Calling getUserByUsername with argument: {}", username);
        KeycloakUserResponse user = userService.getUserByUsername(username);
        logger.info("getUserByUsername returning: {}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}/details")
    public ResponseEntity<KeycloakUserDetailsResponse> getUserDetailsByUsername(@PathVariable String username) {
        logger.info("Calling getUserDetailsByUsername with argument: {}", username);
        KeycloakUserDetailsResponse details = userService.getUserDetailsByUsername(username);
        logger.info("getUserDetailsByUsername returning: {}", details);
        return ResponseEntity.ok(details);
    }

    @GetMapping
    public ResponseEntity<List<KeycloakUserResponse>> listAllUsers() {
        logger.info("Calling listAllUsers");
        List<KeycloakUserResponse> users = userService.listAllUsers();
        logger.info("listAllUsers returning: {}", users);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable String id) {
        logger.info("Calling deleteUserById with argument: {}", id);
        userService.deleteUser(id);
        logger.info("deleteUserById completed");
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/username/{username}")
    public ResponseEntity<Void> deleteUserByUsername(@PathVariable String username) {
        logger.info("Calling deleteUserByUsername with argument: {}", username);
        userService.deleteUserByUsername(username);
        logger.info("deleteUserByUsername completed");
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updateUserPassword(@PathVariable String id, @RequestBody UserPasswordUpdateDTO dto) {
        logger.info("Calling updateUserPassword with arguments: id={}, newPassword={}", id, dto.newPassword());
        userService.updateUserPassword(id, dto.newPassword());
        logger.info("updateUserPassword completed");
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<KeycloakUserResponse> updateUserData(@PathVariable String id, @RequestBody UserUpdateDTO dto) {
        logger.info("Calling updateUserData with arguments: id={}, dto={}", id, dto);
        KeycloakUserResponse user = userService.updateUserData(id, dto.username(), dto.email(), dto.firstName(), dto.lastName());
        logger.info("updateUserData returning: {}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> userExistsByUsername(@PathVariable String username) {
        logger.info("Calling userExistsByUsername with argument: {}", username);
        Boolean exists = userService.userExistsByUsername(username);
        logger.info("userExistsByUsername returning: {}", exists);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> userExistsByEmail(@PathVariable String email) {
        logger.info("Calling userExistsByEmail with argument: {}", email);
        Boolean exists = userService.userExistsByEmail(email);
        logger.info("userExistsByEmail returning: {}", exists);
        return ResponseEntity.ok(exists);
    }
}
