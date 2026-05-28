package matchuri.backend.api.group.dto.docs;

public final class GroupApiExamples {

    private GroupApiExamples() {
    }

    public static final String CREATE_GROUP_SUCCESS = """
            {
              "success": true,
              "data": {
                "groupId": 3001,
                "inviteCode": "LUNCH42",
                "status": "ACTIVE"
              },
              "error": null
            }
            """;

    public static final String GROUP_LIST_SUCCESS = """
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "id": 3001,
                    "name": "오늘 점심 메뉴 회의",
                    "status": "ACTIVE",
                    "memberCount": 4,
                    "latestRecommendationStatus": "PREPARING",
                    "createdAt": "2026-05-06T12:00:00"
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

    public static final String GROUP_DETAIL_SUCCESS = """
            {
              "success": true,
              "data": {
                "id": 3001,
                "name": "오늘 점심 메뉴 회의",
                "inviteCode": "LUNCH42",
                "latitude": 37.498095,
                "longitude": 127.027610,
                "status": "ACTIVE",
                "members": [
                  {
                    "memberId": 1,
                    "nickname": "점심탐험가",
                    "role": "OWNER",
                    "status": "ACTIVE",
                    "joinedAt": "2026-05-06T12:01:00"
                  },
                  {
                    "memberId": 2,
                    "nickname": "든든한한끼",
                    "role": "MEMBER",
                    "status": "ACTIVE",
                    "joinedAt": "2026-05-06T12:02:00"
                  },
                  {
                    "memberId": 3,
                    "nickname": "매콤러버",
                    "role": "MEMBER",
                    "status": "ACTIVE",
                    "joinedAt": "2026-05-06T12:02:00"
                  },
                  {
                    "memberId": 4,
                    "nickname": "국물파",
                    "role": "MEMBER",
                    "status": "ACTIVE",
                    "joinedAt": "2026-05-06T12:02:00"
                  }
                ],
                "activeRecommendation": {
                  "sessionId": 5001,
                  "status": "PREPARING",
                  "readiness": {
                    "totalMemberCount": 4,
                    "readyMemberCount": 2,
                    "allReady": false
                  },
                  "candidates": [],
                  "voteProgress": null,
                  "finalCandidate": null,
                  "createdAt": "2026-05-06T12:05:00"
                }
              },
              "error": null
            }
            """;

    public static final String GROUP_DETAIL_OPEN_RECOMMENDATION_SUCCESS = """
            {
              "success": true,
              "data": {
                "id": 3001,
                "name": "오늘 점심 메뉴 회의",
                "inviteCode": "LUNCH42",
                "latitude": 37.498095,
                "longitude": 127.027610,
                "status": "ACTIVE",
                "members": [
                  {
                    "memberId": 1,
                    "nickname": "점심탐험가",
                    "role": "OWNER",
                    "status": "ACTIVE",
                    "joinedAt": "2026-05-06T12:01:00"
                  },
                  {
                    "memberId": 2,
                    "nickname": "든든한한끼",
                    "role": "MEMBER",
                    "status": "ACTIVE",
                    "joinedAt": "2026-05-06T12:02:00"
                  }
                ],
                "activeRecommendation": {
                  "sessionId": 5001,
                  "status": "OPEN",
                  "readiness": null,
                  "candidates": [
                    {
                      "candidateId": 8001,
                      "menuId": 1001,
                      "menuName": "비빔밥",
                      "rankNo": 1,
                      "score": 91.5,
                      "voteCount": 3
                    },
                    {
                      "candidateId": 8002,
                      "menuId": 1002,
                      "menuName": "돈까스",
                      "rankNo": 2,
                      "score": 84.0,
                      "voteCount": 1
                    },
                    {
                      "candidateId": 8003,
                      "menuId": 1003,
                      "menuName": "쌀국수",
                      "rankNo": 3,
                      "score": 79.5,
                      "voteCount": 0
                    }
                  ],
                  "voteProgress": {
                    "totalMemberCount": 4,
                    "votedMemberCount": 3
                  },
                  "finalCandidate": null,
                  "createdAt": "2026-05-06T12:05:00"
                }
              },
              "error": null
            }
            """;

    public static final String UPDATE_GROUP_SUCCESS = """
            {
              "success": true,
              "data": {
                "groupId": 3001,
                "name": "점심 회의방",
                "latitude": 37.498095,
                "longitude": 127.027610,
                "status": "ACTIVE",
                "updatedAt": "2026-05-18T12:30:00",
                "openGroupRecommendationId": 5001
              },
              "error": null
            }
            """;

    public static final String REROLL_RECOMMENDATION_DISABLED = """
            {
              "success": false,
              "data": null,
              "error": {
                "status": 410,
                "code": "GROUP_RECOMMENDATION_REROLL_DISABLED",
                "message": "그룹 추천 재요청은 현재 MVP에서 지원하지 않습니다."
              }
            }
            """;

    public static final String CREATE_NICKNAME_INVITE_SUCCESS = """
            {
              "success": true,
              "data": {
                "inviteId": 501,
                "groupId": 3001,
                "groupName": "오늘 점심 메뉴 회의",
                "targetMemberId": 42,
                "targetNickname": "점심탐험가",
                "expiresAt": "2026-05-20T12:00:00",
                "status": "PENDING"
              },
              "error": null
            }
            """;

    public static final String MY_INVITES_SUCCESS = """
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "inviteId": 501,
                    "groupId": 3001,
                    "groupName": "맛집 탐방 모임",
                    "requestMemberId": 11,
                    "requestMemberNickname": "나는야 임영웅",
                    "status": "PENDING",
                    "expiresAt": "2026-05-20T12:00:00",
                    "createdAt": "2026-05-19T12:00:00"
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

    public static final String RESPOND_INVITE_SUCCESS = """
            {
              "success": true,
              "data": {
                "inviteId": 501,
                "groupId": 3001,
                "inviteStatus": "ACCEPTED",
                "memberStatus": "ACTIVE",
                "respondedAt": "2026-05-19T12:10:00"
              },
              "error": null
            }
            """;

    public static final String JOIN_GROUP_SUCCESS = """
            {
              "success": true,
              "data": {
                "groupId": 3001,
                "memberStatus": "ACTIVE"
              },
              "error": null
            }
            """;

    public static final String LEAVE_GROUP_SUCCESS = """
            {
              "success": true,
              "data": {
                "groupId": 3001,
                "memberStatus": "LEFT",
                "leftAt": "2026-05-06T12:30:00"
              },
              "error": null
            }
            """;

    public static final String DELETE_GROUP_SUCCESS = """
            {
              "success": true,
              "data": {
                "groupId": 3001,
                "status": "DELETED",
                "deletedAt": "2026-05-18T12:30:00"
              },
              "error": null
            }
            """;

    public static final String CREATE_RECOMMENDATION_SUCCESS = """
            {
              "success": true,
              "data": {
                "sessionId": 5001,
                "status": "PREPARING",
                "candidates": []
              },
              "error": null
            }
            """;

    public static final String REROLL_RECOMMENDATION_SUCCESS = """
            {
              "success": true,
              "data": {
                "sessionId": 5001,
                "status": "OPEN",
                "candidates": [
                  {
                    "candidateId": 8001,
                    "menuId": 1001,
                    "menuName": "비빔밥",
                    "rankNo": 1,
                    "score": 91.5,
                    "voteCount": 3
                  },
                  {
                    "candidateId": 8002,
                    "menuId": 1002,
                    "menuName": "돈까스",
                    "rankNo": 2,
                    "score": 84.0,
                    "voteCount": 1
                  },
                  {
                    "candidateId": 8003,
                    "menuId": 1003,
                    "menuName": "쌀국수",
                    "rankNo": 3,
                    "score": 79.5,
                    "voteCount": 0
                  }
                ]
              },
              "error": null
            }
            """;

    public static final String RECOMMENDATION_LIST_SUCCESS = """
            {
              "success": true,
              "data": {
                "content": [
                  {
                    "sessionId": 5002,
                    "status": "PREPARING",
                    "startedAt": "2026-05-26T12:20:00",
                    "endedAt": null
                  },
                  {
                    "sessionId": 5001,
                    "status": "FINALIZED",
                    "startedAt": "2026-05-26T12:00:00",
                    "endedAt": "2026-05-26T12:15:00"
                  }
                ],
                "pageInfo": {
                  "page": 0,
                  "size": 20,
                  "totalElements": 2,
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

    public static final String RECOMMENDATION_SESSION_SUCCESS = """
            {
              "success": true,
              "data": {
                "sessionId": 5001,
                "status": "OPEN",
                "readiness": null,
                "candidates": [
                  {
                    "candidateId": 8001,
                    "menuId": 1001,
                    "menuName": "비빔밥",
                    "rankNo": 1,
                    "score": 91.5,
                    "voteCount": 3
                  },
                  {
                    "candidateId": 8002,
                    "menuId": 1002,
                    "menuName": "돈까스",
                    "rankNo": 2,
                    "score": 84.0,
                    "voteCount": 1
                  },
                  {
                    "candidateId": 8003,
                    "menuId": 1003,
                    "menuName": "쌀국수",
                    "rankNo": 3,
                    "score": 79.5,
                    "voteCount": 0
                  }
                ],
                "voteProgress": {
                  "totalMemberCount": 4,
                  "votedMemberCount": 3
                },
                "finalCandidate": null,
                "createdAt": "2026-05-06T12:05:00"
              },
              "error": null
            }
            """;

    public static final String RECOMMENDATION_CANDIDATES_SUCCESS = """
            {
              "success": true,
              "data": {
                "sessionId": 5001,
                "candidates": [
                  {
                    "candidateId": 8001,
                    "menuId": 1001,
                    "menuName": "비빔밥",
                    "rankNo": 1,
                    "score": 91.5,
                    "voteCount": 3
                  },
                  {
                    "candidateId": 8002,
                    "menuId": 1002,
                    "menuName": "돈까스",
                    "rankNo": 2,
                    "score": 84.0,
                    "voteCount": 1
                  },
                  {
                    "candidateId": 8003,
                    "menuId": 1003,
                    "menuName": "쌀국수",
                    "rankNo": 3,
                    "score": 79.5,
                    "voteCount": 0
                  }
                ]
              },
              "error": null
            }
            """;

    public static final String RECOMMENDATION_READINESS_SUCCESS = """
            {
              "success": true,
              "data": {
                "sessionId": 5001,
                "status": "PREPARING",
                "progress": {
                  "totalMemberCount": 4,
                  "readyMemberCount": 2,
                  "allReady": false
                },
                "members": [
                  {
                    "memberId": 1,
                    "nickname": "김철수",
                    "role": "OWNER",
                    "ready": true,
                    "readinessStatus": "READY",
                    "readinessUpdatedAt": "2026-05-26T12:05:00"
                  },
                  {
                    "memberId": 2,
                    "nickname": "김덕배",
                    "role": "MEMBER",
                    "ready": false,
                    "readinessStatus": null,
                    "readinessUpdatedAt": null
                  }
                ]
              },
              "error": null
            }
            """;

    public static final String READY_RECOMMENDATION_SUCCESS = """
            {
              "success": true,
              "data": {
                "sessionId": 5001,
                "status": "PREPARING",
                "readiness": {
                  "totalMemberCount": 4,
                  "readyMemberCount": 3,
                  "allReady": false
                },
                "candidates": []
              },
              "error": null
            }
            """;

    public static final String READY_RECOMMENDATION_OPEN_SUCCESS = """
            {
              "success": true,
              "data": {
                "sessionId": 5001,
                "status": "OPEN",
                "readiness": {
                  "totalMemberCount": 4,
                  "readyMemberCount": 4,
                  "allReady": true
                },
                "candidates": [
                  {
                    "candidateId": 8001,
                    "menuId": 1001,
                    "menuName": "비빔밥",
                    "rankNo": 1,
                    "score": 91.5,
                    "voteCount": 0
                  },
                  {
                    "candidateId": 8002,
                    "menuId": 1002,
                    "menuName": "돈까스",
                    "rankNo": 2,
                    "score": 84.0,
                    "voteCount": 0
                  },
                  {
                    "candidateId": 8003,
                    "menuId": 1003,
                    "menuName": "쌀국수",
                    "rankNo": 3,
                    "score": 79.5,
                    "voteCount": 0
                  }
                ]
              },
              "error": null
            }
            """;

    public static final String VOTE_SUCCESS = """
            {
              "success": true,
              "data": {
                "voteId": 91001,
                "candidateId": 8001,
                "votedAt": "2026-05-06T12:20:00"
              },
              "error": null
            }
            """;

    public static final String REVOTE_SUCCESS = """
            {
              "success": true,
              "data": {
                "voteId": 91001,
                "candidateId": 8002,
                "votedAt": "2026-05-06T12:23:00"
              },
              "error": null
            }
            """;

    public static final String FINALIZE_RECOMMENDATION_SUCCESS = """
            {
              "success": true,
              "data": {
                "sessionId": 5001,
                "status": "FINALIZED",
                "finalCandidate": {
                  "candidateId": 8001,
                  "menuId": 1001,
                  "menuName": "비빔밥",
                  "rankNo": 1,
                  "score": 91.5,
                  "voteCount": 3
                },
                "finalizedAt": "2026-05-06T12:25:00"
              },
              "error": null
            }
            """;
}
