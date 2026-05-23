package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.ReferralPointsResponseDTO;
import com.aibert.dosw.domain.model.user.ReferralPoints;
import com.aibert.dosw.domain.ports.in.ReferralPointsUseCase;
import com.aibert.dosw.domain.ports.out.ReferralPointsRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralPointsService implements ReferralPointsUseCase {

    private final ReferralPointsRepositoryPort pointsRepository;

    @Value("${app.referral.points-per-referral:50}")
    private int pointsPerReferral;

    @Value("${app.referral.weekly-limit:500}")
    private int weeklyLimit;

    @Override
    public ReferralPointsResponseDTO awardPoints(UUID inviterId) {
        log.info("Awarding referral points to inviterId={}", inviterId);
        LocalDate currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);

        ReferralPoints points = pointsRepository.findByUserId(inviterId)
                .map(p -> p.getWeekStart().isBefore(currentWeekStart)
                        ? p.toBuilder().weeklyPoints(0).weekStart(currentWeekStart).build()
                        : p)
                .orElse(ReferralPoints.builder()
                        .userId(inviterId)
                        .totalPoints(0).weeklyPoints(0)
                        .weekStart(currentWeekStart).build());

        int awardable = Math.min(pointsPerReferral, weeklyLimit - points.getWeeklyPoints());
        boolean awarded = awardable > 0;

        if (awarded) {
            points = pointsRepository.save(points.toBuilder()
                    .totalPoints(points.getTotalPoints() + awardable)
                    .weeklyPoints(points.getWeeklyPoints() + awardable)
                    .build());
            log.debug("Awarded {} points to inviterId={} totalPoints={}", awardable, inviterId, points.getTotalPoints());
        } else {
            log.warn("Weekly limit reached for inviterId={}, no points awarded", inviterId);
        }

        return toDTO(points, awarded ? awardable : 0);
    }

    @Override
    public ReferralPointsResponseDTO getPoints(UUID userId) {
        log.debug("Getting referral points for userId={}", userId);
        LocalDate currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        ReferralPoints points = pointsRepository.findByUserId(userId)
                .map(p -> p.getWeekStart().isBefore(currentWeekStart)
                        ? p.toBuilder().weeklyPoints(0).weekStart(currentWeekStart).build()
                        : p)
                .orElse(ReferralPoints.builder()
                        .userId(userId).totalPoints(0).weeklyPoints(0)
                        .weekStart(currentWeekStart).build());
        return toDTO(points, 0);
    }

    private ReferralPointsResponseDTO toDTO(ReferralPoints p, int pointsAwarded) {
        return ReferralPointsResponseDTO.builder()
                .userId(p.getUserId())
                .totalPoints(p.getTotalPoints())
                .weeklyPoints(p.getWeeklyPoints())
                .weeklyLimit(weeklyLimit)
                .pointsAwarded(pointsAwarded)
                .weeklyLimitReached(p.getWeeklyPoints() >= weeklyLimit)
                .build();
    }
}
