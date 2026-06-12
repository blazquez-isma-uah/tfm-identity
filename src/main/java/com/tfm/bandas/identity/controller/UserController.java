package com.tfm.bandas.identity.controller;

import com.tfm.bandas.identity.dto.*;
import com.tfm.bandas.identity.service.UserIdentityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/identity/users")
@RequiredArgsConstructor
public class UserController {
    private final UserIdentityService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public ResponseEntity<UserIdentityResponse> createUser(@Valid @RequestBody UserRegisterDTO dto) {
        logger.info("Calling createUser with argument: {}", dto);
        UserIdentityResponse user = userService.createUser(dto);
        logger.info("createUser returning: {}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserIdentityResponse> getUserById(@PathVariable String userId) {
        logger.info("Calling getUserById with argument: {}", userId);
        UserIdentityResponse user = userService.getUserById(userId);
        logger.info("getUserById returning: {}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}/details")
    public ResponseEntity<UserIdentityDetailsResponse> getUserDetailsById(@PathVariable String userId) {
        logger.info("Calling getUserDetailsById with argument: {}", userId);
        UserIdentityDetailsResponse details = userService.getUserDetailsById(userId);
        logger.info("getUserDetailsById returning: {}", details);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserIdentityResponse> getUserByUsername(@PathVariable String username) {
        logger.info("Calling getUserByUsername with argument: {}", username);
        UserIdentityResponse user = userService.getUserByUsername(username);
        logger.info("getUserByUsername returning: {}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}/details")
    public ResponseEntity<UserIdentityDetailsResponse> getUserDetailsByUsername(@PathVariable String username) {
        logger.info("Calling getUserDetailsByUsername with argument: {}", username);
        UserIdentityDetailsResponse details = userService.getUserDetailsByUsername(username);
        logger.info("getUserDetailsByUsername returning: {}", details);
        return ResponseEntity.ok(details);
    }

    @GetMapping
    public ResponseEntity<List<UserIdentityResponse>> listAllUsers() {
        logger.info("Calling listAllUsers");
        List<UserIdentityResponse> users = userService.listAllUsers();
        logger.info("listAllUsers returning: {}", users);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserById(@PathVariable String userId) {
        logger.info("Calling deleteUserById with argument: {}", userId);
        userService.deleteUser(userId);
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

    @PutMapping("/{userId}/password")
    public ResponseEntity<Void> updateUserPassword(@PathVariable String userId, @RequestBody UserPasswordUpdateDTO dto) {
        logger.info("Calling updateUserPassword with arguments: userId={}", userId);
        userService.updateUserPassword(userId, dto.newPassword());
        logger.info("updateUserPassword completed");
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserIdentityResponse> updateUserData(@PathVariable String userId, @RequestBody UserUpdateDTO dto) {
        logger.info("Calling updateUserData with arguments: userId={}, dto={}", userId, dto);
        UserIdentityResponse user = userService.updateUserData(userId, dto.email(), dto.firstName(), dto.lastName());
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
