package com.eray.ortakalisveris;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HouseholdServiceTest {

    @Autowired
    private HouseholdService service;

    @Autowired
    private AppUserRepository users;

    @BeforeEach
    void prepareUsers() {
        users.save(new AppUser(
            "Ev sahibi",
            "owner@example.com",
            "test-only-placeholder"
        ));

        users.save(new AppUser(
            "Katılan üye",
            "member@example.com",
            "test-only-placeholder"
        ));
    }

    @Test
    void creatingHouseholdMakesCreatorAMember() {
        var created = service.create(
            "owner@example.com",
            "Bizim ev"
        );

        var current = service.getCurrent("owner@example.com");

        assertTrue(current.member());
        assertEquals(created.id(), current.id());
        assertEquals("Bizim ev", current.name());
        assertNotNull(current.inviteCode());
        assertTrue(current.inviteCode().matches("[a-f0-9]{32}"));
    }

    @Test
    void invitationAddsUserToSameHousehold() {
        var created = service.create(
            "owner@example.com",
            "Bizim ev"
        );

        var joined = service.join(
            "member@example.com",
            created.inviteCode()
        );

        var current = service.getCurrent("member@example.com");

        assertTrue(current.member());
        assertEquals(created.id(), joined.id());
        assertEquals(created.id(), current.id());
    }

    @Test
    void rejectsUnknownInvitation() {
        var exception = assertThrows(
            ResponseStatusException.class,
            () -> service.join(
                "member@example.com",
                "00000000000000000000000000000000"
            )
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void existingMemberCannotJoinAnotherHousehold() {
        service.create("owner@example.com", "Birinci ev");

        var secondHome = service.create(
            "member@example.com",
            "İkinci ev"
        );

        var exception = assertThrows(
            ResponseStatusException.class,
            () -> service.join(
                "owner@example.com",
                secondHome.inviteCode()
            )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void existingMemberCannotCreateAnotherHousehold() {
        service.create("owner@example.com", "Birinci ev");

        var exception = assertThrows(
            ResponseStatusException.class,
            () -> service.create(
                "owner@example.com",
                "İkinci ev"
            )
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
}
