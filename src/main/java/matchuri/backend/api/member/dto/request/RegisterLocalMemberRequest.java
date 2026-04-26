package matchuri.backend.api.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import matchuri.backend.domain.member.entity.Member;

@Schema(
        name = "RegisterLocalMemberRequest",
        example = """
                {
                  "loginId": "tester2372",
                  "password": "tester2372!",
                  "nickname": "점심탐험가123",
                  "email": "tester@example.com",
                  "emailVerificationToken": "ev_q3JxFrSxYk4zJw2zq3ZpQh0a3z9q0x1y2z3A4b5C6dE",
                  "agreements": [
                    {
                      "agreementType": "TERMS_OF_SERVICE",
                      "agreementVersion": "2026-04-10"
                    },
                    {
                      "agreementType": "PRIVACY_POLICY",
                      "agreementVersion": "2026-04-10"
                    }
                  ]
                }
                """
)
public record RegisterLocalMemberRequest(
        @Schema(
                description = "회원 가입에 사용할 loginId입니다. 영문, 숫자, 점(.), 밑줄(_), 하이픈(-)만 사용할 수 있습니다.",
                example = "tester2372",
                maxLength = Member.LOGIN_ID_MAX_SIZE
        )
        @NotBlank(message = "loginId는 비어 있을 수 없습니다.")
        @Size(max = Member.LOGIN_ID_MAX_SIZE, message = "loginId는 " + Member.LOGIN_ID_MAX_SIZE + "자를 초과할 수 없습니다.")
        @Pattern(regexp = Member.LOGIN_ID_PATTERN, message = "loginId는 영문, 숫자, 점(.), 밑줄(_), 하이픈(-)만 사용할 수 있습니다.")
        String loginId,

        @Schema(
                description = "회원 가입 비밀번호입니다. " + Member.PASSWORD_MIN_SIZE + "자 이상 " + Member.PASSWORD_MAX_SIZE
                        + "자 이하를 사용합니다.",
                example = "P@ssw0rd!",
                minLength = Member.PASSWORD_MIN_SIZE,
                maxLength = Member.PASSWORD_MAX_SIZE
        )
        @NotBlank(message = "password는 비어 있을 수 없습니다.")
        @Size(
                min = Member.PASSWORD_MIN_SIZE,
                max = Member.PASSWORD_MAX_SIZE,
                message = "password는 " + Member.PASSWORD_MIN_SIZE + "자 이상 " + Member.PASSWORD_MAX_SIZE + "자 이하여야 합니다."
        )
        String password,

        @Schema(
                description = "가입 완료 전에 반드시 설정해야 하는 닉네임입니다.",
                example = "점심탐험가123",
                maxLength = Member.NICKNAME_MAX_SIZE
        )
        @NotBlank(message = "nickname은 비어 있을 수 없습니다.")
        @Pattern(regexp = "^(?!\\s*$).+", message = "nickname은 비어 있을 수 없습니다.")
        @Size(max = Member.NICKNAME_MAX_SIZE, message = "nickname은 " + Member.NICKNAME_MAX_SIZE + "자를 초과할 수 없습니다.")
        String nickname,

        @Schema(
                description = "이메일 인증을 완료한 자체 로그인 계정 복구용 이메일입니다.",
                example = "tester@example.com",
                maxLength = 150
        )
        @NotBlank(message = "email은 비어 있을 수 없습니다.")
        @Email(message = "email은 올바른 이메일 형식이어야 합니다.")
        @Size(max = 150, message = "email은 150자 이하여야 합니다.")
        String email,

        @Schema(
                description = "SIGNUP 목적 이메일 인증 확인 API에서 발급받은 token입니다.",
                example = "ev_q3JxFrSxYk4zJw2zq3ZpQh0a3z9q0x1y2z3A4b5C6dE"
        )
        @NotBlank(message = "emailVerificationToken은 비어 있을 수 없습니다.")
        String emailVerificationToken,

        @Schema(
                description = "필수 약관 동의 목록입니다. 현재는 서비스 이용약관과 개인정보 처리방침 2종을 모두 포함해야 합니다.",
                example = """
                        [
                          {
                            "agreementType": "TERMS_OF_SERVICE",
                            "agreementVersion": "2026-04-10"
                          },
                          {
                            "agreementType": "PRIVACY_POLICY",
                            "agreementVersion": "2026-04-10"
                          }
                        ]
                        """
        )
        @NotEmpty(message = "agreements는 비어 있을 수 없습니다.")
        List<@Valid AgreementConsentRequest> agreements
) {

    public record AgreementConsentRequest(
            @Schema(
                    description = "약관 종류입니다.",
                    example = "TERMS_OF_SERVICE"
            )
            @NotBlank(message = "agreementType은 비어 있을 수 없습니다.")
            String agreementType,
            @Schema(
                    description = "동의한 약관 버전입니다.",
                    example = "2026-04-10"
            )
            @NotBlank(message = "agreementVersion은 비어 있을 수 없습니다.")
            String agreementVersion
    ) {
    }
}
