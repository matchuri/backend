package matchuri.backend.api.member.mapper;

import matchuri.backend.api.member.dto.LoginIdExistsResponse;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public LoginIdExistsResponse toLoginIdExistsResponse(String loginId, boolean exists) {
        return new LoginIdExistsResponse(loginId, exists);
    }
}
