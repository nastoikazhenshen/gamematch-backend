package kz.gamematch.security.jwt;

import kz.gamematch.entity.Role;
import kz.gamematch.entity.RoleName;
import kz.gamematch.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTests {

    private final JwtService jwtService = new JwtService("test-secret-key-that-is-at-least-32-bytes");

    @Test
    void generatedTokenContainsUserIdentityAndRole() {
        User user = user(RoleName.ADMIN);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("admin@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void tokenIsValidForMatchingUserDetails() {
        User user = user(RoleName.PLAYER);
        String token = jwtService.generateToken(user);
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("player@example.com")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PLAYER")))
                .build();

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void tokenIsInvalidForDifferentUserDetails() {
        User user = user(RoleName.PLAYER);
        String token = jwtService.generateToken(user);
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("other@example.com")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PLAYER")))
                .build();

        assertThat(jwtService.isTokenValid(token, userDetails)).isFalse();
    }

    private User user(RoleName roleName) {
        Role role = new Role();
        role.setId(roleName == RoleName.ADMIN ? 2L : 1L);
        role.setName(roleName);

        User user = new User();
        user.setId(42L);
        user.setEmail(roleName.name().toLowerCase() + "@example.com");
        user.setPassword("password");
        user.setRole(role);
        user.setIsBlocked(false);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }
}
