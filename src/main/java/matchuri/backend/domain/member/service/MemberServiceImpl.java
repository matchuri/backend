package matchuri.backend.domain.member.service;

import lombok.RequiredArgsConstructor;
import matchuri.backend.api.auth.dto.LoginRequest;
import matchuri.backend.api.auth.dto.LoginResponse;
import matchuri.backend.api.auth.dto.LogoutResponse;
import matchuri.backend.api.member.dto.CreateMemberRequest;
import matchuri.backend.api.member.dto.CreateMemberResponse;
import matchuri.backend.api.member.dto.MemberProfileResponse;
import matchuri.backend.api.member.dto.UpdateMemberBasicInfoRequest;
import matchuri.backend.api.member.dto.UpdateMemberResponse;
import matchuri.backend.api.member.dto.UpdateMemberTasteProfileRequest;
import matchuri.backend.api.member.dto.WithdrawMemberResponse;
import matchuri.backend.api.member.mapper.MemberMapper;
import matchuri.backend.domain.auth.AuthErrorCode;
import matchuri.backend.domain.auth.service.JwtTokenProvider;
import matchuri.backend.domain.auth.service.TokenPair;
import matchuri.backend.domain.member.MemberErrorCode;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.global.exception.AuthenticationException;
import matchuri.backend.global.exception.BusinessException;
import matchuri.backend.global.security.AuthenticatedMember;
import matchuri.backend.global.security.AuthenticationFacade;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberTasteProfileRepository memberTasteProfileRepository;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public boolean existsByLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    @Override
    @Transactional
    public CreateMemberResponse createMember(CreateMemberRequest request) {
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(
                    MemberErrorCode.DUPLICATE_LOGIN_ID,
                    MemberErrorCode.DUPLICATE_LOGIN_ID.format(request.loginId())
            );
        }

        String passwordHash = passwordEncoder.encode(request.password());
        Member member = Member.createWithEncodedPassword(request.loginId(), passwordHash);
        memberRepository.save(member);

        return memberMapper.toCreateMemberResponse(member);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new AuthenticationException(AuthErrorCode.LOGIN_FAILED));

        ensureActive(member);

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new AuthenticationException(AuthErrorCode.LOGIN_FAILED);
        }

        TokenPair tokenPair = jwtTokenProvider.issueTokenPair(member);
        member.issueRefreshToken(tokenPair.refreshToken(), tokenPair.refreshTokenExpiresAt());

        return memberMapper.toLoginResponse(member, tokenPair);
    }

    @Override
    @Transactional
    public LogoutResponse logout() {
        Member member = getCurrentActiveMember();
        member.clearRefreshToken();
        return new LogoutResponse(true);
    }

    @Override
    public MemberProfileResponse getMyProfile() {
        return memberMapper.toMemberProfileResponse(getCurrentActiveMember());
    }

    @Override
    @Transactional
    public UpdateMemberResponse updateMyProfile(UpdateMemberBasicInfoRequest request) {
        Member member = getCurrentActiveMember();

        if (request.nickname() != null) {
            member.updateNickname(request.nickname().isBlank() ? null : request.nickname());
        }

        return memberMapper.toUpdateMemberResponse(member);
    }

    @Override
    @Transactional
    public UpdateMemberResponse updateMyTasteProfile(UpdateMemberTasteProfileRequest request) {
        Member member = getCurrentActiveMember();

        MemberTasteProfile tasteProfile = memberTasteProfileRepository.findByMemberId(member.getId())
                .orElseGet(() -> memberTasteProfileRepository.save(
                        new MemberTasteProfile(member, request.profileVersion())
                ));
        tasteProfile.updateProfileVersion(request.profileVersion());

        return memberMapper.toUpdateMemberResponse(member);
    }

    @Override
    @Transactional
    public WithdrawMemberResponse withdraw() {
        Member member = getCurrentActiveMember();
        member.withdraw();
        return memberMapper.toWithdrawMemberResponse(member);
    }

    private Member getCurrentActiveMember() {
        AuthenticatedMember authenticatedMember = authenticationFacade.getCurrentMember();
        Member member = memberRepository.findById(authenticatedMember.memberId())
                .orElseThrow(() -> new BusinessException(
                        MemberErrorCode.NOT_FOUND,
                        MemberErrorCode.NOT_FOUND.format(authenticatedMember.memberId())
                ));
        ensureActive(member);
        return member;
    }

    private void ensureActive(Member member) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(
                    MemberErrorCode.INACTIVE_MEMBER,
                    MemberErrorCode.INACTIVE_MEMBER.format(member.getId())
            );
        }
    }
}
