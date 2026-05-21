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

    public static final String GUEST_RECOMMENDATION_DUPLICATE_ATTRIBUTE_CATEGORY = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 400,
                "code": "GUEST_RECOMMENDATION_DUPLICATE_ATTRIBUTE_CATEGORY",
                "message": "중복된 attribute category ID가 포함되어 있습니다. attributeCategoryIds : [10, 10]",
                "details": []
              }
            }
            """;

    public static final String GUEST_RECOMMENDATION_DUPLICATE_RESTRICTION_INGREDIENT = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 400,
                "code": "GUEST_RECOMMENDATION_DUPLICATE_RESTRICTION_INGREDIENT",
                "message": "중복된 restriction ingredient ID가 포함되어 있습니다. restrictionIngredientIds : [100, 100]",
                "details": []
              }
            }
            """;

    public static final String GUEST_RECOMMENDATION_DUPLICATE_DISLIKED_MENU_ITEM = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 400,
                "code": "GUEST_RECOMMENDATION_DUPLICATE_DISLIKED_MENU_ITEM",
                "message": "중복된 disliked menu item ID가 포함되어 있습니다. dislikedMenuItemIds : [1001, 1001]",
                "details": []
              }
            }
            """;

    public static final String GUEST_RECOMMENDATION_INVALID_ATTRIBUTE_CATEGORY = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 400,
                "code": "GUEST_RECOMMENDATION_INVALID_ATTRIBUTE_CATEGORY",
                "message": "유효하지 않거나 비활성화된 attribute category ID가 포함되어 있습니다. attributeCategoryIds : [9999]",
                "details": []
              }
            }
            """;

    public static final String GUEST_RECOMMENDATION_INVALID_RESTRICTION_INGREDIENT = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 400,
                "code": "GUEST_RECOMMENDATION_INVALID_RESTRICTION_INGREDIENT",
                "message": "유효하지 않거나 비활성화된 restriction ingredient ID가 포함되어 있습니다. restrictionIngredientIds : [9999]",
                "details": []
              }
            }
            """;

    public static final String GUEST_RECOMMENDATION_INVALID_DISLIKED_MENU_ITEM = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 400,
                "code": "GUEST_RECOMMENDATION_INVALID_DISLIKED_MENU_ITEM",
                "message": "유효하지 않거나 비활성화된 disliked menu item ID가 포함되어 있습니다. dislikedMenuItemIds : [9999]",
                "details": []
              }
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
                    "requestedAt": "2026-05-06T12:10:00",
                    "closedAt": null,
                    "closeReason": null
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
                "closedAt": null,
                "closeReason": null,
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

    public static final String PERSONAL_RECOMMENDATION_REROLL_SUCCESS = """
            {
              "success": true,
              "data": {
                "requestId": 9002,
                "status": "COMPLETED",
                "requestedAt": "2026-05-06T12:20:00",
                "closedAt": null,
                "closeReason": null,
                "candidates": [
                  {
                    "id": 10004,
                    "menuId": 1004,
                    "menuName": "김치찌개",
                    "rankNo": 1,
                    "score": 91.0
                  },
                  {
                    "id": 10005,
                    "menuId": 1005,
                    "menuName": "샐러드볼",
                    "rankNo": 2,
                    "score": 84.0
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
                "closedAt": "2026-05-06T12:15:00",
                "closeReason": "SELECTED",
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

    public static final String PERSONAL_RECOMMENDATION_TASTE_PROFILE_REQUIRED = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 403,
                "code": "PERSONAL_RECOMMENDATION_TASTE_PROFILE_REQUIRED",
                "message": "개인 추천을 만들려면 취향 프로필이 필요합니다. memberId : 1",
                "details": []
              }
            }
            """;

    public static final String PERSONAL_RECOMMENDATION_OPEN_EXISTS = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 409,
                "code": "PERSONAL_RECOMMENDATION_OPEN_EXISTS",
                "message": "이미 열린 개인 추천이 있습니다. personalRecommendationId : 9001",
                "details": []
              }
            }
            """;

    public static final String PERSONAL_RECOMMENDATION_NOT_FOUND = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 404,
                "code": "PERSONAL_RECOMMENDATION_NOT_FOUND",
                "message": "해당 개인 추천을 찾을 수 없습니다. personalRecommendationId : 9001",
                "details": []
              }
            }
            """;

    public static final String PERSONAL_RECOMMENDATION_CANDIDATE_NOT_FOUND = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 404,
                "code": "PERSONAL_RECOMMENDATION_CANDIDATE_NOT_FOUND",
                "message": "해당 개인 추천 후보를 찾을 수 없습니다. candidateId : 10001",
                "details": []
              }
            }
            """;

    public static final String PERSONAL_RECOMMENDATION_ALREADY_SELECTED = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 409,
                "code": "PERSONAL_RECOMMENDATION_ALREADY_SELECTED",
                "message": "이미 최종 후보가 선택된 개인 추천입니다. personalRecommendationId : 9001",
                "details": []
              }
            }
            """;

    public static final String PERSONAL_RECOMMENDATION_ALREADY_CLOSED = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 409,
                "code": "PERSONAL_RECOMMENDATION_ALREADY_CLOSED",
                "message": "이미 종료된 개인 추천입니다. personalRecommendationId : 9001",
                "details": []
              }
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
                "closedAt": "2026-05-06T12:15:00",
                "closeReason": "SELECTED",
                "updatedAt": "2026-05-06T12:15:00"
              },
              "error": null
            }
            """;
}
