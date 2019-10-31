package microservices.book.gamification.gamification.service;

import microservices.book.gamification.gamification.domain.LeaderBoardRow;

import java.util.List;

public interface LeaderBoardService {
    List<LeaderBoardRow> getCurrentLeaderBoard();
}
