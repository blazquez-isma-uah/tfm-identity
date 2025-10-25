package com.tfm.bandas.identity.service;

import com.tfm.bandas.identity.dto.*;
import java.util.List;

public interface UserKeycloakService {
    KeycloakUserResponse createUser(UserRegisterDTO req);
    void deleteUser(String userId);
    void deleteUserByUsername(String username);
    void updateUserPassword(String userId, String newPassword);
    KeycloakUserResponse updateUserData(String userId, String username, String email, String firstName, String lastName);
    boolean userExistsByUsername(String username);
    boolean userExistsByEmail(String email);
    KeycloakUserResponse getUserByUsername(String username);
    KeycloakUserResponse getUserById(String userId);
    KeycloakUserDetailsResponse getUserDetailsById(String userId);
    KeycloakUserDetailsResponse getUserDetailsByUsername(String username);
    List<KeycloakUserResponse> listAllUsers();
}
