package matchuri.backend.global.security;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import matchuri.backend.domain.auth.entity.FailureCode;
import matchuri.backend.domain.auth.service.OAuthUnlinkJobServiceImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthUnlinkFailureHandler {

    private final OAuthUnlinkJobServiceImpl jobService;

    public void handle(Long memberId, RuntimeException exception) {
        Failure failure = classify(exception);
        if (!failure.code().isRetryable()) {
            log.error("OAuth2 unlink failed. memberId={} failureCode={} httpStatus={} errorType={}",
                    memberId, failure.code(), failure.httpStatus(), exception.getClass().getSimpleName());
            return;
        }

        log.warn("OAuth2 unlink attempt failed. memberId={} failureCode={} httpStatus={}",
                memberId, failure.code(), failure.httpStatus());
        try {
            jobService.recordFailureJob(memberId, failure.code(), failure.httpStatus());
        } catch (RuntimeException recordException) {
            log.error("Failed to record OAuth2 unlink failure. memberId={} errorType={}",
                    memberId, recordException.getClass().getSimpleName());
        }
    }

    public void handleRetryFailure(Long jobId, RuntimeException exception) {
        Failure failure = classify(exception);
        jobService.recordRetryFailure(jobId, failure.code(), failure.httpStatus());
    }

    private Failure classify(RuntimeException exception) {
        if (exception instanceof RestClientResponseException response) {
            var status = response.getStatusCode();
            FailureCode code = status.is5xxServerError() ? FailureCode.HTTP_5XX : FailureCode.HTTP_ERROR;
            return new Failure(code, status.value());
        }
        if (exception instanceof ResourceAccessException access
                && (access.contains(SocketTimeoutException.class) || access.contains(HttpTimeoutException.class))) {
            return new Failure(FailureCode.TIMEOUT, null);
        }
        return new Failure(FailureCode.UNEXPECTED_ERROR, null);
    }

    private record Failure(FailureCode code, Integer httpStatus) {}
}
