package matchuri.backend.domain.group.repository;

public interface GroupCandidateVoteCountProjection {

    Long getCandidateId();

    Long getVoteCount();
}
