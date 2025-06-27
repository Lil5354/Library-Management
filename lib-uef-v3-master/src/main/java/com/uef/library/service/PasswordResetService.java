package com.uef.library.service;

import com.uef.library.model.User;

public interface PasswordResetService {

    void createAndSendPasswordResetToken(User user);

    String validatePasswordResetToken(String token);

    User getUserByPasswordResetToken(String token);

    void changeUserPassword(User user, String newPassword);

    void deleteToken(String token);
}