package app.omniOne.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@Table(name = "coaching", indexes = {
        @Index(name = "idx_coaching_coach_client_end", columnList = "coach_id,client_id,end_date"),
        @Index(name = "idx_coaching_client_end", columnList = "client_id,end_date")
})
public class Coaching {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @CreationTimestamp
    @Column(nullable = false, columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime startDate;

    @Column(columnDefinition = "TIMESTAMP(0)")
    private LocalDateTime endDate;

}
