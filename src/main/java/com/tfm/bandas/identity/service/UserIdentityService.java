package com.tfm.bandas.identity.service;

import com.tfm.bandas.identity.dto.*;
import java.util.List;

public interface UserIdentityService {
    UserIdentityResponse createUser(UserRegisterDTO req);
    void deleteUser(String userId);
    void deleteUserByUsername(String username);
    void updateUserPassword(String userId, String newPassword);
    UserIdentityResponse updateUserData(String userId, String email, String firstName, String lastName);
    boolean userExistsByUsername(String username);
    boolean userExistsByEmail(String email);
    UserIdentityResponse getUserByUsername(String username);
    UserIdentityResponse getUserById(String userId);
    UserIdentityDetailsResponse getUserDetailsById(String userId);
    UserIdentityDetailsResponse getUserDetailsByUsername(String username);
    List<UserIdentityResponse> listAllUsers();
}
