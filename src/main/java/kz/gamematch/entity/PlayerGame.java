package kz.gamematch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "player_games",
        uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "game_id"})
)
@Getter
@Setter
public class PlayerGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private PlayerProfile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(length = 80)
    private String rank;

    @Column(name = "main_role", length = 80)
    private String mainRole;
}