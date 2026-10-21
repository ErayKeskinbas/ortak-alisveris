package com.eray.ortakalisveris;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationService {

    private final AppUserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder =
        new BCryptPasswordEncoder();

    public RegistrationService(AppUserRepository repository) {
        this.repository = repository;
    }

    public AppUser register(
        String displayName,
        String email,
        String password
    ) {
        String normalizedEmail =
            email.trim().toLowerCase(Locale.ROOT);

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Şifre UTF-8 biçiminde 72 baytı aşamaz"
            );
        }

        if (repository.existsByEmail(normalizedEmail)) {
            throw emailAlreadyUsed();
        }

        AppUser user = new AppUser(
            displayName.trim(),
            normalizedEmail,
            passwordEncoder.encode(password)
        );

        try {
            return repository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Kullanıcı kaydedilemedi; e-posta kullanımda olabilir"
            );
        }
    }

    private ResponseStatusException emailAlreadyUsed() {
        return new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Bu e-posta adresi zaten kullanılıyor"
        );
    }
}