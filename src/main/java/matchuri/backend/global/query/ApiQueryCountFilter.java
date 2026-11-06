package matchuri.backend.global.query;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@ConditionalOnProperty(name = "matchuri.query-monitor.enabled", havingValue = "true")
public class ApiQueryCountFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        QueryCountHolder.clear();

        try {
            filterChain.doFilter(request, response);
        } finally {
            QueryCount queryCount = QueryCountHolder.getGrandTotal();
            log.info(
                    "API_QUERY_BEFORE method={} uri={} status={} total={} select={} insert={} update={} delete={} other={} jdbcMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    queryCount.getTotal(),
                    queryCount.getSelect(),
                    queryCount.getInsert(),
                    queryCount.getUpdate(),
                    queryCount.getDelete(),
                    queryCount.getOther(),
                    queryCount.getTime()
            );
            QueryCountHolder.clear();
        }
    }
}
