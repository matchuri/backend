package matchuri.backend.domain.group.repository;

public interface GroupRecommendationVoteCountProjection {

    Long getCandidateId();

    Long getVoteCount();
}
