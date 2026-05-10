package matchuri.backend.domain.recommendation.exception;

import java.text.MessageFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import matchuri.backend.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationErrorCode implements ErrorCode {

    TASTE_PROFILE_REQUIRED(HttpStatus.FORBIDDEN, "개인 추천을 만들려면 취향 프로필이 필요합니다. memberId : {0}"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "해당 개인 추천을 찾을 수 없습니다. personalRecommendationId : {0}"),
    CANDIDATE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 개인 추천 후보를 찾을 수 없습니다. candidateId : {0}"),
    ALREADY_SELECTED(HttpStatus.CONFLICT, "이미 최종 후보가 선택된 개인 추천입니다. personalRecommendationId : {0}");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public String format(Object... args) {
        return MessageFormat.format(message, args);
    }

    @Override
    public String getDomainPrefix() {
        return "PERSONAL_RECOMMENDATION_";
    }
}
