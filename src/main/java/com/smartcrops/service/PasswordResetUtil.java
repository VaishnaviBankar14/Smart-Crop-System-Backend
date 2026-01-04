package com.smartcrops.service;

import java.util.UUID;

public class PasswordResetUtil {

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
