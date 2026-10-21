package com.eray.ortakalisveris;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RegistrationServiceTest {

    @Autowired
    private RegistrationService service;

    @Test
    void storesPasswordAsBcryptHash() {
        String password = "Test-Sifresi-2026!";

        AppUser user = service.register(
            "Eray",
            "eray@example.com",
            password
        );

        assertNotNull(user.getId());
        assertNotEquals(password, user.getPasswordHash());

        assertTrue(
            new BCryptPasswordEncoder()
                .matches(password, user.getPasswordHash())
        );
    }

    @Test
    void normalizesEmailAddress() {
        AppUser user = service.register(
            "Eray",
            "  ERAY@Example.COM  ",
            "Test-Sifresi-2026!"
        );

        assertEquals("eray@example.com", user.getEmail());
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        service.register(
            "Birinci",
            "eray@example.com",
            "Test-Sifresi-2026!"
        );

        var exception = assertThrows(
            ResponseStatusException.class,
            () -> service.register(
                "İkinci",
                "ERAY@example.com",
                "Baska-Sifre-2026!"
            )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void rejectsPasswordExceedingBcryptByteLimit() {
        // Her "ş" karakteri UTF-8 biçiminde 2 bayttır.
        String password = "ş".repeat(37);

        var exception = assertThrows(
            ResponseStatusException.class,
            () -> service.register(
                "Eray",
                "eray@example.com",
                password
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}