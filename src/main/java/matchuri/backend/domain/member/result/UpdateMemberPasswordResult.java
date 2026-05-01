package matchuri.backend.domain.member.result;

public record UpdateMemberPasswordResult(
        boolean passwordChanged
) {

    public static UpdateMemberPasswordResult success() {
        return new UpdateMemberPasswordResult(true);
    }
}
