package kz.gamematch.security.user;

import kz.gamematch.entity.Role;
import kz.gamematch.entity.RoleName;
import kz.gamematch.entity.User;
import kz.gamematch.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GamematchUserDetailsServiceTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final GamematchUserDetailsService userDetailsService = new GamematchUserDetailsService(userRepository);

    @Test
    void loadsUserWithRoleAuthority() {
        when(userRepository.findByEmail("player@example.com"))
                .thenReturn(Optional.of(user("player@example.com", RoleName.PLAYER, false)));

        var userDetails = userDetailsService.loadUserByUsername("player@example.com");

        assertThat(userDetails.getUsername()).isEqualTo("player@example.com");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_PLAYER");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void blockedUserIsDisabled() {
        when(userRepository.findByEmail("blocked@example.com"))
                .thenReturn(Optional.of(user("blocked@example.com", RoleName.PLAYER, true)));

        var userDetails = userDetailsService.loadUserByUsername("blocked@example.com");

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void unknownUserThrowsUsernameNotFoundException() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private User user(String email, RoleName roleName, boolean blocked) {
        Role role = new Role();
        role.setName(roleName);

        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(role);
        user.setIsBlocked(blocked);
        user.setCreatedAt(LocalDateTime.now());

        return user;
    }
}
