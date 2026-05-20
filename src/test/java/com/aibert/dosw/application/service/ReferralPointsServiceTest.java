package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.response.ReferralPointsResponseDTO;
import com.aibert.dosw.domain.model.user.ReferralPoints;
import com.aibert.dosw.domain.ports.out.ReferralPointsRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferralPointsServiceTest {

    @Mock private ReferralPointsRepositoryPort pointsRepository;
    @InjectMocks private ReferralPointsService referralPointsService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(referralPointsService, "pointsPerReferral", 50);
        ReflectionTestUtils.setField(referralPointsService, "weeklyLimit", 500);
    }

    @Test
    void awardPoints_newUser_awardsFullPoints() {
        when(pointsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(pointsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReferralPointsResponseDTO result = referralPointsService.awardPoints(userId);

        assertEquals(50, result.getPointsAwarded());
        assertEquals(50, result.getTotalPoints());
        assertEquals(50, result.getWeeklyPoints());
        assertFalse(result.isWeeklyLimitReached());
        verify(pointsRepository).save(any());
    }

    @Test
    void awardPoints_currentWeekPoints_accumulates() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        ReferralPoints existing = ReferralPoints.builder()
                .id(UUID.randomUUID()).userId(userId)
                .totalPoints(100).weeklyPoints(100).weekStart(monday).build();

        when(pointsRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(pointsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReferralPointsResponseDTO result = referralPointsService.awardPoints(userId);

        assertEquals(50, result.getPointsAwarded());
        assertEquals(150, result.getTotalPoints());
        assertEquals(150, result.getWeeklyPoints());
    }

    @Test
    void awardPoints_previousWeek_resetsWeeklyAndAwards() {
        LocalDate lastMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
        ReferralPoints existing = ReferralPoints.builder()
                .id(UUID.randomUUID()).userId(userId)
                .totalPoints(500).weeklyPoints(500).weekStart(lastMonday).build();

        when(pointsRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(pointsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReferralPointsResponseDTO result = referralPointsService.awardPoints(userId);

        assertEquals(50, result.getPointsAwarded());
        assertEquals(550, result.getTotalPoints());
        assertEquals(50, result.getWeeklyPoints());
        assertFalse(result.isWeeklyLimitReached());
    }

    @Test
    void awardPoints_weeklyLimitReached_awardsNothing() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        ReferralPoints existing = ReferralPoints.builder()
                .id(UUID.randomUUID()).userId(userId)
                .totalPoints(1000).weeklyPoints(500).weekStart(monday).build();

        when(pointsRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        ReferralPointsResponseDTO result = referralPointsService.awardPoints(userId);

        assertEquals(0, result.getPointsAwarded());
        assertEquals(1000, result.getTotalPoints());
        assertTrue(result.isWeeklyLimitReached());
        verify(pointsRepository, never()).save(any());
    }

    @Test
    void awardPoints_partialLimit_awardsRemaining() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        ReferralPoints existing = ReferralPoints.builder()
                .id(UUID.randomUUID()).userId(userId)
                .totalPoints(800).weeklyPoints(470).weekStart(monday).build();

        when(pointsRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(pointsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReferralPointsResponseDTO result = referralPointsService.awardPoints(userId);

        assertEquals(30, result.getPointsAwarded());
        assertEquals(830, result.getTotalPoints());
        assertEquals(500, result.getWeeklyPoints());
        assertTrue(result.isWeeklyLimitReached());
    }

    @Test
    void getPoints_noPointsYet_returnsZeros() {
        when(pointsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ReferralPointsResponseDTO result = referralPointsService.getPoints(userId);

        assertEquals(0, result.getTotalPoints());
        assertEquals(0, result.getWeeklyPoints());
        assertEquals(500, result.getWeeklyLimit());
        assertEquals(0, result.getPointsAwarded());
    }
}
