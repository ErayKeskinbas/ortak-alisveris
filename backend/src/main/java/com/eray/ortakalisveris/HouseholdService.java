package com.eray.ortakalisveris;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class HouseholdService {

    private final AppUserRepository users;
    private final HouseholdRepository households;

    public HouseholdService(
        AppUserRepository users,
        HouseholdRepository households
    ) {
        this.users = users;
        this.households = households;
    }

    @Transactional(readOnly = true)
    public HouseholdResponse getCurrent(String email) {
        AppUser user = users.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Kullanıcı bulunamadı"
            ));

        return toResponse(user.getHousehold());
    }

    public HouseholdResponse create(String email, String name) {
        AppUser user = findUserForUpdate(email);
        requireNoHousehold(user);

        Household household = households.save(
            new Household(name.trim())
        );

        user.setHousehold(household);
        users.save(user);

        return toResponse(household);
    }

    public HouseholdResponse join(String email, String inviteCode) {
        AppUser user = findUserForUpdate(email);
        requireNoHousehold(user);

        String normalizedCode =
            inviteCode.trim().toLowerCase(Locale.ROOT);

        Household household = households.findByInviteCode(normalizedCode)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Davet koduna ait ev bulunamadı"
            ));

        user.setHousehold(household);
        users.save(user);

        return toResponse(household);
    }

    private AppUser findUserForUpdate(String email) {
        return users.findByEmailForUpdate(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Kullanıcı bulunamadı"
            ));
    }

    private void requireNoHousehold(AppUser user) {
        if (user.getHousehold() != null) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Zaten bir eve üyesin"
            );
        }
    }

    private HouseholdResponse toResponse(Household household) {
        if (household == null) {
            return new HouseholdResponse(false, null, null, null);
        }

        return new HouseholdResponse(
            true,
            household.getId(),
            household.getName(),
            household.getInviteCode()
        );
    }

    public record HouseholdResponse(
        boolean member,
        Long id,
        String name,
        String inviteCode
    ) {}
}