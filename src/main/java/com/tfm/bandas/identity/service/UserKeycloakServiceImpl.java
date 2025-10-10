package com.tfm.bandas.identity.service;

import com.tfm.bandas.identity.client.UserKeycloakApiClient;
import com.tfm.bandas.identity.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserKeycloakServiceImpl implements UserKeycloakService {
    private final UserKeycloakApiClient kc;

    @Override
    public KeycloakUserResponse createUser(UserRegisterDTO req) {
        String token = kc.getAdminToken();
        String userId = kc.createUser(token, req);
        return new KeycloakUserResponse(userId, req.username());
    }

    @Override
    public void deleteUser(String userId) {
        String token = kc.getAdminToken();
        kc.deleteUser(token, userId);
    }

    @Override
    public void deleteUserByUsername(String username) {
        String token = kc.getAdminToken();
        kc.deleteUserByUsername(token, username);
    }

    @Override
    public void updateUserPassword(String userId, String newPassword) {
        String token = kc.getAdminToken();
        kc.updateUserPassword(token, userId, newPassword);
    }

    @Override
    public void updateUserData(String userId, String username, String email, String firstName, String lastName) {
        String token = kc.getAdminToken();
        kc.updateUserData(token, userId, username, email, firstName, lastName);
    }

    @Override
    public boolean userExistsByUsername(String username) {
        String token = kc.getAdminToken();
        return kc.userExistsByUsername(token, username);
    }

    @Override
    public boolean userExistsByEmail(String email) {
        String token = kc.getAdminToken();
        return kc.userExistsByEmail(token, email);
    }

    @Override
    public KeycloakUserResponse getUserByUsername(String username) {
        String token = kc.getAdminToken();
        var user = kc.getUserByUsername(token, username);
        if (user == null) return null;
        return new KeycloakUserResponse((String) user.get("id"), (String) user.get("username"));
    }

    @Override
    public KeycloakUserResponse getUserById(String userId) {
        String token = kc.getAdminToken();
        var user = kc.getUserById(token, userId);
        if (user == null) return null;
        return new KeycloakUserResponse((String) user.get("id"), (String) user.get("username"));
    }

    @Override
    public KeycloakUserDetailsResponse getUserDetailsById(String userId) {
        String token = kc.getAdminToken();
        return kc.getUserDetailsById(token, userId);
    }

    @Override
    public KeycloakUserDetailsResponse getUserDetailsByUsername(String username) {
        String token = kc.getAdminToken();
        return kc.getUserDetailsByUsername(token, username);
    }

    @Override
    public List<KeycloakUserResponse> listAllUsers() {
        String token = kc.getAdminToken();
        return kc.listAllUsers(token);
    }
}
