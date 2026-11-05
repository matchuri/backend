package matchuri.backend.api.group.dto.response;

import java.util.List;

final class GroupMocks {

    private GroupMocks() {
    }

    static List<GroupMemberSummaryResponse> members() {
        return List.of(
                GroupMemberSummaryResponse.mockOwner(),
                GroupMemberSummaryResponse.mockMember(2L, "든든한한끼"),
                GroupMemberSummaryResponse.mockMember(3L, "매콤러버"),
                GroupMemberSummaryResponse.mockMember(4L, "국물파")
        );
    }

    static List<GroupRecommendationCandidateResponse> candidates() {
        return List.of(
                GroupRecommendationCandidateResponse.mockBibimbap(),
                GroupRecommendationCandidateResponse.mockPorkCutlet(),
                GroupRecommendationCandidateResponse.mockRiceNoodle()
        );
    }
}
