package microservices.book.socialmultiplication.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Result {
    private final boolean correct;

    public Result() {
        this(false);
    }
}
