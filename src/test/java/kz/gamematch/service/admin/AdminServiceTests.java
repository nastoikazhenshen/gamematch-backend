package kz.gamematch.service.admin;

import kz.gamematch.dto.admin.CreateComplaintDto;
import kz.gamematch.entity.*;
import kz.gamematch.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AdminServiceTests {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private PlayerProfileRepository playerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TeammateRequestRepository teammateRequestRepository;

    @Autowired
    private GameRepository gameRepository;

    private User admin;
    private User player;
    private User otherPlayer;

    @BeforeEach
    void setUp() {
        complaintRepository.deleteAll();
        teammateRequestRepository.deleteAll();
        playerProfileRepository.deleteAll();
        userRepository.deleteAll();

        admin = createUser(RoleName.ADMIN, "admin");
        player = createUser(RoleName.PLAYER, "player");
        otherPlayer = createUser(RoleName.PLAYER, "other");
    }

    @Test
    void adminCanBlockAndUnblockPlayer() {
        var blocked = adminService.blockUser(admin.getEmail(), player.getId());

        assertThat(blocked.blocked()).isTrue();
        assertThat(userRepository.findById(player.getId()).orElseThrow().getIsBlocked()).isTrue();

        var unblocked = adminService.unblockUser(admin.getEmail(), player.getId());

        assertThat(unblocked.blocked()).isFalse();
        assertThat(userRepository.findById(player.getId()).orElseThrow().getIsBlocked()).isFalse();
    }

    @Test
    void playerCannotUseAdminServiceEvenWithValidUserId() {
        assertThatThrownBy(() -> adminService.blockUser(player.getEmail(), otherPlayer.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Admin privileges required");
    }

    @Test
    void adminCannotBlockSelfOrAnotherAdmin() {
        User secondAdmin = createUser(RoleName.ADMIN, "admin2");

        assertThatThrownBy(() -> adminService.blockUser(admin.getEmail(), admin.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Admin cannot block or unblock self");

        assertThatThrownBy(() -> adminService.blockUser(admin.getEmail(), secondAdmin.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Admin accounts cannot be blocked from this panel");
    }

    @Test
    void complaintLifecycleIsRestrictedToAdmins() {
        CreateComplaintDto request = new CreateComplaintDto();
        request.setReporterId(player.getId());
        request.setReportedUserId(otherPlayer.getId());
        request.setReason("Toxic behavior");

        var created = complaintService.createComplaint(player.getEmail(), request);

        assertThat(created.status()).isEqualTo(ComplaintStatus.OPEN);
        assertThat(adminService.getComplaints(admin.getEmail(), ComplaintStatus.OPEN)).hasSize(1);

        assertThatThrownBy(() -> adminService.resolveComplaint(player.getEmail(), created.id()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Admin privileges required");

        var resolved = adminService.resolveComplaint(admin.getEmail(), created.id());

        assertThat(resolved.status()).isEqualTo(ComplaintStatus.RESOLVED);
        assertThat(resolved.resolvedByUserId()).isEqualTo(admin.getId());
    }

    @Test
    void complaintReporterMustMatchAuthenticatedUser() {
        CreateComplaintDto request = new CreateComplaintDto();
        request.setReporterId(player.getId());
        request.setReportedUserId(otherPlayer.getId());
        request.setReason("Bad teammate");

        assertThatThrownBy(() -> complaintService.createComplaint(otherPlayer.getEmail(), request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Reporter must match authenticated user");
    }

    @Test
    void adminCanDeleteOnlyOldInactiveRequests() {
        Game game = gameRepository.findByName("Dota 2").orElseThrow();
        createRequest(player, game, RequestStatus.CANCELLED, LocalDateTime.now().minusDays(10));
        createRequest(player, game, RequestStatus.CLOSED, LocalDateTime.now().minusDays(8));
        createRequest(player, game, RequestStatus.ACTIVE, LocalDateTime.now().minusDays(10));
        createRequest(player, game, RequestStatus.CANCELLED, LocalDateTime.now().minusDays(2));

        int deleted = adminService.deleteInactiveRequests(admin.getEmail(), 7);

        assertThat(deleted).isEqualTo(2);
        assertThat(teammateRequestRepository.findAll()).hasSize(2);
    }

    private User createUser(RoleName roleName, String prefix) {
        Role role = roleRepository.findByName(roleName).orElseThrow();

        User user = new User();
        user.setEmail(prefix + "-" + UUID.randomUUID() + "@example.com");
        user.setPassword("password");
        user.setRole(role);
        user.setIsBlocked(false);
        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);

        PlayerProfile profile = new PlayerProfile();
        profile.setUser(saved);
        profile.setNickname(prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        profile.setKarma(BigDecimal.ZERO);
        profile.setCompletedMatches(0);
        playerProfileRepository.save(profile);

        return saved;
    }

    private void createRequest(User author, Game game, RequestStatus status, LocalDateTime createdAt) {
        TeammateRequest request = new TeammateRequest();
        request.setAuthor(author);
        request.setGame(game);
        request.setTitle("Old request");
        request.setStatus(status);
        request.setCreatedAt(createdAt);
        teammateRequestRepository.save(request);
    }
}
