package matchuri.backend.domain.member.service;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.MemberErrorCode;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.repository.MemberRepository;
import matchuri.backend.domain.member.repository.MemberTasteProfileRepository;
import matchuri.backend.global.exception.BusinessException;
import matchuri.backend.global.security.AuthenticatedMember;
import matchuri.backend.global.security.AuthenticationFacade;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberTasteProfileRepository memberTasteProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public boolean existsByLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    @Override
    @Transactional
    public CreateMemberResult createMember(CreateMemberCommand command) {
        if (memberRepository.existsByLoginId(command.loginId())) {
            throw new BusinessException(
                    MemberErrorCode.DUPLICATE_LOGIN_ID,
                    MemberErrorCode.DUPLICATE_LOGIN_ID.format(command.loginId())
            );
        }

        String passwordHash = passwordEncoder.encode(command.password());
        Member member = createLocalMember(command.loginId(), passwordHash);

        return new CreateMemberResult(member.getId(), member.getLoginId(), member.getCreatedAt());
    }

    @Override
    public MemberProfileResult getMyProfile() {
        Member member = getCurrentActiveMember();
        return new MemberProfileResult(member.getId(), member.getNickname());
    }

    @Override
    @Transactional
    public UpdateMemberResult updateMyProfile(UpdateMemberBasicInfoCommand command) {
        Member member = getCurrentActiveMember();

        if (command.nickname() != null) {
            member.updateNickname(command.nickname().isBlank() ? null : command.nickname());
        }

        return new UpdateMemberResult(member.getId(), member.getUpdatedAt());
    }

    @Override
    @Transactional
    public UpdateMemberResult updateMyTasteProfile(UpdateMemberTasteProfileCommand command) {
        Member member = getCurrentActiveMember();

        MemberTasteProfile tasteProfile = memberTasteProfileRepository.findByMemberId(member.getId())
                .orElseGet(() -> memberTasteProfileRepository.save(
                        new MemberTasteProfile(member, command.profileVersion())
                ));
        tasteProfile.updateProfileVersion(command.profileVersion());

        return new UpdateMemberResult(member.getId(), member.getUpdatedAt());
    }

    @Override
    @Transactional
    public WithdrawMemberResult withdraw() {
        Member member = getCurrentActiveMember();
        member.withdraw();
        return new WithdrawMemberResult(member.getId(), member.getStatus().name());
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

    private Member createLocalMember(String loginId, String passwordHash) {
        try {
            Member newMember = Member.createWithEncodedPassword(loginId, passwordHash);
            return memberRepository.saveAndFlush(newMember);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(
                    MemberErrorCode.DUPLICATE_LOGIN_ID,
                    MemberErrorCode.DUPLICATE_LOGIN_ID.format(loginId)
            );
        }
    }
}
