package matchuri.backend.domain.group.support;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GroupInviteLinkTokenGenerator {

    public String generate() {
        return UUID.randomUUID().toString();
    }
}
