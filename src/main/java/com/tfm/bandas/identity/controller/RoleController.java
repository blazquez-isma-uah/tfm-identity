package com.tfm.bandas.identity.controller;

import com.tfm.bandas.identity.dto.RoleIdentityResponse;
import com.tfm.bandas.identity.dto.RoleRegisterDTO;
import com.tfm.bandas.identity.service.RoleIdentityService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/identity/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleIdentityService roleService;
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @GetMapping
    public ResponseEntity<List<RoleIdentityResponse>> listAllRoles() {
        logger.info("Calling listAllRoles");
        List<RoleIdentityResponse> roles = roleService.listAllRoles();
        logger.info("listAllRoles returning: {}", roles);
        return ResponseEntity.ok(roles);
    }

    @PostMapping
    public ResponseEntity<RoleIdentityResponse> createRealmRole(@RequestBody RoleRegisterDTO dto) {
        logger.info("Calling createRealmRole with argument: {}", dto);
        RoleIdentityResponse role = roleService.createRealmRole(dto);
        logger.info("createRealmRole returning: {}", role);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<RoleIdentityResponse> getRoleById(@PathVariable String roleId) {
        logger.info("Calling getRoleById with argument: {}", roleId);
        RoleIdentityResponse role = roleService.getRoleById(roleId);
        logger.info("getRoleById returning: {}", role);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/name/{roleName}")
    public ResponseEntity<RoleIdentityResponse> getRoleByName(@PathVariable String roleName) {
        logger.info("Calling getRoleByName with argument: {}", roleName);
        RoleIdentityResponse role = roleService.getRoleByName(roleName);
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
    public ResponseEntity<List<RoleIdentityResponse>> listUserRoles(@PathVariable String userId) {
        logger.info("Calling listUserRoles with argument: {}", userId);
        List<RoleIdentityResponse> roles = roleService.listUserRoles(userId);
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