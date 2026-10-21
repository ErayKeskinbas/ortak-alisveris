package com.eray.ortakalisveris;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
        @Valid @RequestBody RegisterRequest request
    ) {
        AppUser user = registrationService.register(
            request.displayName(),
            request.email(),
            request.password()
        );

        return new UserResponse(
            user.getId(),
            user.getDisplayName(),
            user.getEmail()
        );
    }

    public record RegisterRequest(
        @NotBlank @Size(max = 80) String displayName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 12, max = 72) String password
    ) {}

    public record UserResponse(
        Long id,
        String displayName,
        String email
    ) {}
}