package com.eray.ortakalisveris;

import java.security.Principal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/household")
public class HouseholdController {

    private final HouseholdService service;

    public HouseholdController(HouseholdService service) {
        this.service = service;
    }

    @GetMapping
    public HouseholdService.HouseholdResponse getCurrent(
        Principal principal
    ) {
        return service.getCurrent(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HouseholdService.HouseholdResponse create(
        Principal principal,
        @Valid @RequestBody CreateHouseholdRequest request
    ) {
        return service.create(
            principal.getName(),
            request.name()
        );
    }

    @PostMapping("/join")
    public HouseholdService.HouseholdResponse join(
        Principal principal,
        @Valid @RequestBody JoinHouseholdRequest request
    ) {
        return service.join(
            principal.getName(),
            request.inviteCode()
        );
    }

    public record CreateHouseholdRequest(
        @NotBlank @Size(max = 80) String name
    ) {}

    public record JoinHouseholdRequest(
        @NotBlank
        @Pattern(regexp = "[a-fA-F0-9]{32}")
        String inviteCode
    ) {}
}
