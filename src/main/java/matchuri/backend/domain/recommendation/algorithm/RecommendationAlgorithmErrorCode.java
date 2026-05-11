package matchuri.backend.domain.recommendation.algorithm;

import java.text.MessageFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationAlgorithmErrorCode implements ErrorCode {

    NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "추천 알고리즘을 찾을 수 없습니다. type : {0}");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String format(Object... args) {
        return MessageFormat.format(message, args);
    }

    @Override
    public String getDomainPrefix() {
        return "RECOMMENDATION_ALGORITHM_";
    }
}
