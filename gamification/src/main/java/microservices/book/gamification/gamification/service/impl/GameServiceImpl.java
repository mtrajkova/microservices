package microservices.book.gamification.gamification.service.impl;

import lombok.extern.slf4j.Slf4j;
import microservices.book.gamification.gamification.client.MultiplicationResultAttemptClient;
import microservices.book.gamification.gamification.client.dto.MultiplicationResultAttempt;
import microservices.book.gamification.gamification.domain.Badge;
import microservices.book.gamification.gamification.domain.BadgeCard;
import microservices.book.gamification.gamification.domain.GameStats;
import microservices.book.gamification.gamification.domain.ScoreCard;
import microservices.book.gamification.gamification.repository.BadgeCardRepository;
import microservices.book.gamification.gamification.repository.ScoreCardRepository;
import microservices.book.gamification.gamification.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GameServiceImpl implements GameService {

    public static final int LUCKY_NUMBER = 42;

    private ScoreCardRepository scoreCardRepository;
    private BadgeCardRepository badgeCardRepository;
    private MultiplicationResultAttemptClient attemptClient;

    @Autowired
    public GameServiceImpl(ScoreCardRepository scoreCardRepository, BadgeCardRepository badgeCardRepository, MultiplicationResultAttemptClient attemptClient) {
        this.scoreCardRepository = scoreCardRepository;
        this.badgeCardRepository = badgeCardRepository;
        this.attemptClient = attemptClient;
    }

    @Override
    public GameStats newAttemptForUser(Long userId, Long attemptId, boolean correct) {
        if (correct) {
            ScoreCard scoreCard = new ScoreCard(userId, attemptId);
            scoreCardRepository.save(scoreCard);

            log.info("User with id {} scored {} points for attempt id {}", userId, scoreCard.getScore(), attemptId);

            List<BadgeCard> badgeCards = processForBadges(userId, attemptId);

            return new GameStats(userId, scoreCard.getScore(), badgeCards.stream()
                    .map(BadgeCard::getBadge)
                    .collect(Collectors.toList()));
        }

        return GameStats.emptyStats(userId);
    }

    @Override
    public GameStats retrieveStatsForUser(Long userId) {
        int score = scoreCardRepository.getTotalScoreForUser(userId);

        List<BadgeCard> badgeCards = badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId);
        return new GameStats(userId, score, badgeCards.stream().map(BadgeCard::getBadge).collect(Collectors.toList()));
    }

    private List<BadgeCard> processForBadges(Long userId, Long attemptId) {
        List<BadgeCard> badgeCards = new ArrayList<>();

        int totalScore = scoreCardRepository.getTotalScoreForUser(userId);
        log.info("New score for user {} is {}", userId, totalScore);

        List<ScoreCard> scoreCards = scoreCardRepository.findByUserIdOrOrderByScoreTimestamp(userId);
        List<BadgeCard> badgeCardsList = badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId);

        checkAndGiveBadgesBasedOnScore(badgeCardsList, Badge.BRONZE_MULTIPLICATOR, totalScore, 100, userId)
                .ifPresent(badgeCards::add);
        checkAndGiveBadgesBasedOnScore(badgeCardsList, Badge.SILVER_MULTIPLICATOR, totalScore, 500, userId)
                .ifPresent(badgeCards::add);
        checkAndGiveBadgesBasedOnScore(badgeCardsList, Badge.GOLD_MULTIPLICATOR, totalScore, 999, userId)
                .ifPresent(badgeCards::add);

        if (scoreCards.size() == 1 && !containsBadge(badgeCardsList, Badge.FIRST_WON)) {
            BadgeCard firstWonBadge = giveBadgeToUser(Badge.FIRST_WON, userId);
            badgeCards.add(firstWonBadge);
        }

        MultiplicationResultAttempt attempt = attemptClient.retrieveMultiplicatoinResultAttemptById(attemptId);
        if (!containsBadge(badgeCardsList, Badge.LUCKY_NUMBER) &&
                (LUCKY_NUMBER == attempt.getMultiplicationFactorA() || LUCKY_NUMBER == attempt.getMultiplicationFactorB())) {
            BadgeCard luckyNumberBadge = giveBadgeToUser(Badge.LUCKY_NUMBER, userId);
            badgeCards.add(luckyNumberBadge);
        }

        return badgeCards;
    }

    private Optional<BadgeCard> checkAndGiveBadgesBasedOnScore(List<BadgeCard> badgeCardsList, Badge badge, int score, int scoreThreshold, Long userId) {
        if (score >= scoreThreshold && !containsBadge(badgeCardsList, badge)) {
            return Optional.of(giveBadgeToUser(badge, userId));
        }

        return Optional.empty();
    }

    private boolean containsBadge(final List<BadgeCard> badgeCards, final Badge badge) {
        return badgeCards.stream().anyMatch(b -> b.getBadge().equals(badge));
    }

    private BadgeCard giveBadgeToUser(Badge badge, Long userId) {
        BadgeCard badgeCard = new BadgeCard(userId, badge);
        badgeCardRepository.save(badgeCard);

        log.info("User with id {} won a new badge: {}", userId, badge);
        return badgeCard;
    }
}
