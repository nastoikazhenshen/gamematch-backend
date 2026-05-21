package kz.gamematch.controller;

import jakarta.validation.Valid;
import kz.gamematch.dto.auth.AuthResponseDto;
import kz.gamematch.dto.auth.LoginRequestDto;
import kz.gamematch.dto.auth.RegisterRequestDto;
import kz.gamematch.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponseDto register(
            @Valid @RequestBody RegisterRequestDto request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDto login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        return authService.login(request);
    }
}
