package matchuri.backend.domain.member.service;

import lombok.RequiredArgsConstructor;
import matchuri.backend.domain.member.MemberErrorCode;
import matchuri.backend.domain.member.entity.Member;
import matchuri.backend.domain.member.entity.MemberAgreement;
import matchuri.backend.domain.member.entity.MemberStatus;
import matchuri.backend.domain.member.entity.MemberTasteProfile;
import matchuri.backend.domain.member.repository.MemberAgreementRepository;
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
    private final MemberAgreementRepository memberAgreementRepository;
    private final MemberTasteProfileRepository memberTasteProfileRepository;
    private final RequiredAgreementRequestValidator requiredAgreementRequestValidator;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public boolean existsByLoginId(String loginId) {
        return memberRepository.existsByLoginId(loginId);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    @Override
    @Transactional
    public RegisterLocalMemberResult registerLocalMember(RegisterLocalMemberCommand command) {
        String passwordHash = passwordEncoder.encode(command.password());
        Member member = createLocalMember(command.loginId(), passwordHash, command.nickname());

        requiredAgreementRequestValidator.validateAndIndex(command.agreements())
                .forEach((agreementType, agreementVersion) ->
                        memberAgreementRepository.save(MemberAgreement.create(member, agreementType, agreementVersion))
                );

        return new RegisterLocalMemberResult(
                member.getId(),
                member.getLoginId(),
                member.getNickname(),
                member.getCreatedAt()
        );
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
        Member member = createLocalMember(command.loginId(), passwordHash, null);

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
            String nickname = command.nickname().isBlank() ? null : command.nickname();
            validateNicknameDuplication(member, nickname);

            try {
                member.updateNickname(nickname);
                memberRepository.flush();
            } catch (DataIntegrityViolationException exception) {
                throw new BusinessException(
                        MemberErrorCode.DUPLICATE_NICKNAME,
                        MemberErrorCode.DUPLICATE_NICKNAME.format(nickname)
                );
            }
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

    private Member createLocalMember(String loginId, String passwordHash, String nickname) {
        validateNicknameDuplication(null, nickname);
        try {
            Member newMember = Member.createWithEncodedPassword(loginId, passwordHash, nickname);
            return memberRepository.saveAndFlush(newMember);
        } catch (DataIntegrityViolationException exception) {
            if (nickname != null && memberRepository.existsByNickname(nickname)) {
                throw new BusinessException(
                        MemberErrorCode.DUPLICATE_NICKNAME,
                        MemberErrorCode.DUPLICATE_NICKNAME.format(nickname)
                );
            }
            throw new BusinessException(
                    MemberErrorCode.DUPLICATE_LOGIN_ID,
                    MemberErrorCode.DUPLICATE_LOGIN_ID.format(loginId)
            );
        }
    }

    private void validateNicknameDuplication(Member member, String nickname) {
        if (nickname == null || (member != null && nickname.equals(member.getNickname()))) {
            return;
        }

        if (memberRepository.existsByNickname(nickname)) {
            throw new BusinessException(
                    MemberErrorCode.DUPLICATE_NICKNAME,
                    MemberErrorCode.DUPLICATE_NICKNAME.format(nickname)
            );
        }
    }
}
