package microservices.book.gamification.gamification.events;

import lombok.extern.slf4j.Slf4j;
import microservices.book.gamification.gamification.service.GameService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventHandler {

    private GameService gameService;

    EventHandler(final GameService gameService) {
        this.gameService = gameService;
    }

    @RabbitListener(queues = "${multiplication.queue}")
    void handleMultiplicationSolved(final MultiplicationSolvedEvent event) {
        log.info("Multiplication Solved Event received: {}", event.getMultiplicationResultAttemptId());
        try {
            gameService.newAttemptForUser(event.getUserId(), event.getMultiplicationResultAttemptId(), event.isCorrect());
        } catch (final Exception e) {
            log.error("Error when trying to process MultiplicationSolvedEvent", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
