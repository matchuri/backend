package matchuri.backend.domain.group.repository;

public record GroupRoomMemberCountRow(
        Long roomId,
        long memberCount
) {
}
