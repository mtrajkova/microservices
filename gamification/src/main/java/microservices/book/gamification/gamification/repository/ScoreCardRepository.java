package microservices.book.gamification.gamification.repository;

import microservices.book.gamification.gamification.domain.LeaderBoardRow;
import microservices.book.gamification.gamification.domain.ScoreCard;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScoreCardRepository extends CrudRepository<ScoreCard, Long> {
    @Query("SELECT SUM (s.score) FROM microservices.book.gamification.gamification.domain.ScoreCard s WHERE s.userId = : userId GROUP BY s.userId")
    int getTotalScoreForUser(@Param("userId") final Long userId);

    @Query("SELECT NEW microservices.book.gamification.gamification.domain.LeaderBoardRow(s.userId, SUM(s.score)) " +
            "FROM microservices.book.gamification.gamification.domain.ScoreCard s GROUP BY s.userId ORDER BY SUM(s.score) DESC")
    List<LeaderBoardRow> findFirst10();

    List<ScoreCard> findByUserIdOrOrderByScoreTimestamp(final Long userId);

}
