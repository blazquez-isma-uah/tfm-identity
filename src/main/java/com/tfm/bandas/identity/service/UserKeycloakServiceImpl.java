package com.tfm.bandas.identity.service;

import com.tfm.bandas.identity.client.UserKeycloakApiClient;
import com.tfm.bandas.identity.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserKeycloakServiceImpl implements UserKeycloakService {
    private final UserKeycloakApiClient keycloakApiClient;

    @Override
    public KeycloakUserResponse createUser(UserRegisterDTO req) {
        String token = keycloakApiClient.getAdminToken();
        String userId = keycloakApiClient.createUser(token, req);
        return new KeycloakUserResponse(userId, req.username());
    }

    @Override
    public void deleteUser(String userId) {
        String token = keycloakApiClient.getAdminToken();
        keycloakApiClient.deleteUser(token, userId);
    }

    @Override
    public void deleteUserByUsername(String username) {
        String token = keycloakApiClient.getAdminToken();
        keycloakApiClient.deleteUserByUsername(token, username);
    }

    @Override
    public void updateUserPassword(String userId, String newPassword) {
        String token = keycloakApiClient.getAdminToken();
        keycloakApiClient.updateUserPassword(token, userId, newPassword);
    }

    @Override
    public KeycloakUserResponse updateUserData(String userId, String username, String email, String firstName, String lastName) {
        String token = keycloakApiClient.getAdminToken();
        keycloakApiClient.updateUserData(token, userId, username, email, firstName, lastName);
        return new KeycloakUserResponse(userId, username);
    }

    @Override
    public boolean userExistsByUsername(String username) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.userExistsByUsername(token, username);
    }

    @Override
    public boolean userExistsByEmail(String email) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.userExistsByEmail(token, email);
    }

    @Override
    public KeycloakUserResponse getUserByUsername(String username) {
        String token = keycloakApiClient.getAdminToken();
        var user = keycloakApiClient.getUserByUsername(token, username);
        if (user == null) return null;
        return new KeycloakUserResponse((String) user.get("id"), (String) user.get("username"));
    }

    @Override
    public KeycloakUserResponse getUserById(String userId) {
        String token = keycloakApiClient.getAdminToken();
        var user = keycloakApiClient.getUserById(token, userId);
        if (user == null) return null;
        return new KeycloakUserResponse((String) user.get("id"), (String) user.get("username"));
    }

    @Override
    public KeycloakUserDetailsResponse getUserDetailsById(String userId) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.getUserDetailsById(token, userId);
    }

    @Override
    public KeycloakUserDetailsResponse getUserDetailsByUsername(String username) {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.getUserDetailsByUsername(token, username);
    }

    @Override
    public List<KeycloakUserResponse> listAllUsers() {
        String token = keycloakApiClient.getAdminToken();
        return keycloakApiClient.listAllUsers(token);
    }
}
