package matchuri.backend.domain.group.repository;

public interface GroupRoomMemberCountProjection {

    Long getRoomId();

    long getMemberCount();
}
