package kz.gamematch.service.auth;

import kz.gamematch.dto.auth.AuthResponseDto;
import kz.gamematch.dto.auth.LoginRequestDto;
import kz.gamematch.dto.auth.RegisterRequestDto;
import kz.gamematch.entity.*;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.RoleRepository;
import kz.gamematch.repository.UserRepository;
import kz.gamematch.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (playerProfileRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("Nickname already exists");
        }

        Role role = roleRepository.findByName(RoleName.PLAYER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setIsBlocked(false);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        PlayerProfile profile = new PlayerProfile();
        profile.setUser(savedUser);
        profile.setNickname(request.getNickname());
        profile.setKarma(BigDecimal.ZERO);
        profile.setCompletedMatches(0);

        playerProfileRepository.save(profile);

        String token = jwtService.generateToken(savedUser);

        return new AuthResponseDto(token);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (Boolean.TRUE.equals(user.getIsBlocked())) {
            throw new RuntimeException("User is blocked");
        }

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponseDto(token);
    }
}
