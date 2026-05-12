package matchuri.backend.api.recommendation.dto.docs;

public final class RecommendationApiExamples {

    private RecommendationApiExamples() {
    }

    public static final String GUEST_PERSONAL_RECOMMENDATION_CREATE_SUCCESS = """
            {
              "success": true,
              "data": {
                "candidates": [
                  {
                    "menuId": 1001,
                    "menuName": "비빔밥",
                    "rankNo": 1,
                    "score": 93.5
                  },
                  {
                    "menuId": 1002,
                    "menuName": "돈까스",
                    "rankNo": 2,
                    "score": 86.0
                  },
                  {
                    "menuId": 1003,
                    "menuName": "쌀국수",
                    "rankNo": 3,
                    "score": 81.5
                  }
                ]
              },
              "error": null
            }
            """;

    public static final String PERSONAL_RECOMMENDATION_LIST_SUCCESS = """
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "id": 9001,
                    "status": "COMPLETED",
                    "requestedAt": "2026-05-06T12:10:00"
                  }
                ],
                "pageInfo": {
                  "page": 0,
                  "size": 20,
                  "totalElements": 1,
                  "totalPages": 1,
                  "first": true,
                  "last": true,
                  "hasNext": false,
                  "hasPrevious": false
                }
              },
              "error": null
            }
            """;

    public static final String PERSONAL_RECOMMENDATION_CREATE_SUCCESS = """
            {
              "success": true,
              "data": {
                "requestId": 9001,
                "status": "COMPLETED",
                "requestedAt": "2026-05-06T12:10:00",
                "candidates": [
                  {
                    "id": 10001,
                    "menuId": 1001,
                    "menuName": "비빔밥",
                    "rankNo": 1,
                    "score": 93.5
                  },
                  {
                    "id": 10002,
                    "menuId": 1002,
                    "menuName": "돈까스",
                    "rankNo": 2,
                    "score": 86.0
                  },
                  {
                    "id": 10003,
                    "menuId": 1003,
                    "menuName": "쌀국수",
                    "rankNo": 3,
                    "score": 81.5
                  }
                ]
              },
              "error": null
            }
            """;

    public static final String PERSONAL_RECOMMENDATION_DETAIL_SUCCESS = """
            {
              "success": true,
              "data": {
                "id": 9001,
                "status": "COMPLETED",
                "contextJson": {
                  "mealTime": "LUNCH",
                  "budgetLevel": 2,
                  "mood": "가볍지만 든든한 점심"
                },
                "candidates": [
                  {
                    "id": 10001,
                    "menuId": 1001,
                    "menuName": "비빔밥",
                    "rankNo": 1,
                    "score": 93.5
                  },
                  {
                    "id": 10002,
                    "menuId": 1002,
                    "menuName": "돈까스",
                    "rankNo": 2,
                    "score": 86.0
                  },
                  {
                    "id": 10003,
                    "menuId": 1003,
                    "menuName": "쌀국수",
                    "rankNo": 3,
                    "score": 81.5
                  }
                ],
                "selectedCandidateId": 10001
              },
              "error": null
            }
            """;

    public static final String PERSONAL_RECOMMENDATION_CANDIDATES_SUCCESS = """
            {
              "success": true,
              "data": {
                "requestId": 9001,
                "candidates": [
                  {
                    "id": 10001,
                    "menuId": 1001,
                    "menuName": "비빔밥",
                    "rankNo": 1,
                    "score": 93.5
                  },
                  {
                    "id": 10002,
                    "menuId": 1002,
                    "menuName": "돈까스",
                    "rankNo": 2,
                    "score": 86.0
                  },
                  {
                    "id": 10003,
                    "menuId": 1003,
                    "menuName": "쌀국수",
                    "rankNo": 3,
                    "score": 81.5
                  }
                ]
              },
              "error": null
            }
            """;

    public static final String PERSONAL_RECOMMENDATION_SELECT_SUCCESS = """
            {
              "success": true,
              "data": {
                "id": 9001,
                "selectedCandidateId": 10001,
                "updatedAt": "2026-05-06T12:15:00"
              },
              "error": null
            }
            """;
}
