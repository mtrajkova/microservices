package microservices.book.gamification.gamification.service;

import microservices.book.gamification.gamification.domain.Badge;
import microservices.book.gamification.gamification.domain.GameStats;
import microservices.book.gamification.gamification.domain.ScoreCard;
import microservices.book.gamification.gamification.repository.BadgeCardRepository;
import microservices.book.gamification.gamification.repository.ScoreCardRepository;
import microservices.book.gamification.gamification.service.impl.GameServiceImpl;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

public class GameServiceImplTest {

    @Mock
    private ScoreCardRepository scoreCardRepository;

    @Mock
    private BadgeCardRepository badgeCardRepository;

    private GameServiceImpl gameService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        gameService = new GameServiceImpl(scoreCardRepository, badgeCardRepository);
    }

    @Test
    public void firstCorrectAttemptTest() {
        Long userId = 1L;
        Long attemptId = 1L;
        int totalScore = 10;

        ScoreCard scoreCard = new ScoreCard(userId, attemptId);
        given(scoreCardRepository.getTotalScoreForUser(userId)).willReturn(totalScore);
        given(scoreCardRepository.findByUserIdOrOrderByScoreTimestamp(userId)).willReturn(Collections.singletonList(scoreCard));
        given(badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId)).willReturn(Collections.emptyList());

        GameStats gameStatsResult = gameService.newAttemptForUser(1L, 1L, true);

        assertThat(gameStatsResult.getScore()).isEqualTo(scoreCard.getScore());
        assertThat(gameStatsResult.getBadges()).containsOnly(Badge.FIRST_WON);
    }

    @Test
    public void wrongAttemptTest() {
        Long userId = 1L;
        Long attemptId = 1L;
        int totalScore = 0;

        ScoreCard scoreCard = new ScoreCard(userId, attemptId);
        given(scoreCardRepository.getTotalScoreForUser(userId)).willReturn(totalScore);
        given(scoreCardRepository.findByUserIdOrOrderByScoreTimestamp(userId)).willReturn(Collections.singletonList(scoreCard));
        given(badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId)).willReturn(Collections.emptyList());

        GameStats gameStatsResult = gameService.newAttemptForUser(1L, 1L, false);

        assertThat(gameStatsResult.getScore()).isEqualTo(scoreCard.getScore());
        assertThat(gameStatsResult.getBadges()).isEmpty();
    }
}
