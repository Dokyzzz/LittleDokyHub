package com.littledokyhub;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LegacyAwarePasswordEncoder implements PasswordEncoder {
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (BCRYPT_PATTERN.matcher(storedPassword).matches()) {
            return bcrypt.matches(rawPassword, storedPassword);
        }
        if (storedPassword.startsWith("$2")) {
            return false;
        }
        return MessageDigest.isEqual(rawPassword.toString().getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean upgradeEncoding(String storedPassword) {
        return storedPassword == null || !BCRYPT_PATTERN.matcher(storedPassword).matches()
                || bcrypt.upgradeEncoding(storedPassword);
    }
}