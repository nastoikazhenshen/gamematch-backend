package kz.gamematch.security;

import kz.gamematch.entity.RoleName;
import kz.gamematch.entity.User;
import kz.gamematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Long userId(UserDetails userDetails) {
        return currentUser(userDetails).getId();
    }

    @Transactional(readOnly = true)
    public void requireSelfOrAdmin(UserDetails userDetails, Long requestedUserId) {
        User user = currentUser(userDetails);
        if (!user.getId().equals(requestedUserId) && user.getRole().getName() != RoleName.ADMIN) {
            throw new AccessDeniedException("Access denied");
        }
    }

    private User currentUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Authentication required");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        if (Boolean.TRUE.equals(user.getIsBlocked())) {
            throw new RuntimeException("User is blocked");
        }

        return user;
    }
}
