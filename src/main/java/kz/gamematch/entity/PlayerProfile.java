package kz.gamematch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "player_profiles")
@Getter
@Setter
public class PlayerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 80)
    private String nickname;

    @Column(length = 50)
    private String timezone;

    @Column(name = "average_play_time", length = 100)
    private String averagePlayTime;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal karma = BigDecimal.ZERO;

    @Column(name = "completed_matches", nullable = false)
    private Integer completedMatches = 0;
}
