package kz.gamematch.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.gamematch.entity.Role;
import kz.gamematch.entity.RoleName;
import kz.gamematch.entity.User;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.RoleRepository;
import kz.gamematch.repository.UserRepository;
import kz.gamematch.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityIntegrationTests.TestEndpoints.class)
class SecurityIntegrationTests {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    SecurityIntegrationTests(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PlayerProfileRepository playerProfileRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @BeforeEach
    void cleanUsers() {
        playerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void authEndpointsArePublicAndReturnJwt() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String email = "player-" + suffix + "@example.com";
        String password = "secret123";

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password,
                                "nickname", "nick" + suffix.substring(0, 8)
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(tokenFrom(registerResponse)).isNotBlank();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(tokenFrom(loginResponse)).isNotBlank();
    }

    @Test
    void protectedPlayerZoneRequiresJwt() throws Exception {
        mockMvc.perform(get("/api/player/ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwtCannotAccessProtectedZone() throws Exception {
        mockMvc.perform(get("/api/player/ping")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void playerCanAccessPlayerZones() throws Exception {
        String token = tokenFor(createUser(RoleName.PLAYER, false));

        mockMvc.perform(get("/api/player/ping").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/profiles/ping").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanAccessPlayerAndAdminZones() throws Exception {
        String token = tokenFor(createUser(RoleName.ADMIN, false));

        mockMvc.perform(get("/api/player/ping").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/ping").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void playerCannotAccessAdminZones() throws Exception {
        String token = tokenFor(createUser(RoleName.PLAYER, false));

        mockMvc.perform(get("/api/admin/ping").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/requests/inactive").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanDeleteInactiveRequestsEndpoint() throws Exception {
        String token = tokenFor(createUser(RoleName.ADMIN, false));

        mockMvc.perform(delete("/api/requests/inactive").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void blockedUserTokenIsRejected() throws Exception {
        String token = tokenFor(createUser(RoleName.PLAYER, true));

        mockMvc.perform(get("/api/player/ping").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blockedUserCannotLogin() throws Exception {
        String email = createUser(RoleName.PLAYER, true).getEmail();

        assertThatThrownBy(() -> mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "email", email,
                        "password", "secret123"
                )))))
                .hasRootCauseMessage("User is blocked");
    }

    private User createUser(RoleName roleName, boolean blocked) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow();

        User user = new User();
        user.setEmail(roleName.name().toLowerCase() + "-" + UUID.randomUUID() + "@example.com");
        user.setPassword(passwordEncoder.encode("secret123"));
        user.setRole(role);
        user.setIsBlocked(blocked);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    private String tokenFor(User user) {
        return jwtService.generateToken(user);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String tokenFrom(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        return root.get("token").asText();
    }

    @TestConfiguration
    static class TestEndpoints {

        @RestController
        static class TestController {

            @GetMapping("/api/player/ping")
            String playerPing() {
                return "player";
            }

            @GetMapping("/api/profiles/ping")
            String profilesPing() {
                return "profiles";
            }

            @GetMapping("/api/admin/ping")
            String adminPing() {
                return "admin";
            }

            @DeleteMapping("/api/requests/inactive")
            String deleteInactiveRequests() {
                return "deleted";
            }
        }
    }
}
