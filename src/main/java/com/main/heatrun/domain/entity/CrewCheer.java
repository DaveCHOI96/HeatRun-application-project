package com.main.heatrun.domain.entity;

import com.main.heatrun.global.enums.CheerType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "crew_cheers",
indexes = {
        // 수진자 기준 최근 응원 조회
        @Index(name = "idx_crew_cheers_receiver",
        columnList = "receiver_id, sent_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrewCheer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    private Crew crew;

    // 응원 유형 - PUSH(푸시 알림) / SOUND(사운드)
    @Enumerated(EnumType.STRING)
    @Column(name = "cheer_type", nullable = false, length = 20)
    private CheerType cheerType;

    // 응원 발송 시간
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    public static CrewCheer send(User sender, User receiver,
                                 Crew crew, CheerType cheerType) {
        CrewCheer cheer = new CrewCheer();
        cheer.sender = sender;
        cheer.receiver = receiver;
        cheer.crew = crew;
        cheer.cheerType = cheerType;
        cheer.sentAt = LocalDateTime.now();
        return cheer;
    }
}
