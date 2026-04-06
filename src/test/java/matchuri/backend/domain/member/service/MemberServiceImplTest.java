package matchuri.backend.domain.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import matchuri.backend.api.member.dto.CreateMemberRequest;
import matchuri.backend.api.member.mapper.MemberMapper;
import matchuri.backend.domain.member.MemberErrorCode;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.global.exception.BusinessException;
import matchuri.backend.global.security.AuthenticationFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberTasteProfileRepository memberTasteProfileRepository;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Test
    @DisplayName("회원 가입 저장 충돌은 MEMBER_DUPLICATE_LOGIN_ID로 번역한다")
    void createMemberTranslatesIntegrityViolationToDuplicateLoginId() {
        CreateMemberRequest request = new CreateMemberRequest("tester01", "P@ssw0rd!");

        when(memberRepository.existsByLoginId("tester01")).thenReturn(false);
        when(passwordEncoder.encode("P@ssw0rd!")).thenReturn("encoded-password");
        when(memberRepository.saveAndFlush(any(Member.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate login id"));

        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(MemberErrorCode.DUPLICATE_LOGIN_ID);
    }
}
