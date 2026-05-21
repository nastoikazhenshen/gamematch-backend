package kz.gamematch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "request_responses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"request_id", "responder_id"})
)
@Getter
@Setter
public class RequestResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private TeammateRequest request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responder_id", nullable = false)
    private User responder;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResponseStatus status = ResponseStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
