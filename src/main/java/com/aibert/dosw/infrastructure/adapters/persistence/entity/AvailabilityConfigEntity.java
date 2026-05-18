package com.aibert.dosw.infrastructure.adapters.persistence.entity;

import com.aibert.dosw.domain.model.user.VisibilityLevel;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "availability_configs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AvailabilityConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisibilityLevel visibility;

    @ElementCollection
    @CollectionTable(name = "authorized_friends", joinColumns = @JoinColumn(name = "config_id"))
    @Column(name = "friend_id")
    private List<UUID> authorizedFriends;
}
