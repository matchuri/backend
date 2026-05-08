package matchuri.backend.global.docs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OpenAPI 문서에 loginId 중복 확인 API의 요약과 제약 설명이 노출된다")
    void exposesFriendlyMetadataInOpenApi() throws Exception {
        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.summary")
                        .value("로그인 ID 중복 확인"))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.description")
                        .value(org.hamcrest.Matchers.containsString("인증 없이 호출할 수 있습니다.")))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.parameters[0].description")
                        .value(org.hamcrest.Matchers.containsString("1자 이상 50자 이하")))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.parameters[0].example")
                        .value("tester01"))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.security").isEmpty())
                .andExpect(jsonPath("$.components.schemas.LoginIdExistsResponse.properties.loginId.description")
                        .value("중복 확인한 로그인 ID"))
                .andExpect(jsonPath("$.components.schemas.LoginIdExistsResponse.properties.exists.description")
                        .value(org.hamcrest.Matchers.containsString("이미 존재하는 로그인 ID인지 여부")));
    }

    @Test
    @DisplayName("OpenAPI 문서에 Auth 공개 API의 비인증 정책과 envelope 응답 스키마가 노출된다")
    void exposesAuthPublicApiMetadataInOpenApi() throws Exception {
        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.summary")
                        .value("로컬 로그인"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/login'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/LoginApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/refresh'].post.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/refresh'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/LoginApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/refresh'].post.responses['401'].content['application/json'].examples.refreshTokenMissing.value.error.code")
                        .value("AUTH_REFRESH_TOKEN_MISSING"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/refresh'].post.responses['403'].content['application/json'].examples.inactiveMember.value.error.code")
                        .value("MEMBER_INACTIVE_MEMBER"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/oauth2/google'].get.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/auth/oauth2/exchange'].post.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/oauth2/exchange'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/LoginApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/email'].post.summary")
                        .value("이메일 인증 코드 발송"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/email'].post.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/email'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/SendEmailVerificationApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/email'].post.responses['502'].content['application/json'].examples.sendFailed.value.error.code")
                        .value("AUTH_EMAIL_SEND_FAILED"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/email/confirm'].post.summary")
                        .value("이메일 인증 코드 확인"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/email/confirm'].post.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/email/confirm'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/ConfirmEmailVerificationApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/email/confirm'].post.responses['401'].content['application/json'].examples.verificationFailed.value.error.code")
                        .value("AUTH_EMAIL_VERIFICATION_FAILED"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/recovery/login-id'].post.summary")
                        .value("로그인 ID 찾기"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/recovery/login-id'].post.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/recovery/login-id'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/FindLoginIdApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/recovery/login-id'].post.responses['401'].content['application/json'].examples.emailVerificationFailed.value.error.code")
                        .value("AUTH_EMAIL_VERIFICATION_FAILED"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/recovery/password'].post.summary")
                        .value("비밀번호 재설정"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/recovery/password'].post.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/recovery/password'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/ResetPasswordApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/recovery/password'].post.responses['401'].content['application/json'].examples.emailVerificationFailed.value.error.code")
                        .value("AUTH_EMAIL_VERIFICATION_FAILED"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/oauth2/exchange'].post.responses['400'].content['application/json'].examples.providerNotSupported.value.error.code")
                        .value("AUTH_OAUTH2_PROVIDER_NOT_SUPPORTED"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/logout'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/LogoutApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/auth/logout'].post.responses['401'].content['application/json'].examples.tokenMissing.value.error.code")
                        .value("AUTH_TOKEN_MISSING"));
    }

    @Test
    @DisplayName("OpenAPI 문서에 Member 공개 API의 비인증 정책과 envelope 응답 스키마가 노출된다")
    void exposesMemberPublicApiMetadataInOpenApi() throws Exception {
        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.paths['/api/v1/members/signup'].post.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/signup'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/RegisterLocalMemberApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/signup'].post.responses['401'].content['application/json'].examples.emailVerificationFailed.value.error.code")
                        .value("AUTH_EMAIL_VERIFICATION_FAILED"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/signup'].post.responses['409'].content['application/json'].examples.duplicateEmail.value.error.code")
                        .value("MEMBER_DUPLICATE_EMAIL"))
                .andExpect(jsonPath(
                        "$.components.schemas.RegisterLocalMemberRequest.properties.email.description")
                        .value(org.hamcrest.Matchers.containsString("이메일 인증")))
                .andExpect(jsonPath(
                        "$.components.schemas.RegisterLocalMemberRequest.properties.emailVerificationToken.description")
                        .value(org.hamcrest.Matchers.containsString("SIGNUP 목적")))
                .andExpect(jsonPath(
                        "$.components.schemas.RegisterLocalMemberResponse.properties.email.description")
                        .value("가입 시 인증 완료된 이메일입니다."))
                .andExpect(jsonPath("$.paths['/api/v1/members'].post.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/CreateMemberApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/{loginId}'].get.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/exists/{loginId}'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/LoginIdExistsApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/members/exists/nickname/{nickname}'].get.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/exists/nickname/{nickname}'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/NicknameExistsApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/me'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/MemberProfileApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/me'].get.responses['200'].content['application/json'].examples.success.value.data.email")
                        .value("tester@example.com"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/me'].get.responses['200'].content['application/json'].examples.success.value.data.loginId")
                        .value("tester01"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/me'].get.responses['401'].content['application/json'].examples.tokenMissing.value.error.code")
                        .value("AUTH_TOKEN_MISSING"))
                .andExpect(jsonPath(
                        "$.components.schemas.MemberProfileResponse.properties.loginId.description")
                        .value(org.hamcrest.Matchers.containsString("현재 로그인한 회원의 로그인 ID")))
                .andExpect(jsonPath(
                        "$.components.schemas.MemberProfileResponse.properties.email.description")
                        .value(org.hamcrest.Matchers.containsString("현재 로그인한 회원의 이메일")))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/me/taste-profile'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/MemberTasteProfileSummaryApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/me/taste-profile'].get.responses['401'].content['application/json'].examples.tokenMissing.value.error.code")
                        .value("AUTH_TOKEN_MISSING"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/me/taste-profile'].get.responses['403'].content['application/json'].examples.requiredAgreement.value.error.code")
                        .value("MEMBER_AGREEMENT_REQUIRED"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/me/taste-profile'].patch.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/MemberTasteProfileSummaryApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/me/taste-profile'].patch.responses['400'].content['application/json'].examples.invalidAttributeCategory.value.error.code")
                        .value("MEMBER_INVALID_TASTE_ATTRIBUTE_CATEGORY"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/me/taste-profile'].patch.responses['401'].content['application/json'].examples.tokenMissing.value.error.code")
                        .value("AUTH_TOKEN_MISSING"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/members/me/taste-profile'].patch.responses['403'].content['application/json'].examples.requiredAgreement.value.error.code")
                        .value("MEMBER_AGREEMENT_REQUIRED"))
                .andExpect(jsonPath(
                        "$.components.schemas.UpdateMemberTasteProfileRequest.properties.attributeCategoryIds.description")
                        .value(org.hamcrest.Matchers.containsString("attribute category ID 목록")))
                .andExpect(jsonPath(
                        "$.components.schemas.UpdateMemberTasteProfileRequest.properties.restrictionIngredientIds.description")
                        .value(org.hamcrest.Matchers.containsString("restriction ingredient ID 목록")))
                .andExpect(jsonPath(
                        "$.components.schemas.UpdateMemberTasteProfileRequest.properties.dislikedMenuItemIds.description")
                        .value(org.hamcrest.Matchers.containsString("disliked menu item ID 목록")))
                .andExpect(jsonPath(
                        "$.components.schemas.MemberTasteProfileSummaryResponse.properties.memberId.description")
                        .value("현재 로그인한 회원 ID입니다."))
                .andExpect(jsonPath(
                        "$.components.schemas.MemberTasteAttributeCategoryResponse.properties.categoryType.description")
                        .value("선택된 attribute category의 상위 유형입니다."))
                .andExpect(jsonPath(
                        "$.components.schemas.MemberTasteRestrictionIngredientResponse.properties.allergen.description")
                        .value("알레르기 유발 재료 여부입니다."))
                .andExpect(
                        jsonPath("$.components.schemas.MemberTasteDislikedMenuItemResponse.properties.code.description")
                                .value("선택된 disliked menu item 코드입니다."));
    }

    @Test
    @DisplayName("OpenAPI 문서에 Mock API 표시와 200 응답 예시가 노출된다")
    void exposesMockApiMetadataAndSuccessExamplesInOpenApi() throws Exception {
        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.paths['/api/v1/personal/recommendations'].get.summary")
                        .value("내 개인 추천 이력 목록 조회 (Mock API)"))
                .andExpect(jsonPath("$.paths['/api/v1/personal/recommendations'].get.parameters[0].name")
                        .value("page"))
                .andExpect(jsonPath("$.paths['/api/v1/personal/recommendations'].get.parameters[0].description")
                        .value("0부터 시작하는 페이지 번호입니다."))
                .andExpect(jsonPath("$.paths['/api/v1/personal/recommendations'].get.parameters[0].schema.default")
                        .value(0))
                .andExpect(jsonPath("$.paths['/api/v1/personal/recommendations'].get.parameters[1].name")
                        .value("size"))
                .andExpect(jsonPath("$.paths['/api/v1/personal/recommendations'].get.parameters[1].description")
                        .value("페이지 크기입니다. 기본값은 20입니다."))
                .andExpect(jsonPath("$.paths['/api/v1/personal/recommendations'].get.parameters[1].schema.default")
                        .value(20))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/personal/recommendations'].get.responses['200'].content['application/json'].examples.success.value.data.content[0].id")
                        .value(9001))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/personal/recommendations'].get.responses['200'].content['application/json'].examples.success.value.data.pageInfo.size")
                        .value(20))
                .andExpect(jsonPath("$.paths['/api/v1/personal/recommendations'].post.summary")
                        .value("개인 추천 요청 생성 (Mock API)"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/personal/recommendations'].post.responses['200'].content['application/json'].examples.success.value.data.candidates[0].menuName")
                        .value("비빔밥"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/personal/recommendations'].post.responses['200'].content['application/json'].examples.success.value.data.resultJson")
                        .doesNotExist())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/personal/recommendations/{requestId}'].get.responses['200'].content['application/json'].examples.success.value.data.contextJson.mealTime")
                        .value("LUNCH"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/personal/recommendations/{requestId}'].get.responses['200'].content['application/json'].examples.success.value.data.resultJson")
                        .doesNotExist())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/personal/recommendations/{requestId}/candidates'].get.responses['200'].content['application/json'].examples.success.value.data.candidates[2].menuName")
                        .value("쌀국수"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/personal/recommendations/{requestId}'].patch.responses['200'].content['application/json'].examples.success.value.data.selectedCandidateId")
                        .value(10001))
                .andExpect(jsonPath("$.paths['/api/v1/groups'].post.summary")
                        .value("그룹 생성 (Mock API)"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups'].post.responses['200'].content['application/json'].examples.success.value.data.groupId")
                        .value(3001))
                .andExpect(jsonPath("$.paths['/api/v1/groups'].get.summary")
                        .value("내 그룹 목록 조회 (Mock API)"))
                .andExpect(jsonPath("$.paths['/api/v1/groups'].get.parameters[1].name")
                        .value("page"))
                .andExpect(jsonPath("$.paths['/api/v1/groups'].get.parameters[1].description")
                        .value("0부터 시작하는 페이지 번호입니다."))
                .andExpect(jsonPath("$.paths['/api/v1/groups'].get.parameters[1].schema.default")
                        .value(0))
                .andExpect(jsonPath("$.paths['/api/v1/groups'].get.parameters[2].name")
                        .value("size"))
                .andExpect(jsonPath("$.paths['/api/v1/groups'].get.parameters[2].description")
                        .value("페이지 크기입니다. 기본값은 20입니다."))
                .andExpect(jsonPath("$.paths['/api/v1/groups'].get.parameters[2].schema.default")
                        .value(20))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups'].get.responses['200'].content['application/json'].examples.success.value.data.content[0].latestRecommendationStatus")
                        .value("OPEN"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups'].get.responses['200'].content['application/json'].examples.success.value.data.pageInfo.size")
                        .value(20))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups/{groupId}'].get.responses['200'].content['application/json'].examples.success.value.data.activeRecommendation.voteProgress.votedMemberCount")
                        .value(3))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups/{groupId}/invites'].post.responses['200'].content['application/json'].examples.success.value.data.inviteCode")
                        .value("LUNCH42"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups/join'].post.responses['200'].content['application/json'].examples.success.value.data.memberStatus")
                        .value("ACTIVE"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups/{groupId}/leave'].post.responses['200'].content['application/json'].examples.success.value.data.memberStatus")
                        .value("LEFT"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups/{groupId}/recommendations'].post.responses['200'].content['application/json'].examples.success.value.data.candidates[0].candidateId")
                        .value(8001))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups/{groupId}/recommendations/{sessionId}'].get.responses['200'].content['application/json'].examples.success.value.data.finalCandidate")
                        .value((Object) null))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups/{groupId}/recommendations/{sessionId}'].get.responses['200'].content['application/json'].examples.success.value.data.resultJson")
                        .doesNotExist())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups/{groupId}/recommendations/{sessionId}/candidates'].get.responses['200'].content['application/json'].examples.success.value.data.candidates[1].menuName")
                        .value("돈까스"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups/{groupId}/recommendations/{sessionId}/votes'].post.responses['200'].content['application/json'].examples.success.value.data.voteValue")
                        .value(1))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/groups/{groupId}/recommendations/{sessionId}/finalize'].patch.responses['200'].content['application/json'].examples.success.value.data.finalCandidate.menuName")
                        .value("비빔밥"))
                .andExpect(jsonPath("$.components.schemas.PageInfo.description")
                        .value("페이지네이션 메타데이터입니다. 페이지 번호는 0부터 시작합니다."))
                .andExpect(jsonPath("$.components.schemas.PageInfo.properties.page.description")
                        .value("현재 페이지 번호입니다. 0부터 시작합니다."))
                .andExpect(jsonPath("$.components.schemas.PageInfo.properties.size.description")
                        .value("요청한 페이지 크기입니다."))
                .andExpect(jsonPath("$.components.schemas.PageInfo.properties.totalElements.description")
                        .value("조회 조건에 해당하는 전체 요소 수입니다."))
                .andExpect(jsonPath("$.components.schemas.PageInfo.properties.hasNext.description")
                        .value("다음 페이지 존재 여부입니다."));
    }

    @Test
    @DisplayName("OpenAPI 문서에 Menu Reference 공개 API의 비인증 정책과 envelope 응답 스키마가 노출된다")
    void exposesMenuReferenceApiMetadataInOpenApi() throws Exception {
        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.paths['/api/v1/attribute-categories'].get.summary")
                        .value("attribute category 목록 조회"))
                .andExpect(jsonPath("$.paths['/api/v1/attribute-categories'].get.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/attribute-categories'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/AttributeCategoryListApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/restriction-ingredients'].get.summary")
                        .value("restriction ingredient 목록 조회"))
                .andExpect(jsonPath("$.paths['/api/v1/restriction-ingredients'].get.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/restriction-ingredients'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/RestrictionIngredientListApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/menu-items'].get.summary")
                        .value("메뉴 목록 조회"))
                .andExpect(jsonPath("$.paths['/api/v1/menu-items'].get.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/menu-items'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/MenuItemSummaryListApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/menu-items/{menuItemId}'].get.summary")
                        .value("메뉴 상세 조회"))
                .andExpect(jsonPath("$.paths['/api/v1/menu-items/{menuItemId}'].get.security").isEmpty())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/menu-items/{menuItemId}'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/MenuItemDetailApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/menu-items/{menuItemId}'].get.responses['404'].content['application/json'].examples.menuNotFound.value.error.code")
                        .value("MENU_NOT_FOUND"))
                .andExpect(
                        jsonPath("$.components.schemas.AttributeCategoryResponse.properties.categoryType.description")
                                .value("attribute category의 상위 유형입니다."))
                .andExpect(
                        jsonPath("$.components.schemas.RestrictionIngredientResponse.properties.allergen.description")
                                .value("알레르기 유발 재료 여부입니다."))
                .andExpect(jsonPath("$.components.schemas.MenuItemSummaryResponse.properties.code.description")
                        .value("메뉴 코드입니다."))
                .andExpect(jsonPath("$.components.schemas.MenuItemDetailResponse.properties.description.description")
                        .value("메뉴 설명입니다."));
    }

    @Test
    @DisplayName("OpenAPI 문서에 관리자 attribute category 조회 API의 보안 요구와 envelope 응답 스키마가 노출된다")
    void exposesMenuAdminReferenceApiMetadataInOpenApi() throws Exception {
        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/attribute-categories'].get.summary")
                        .value("관리자 attribute category 목록 조회"))
                .andExpect(
                        jsonPath("$.paths['/api/v1/admin/attribute-categories'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/attribute-categories'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/AdminAttributeCategoryListApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/attribute-categories'].get.responses['401'].content['application/json'].examples.tokenMissing.value.error.code")
                        .value("AUTH_TOKEN_MISSING"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/attribute-categories'].get.responses['403'].content['application/json'].examples.forbidden.value.error.code")
                        .value("AUTH_FORBIDDEN"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/ingredients'].get.summary")
                        .value("관리자 ingredient 목록 조회"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/ingredients'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/ingredients'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/AdminIngredientListApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/ingredients'].get.responses['401'].content['application/json'].examples.tokenMissing.value.error.code")
                        .value("AUTH_TOKEN_MISSING"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/ingredients'].get.responses['403'].content['application/json'].examples.forbidden.value.error.code")
                        .value("AUTH_FORBIDDEN"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/menu-items'].get.summary")
                        .value("관리자 메뉴 목록 조회"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/menu-items'].get.security[0].bearerAuth").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/menu-items'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/AdminMenuItemListApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/menu-items'].get.responses['401'].content['application/json'].examples.tokenMissing.value.error.code")
                        .value("AUTH_TOKEN_MISSING"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/menu-items'].get.responses['403'].content['application/json'].examples.forbidden.value.error.code")
                        .value("AUTH_FORBIDDEN"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/menu-items/{menuItemId}'].patch.summary")
                        .value("관리자 메뉴 수정"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/menu-items/{menuItemId}'].patch.security[0].bearerAuth").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/menu-items/{menuItemId}'].patch.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/AdminMenuItemApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/menu-items/{menuItemId}'].patch.responses['404'].content['application/json'].examples.notFound.value.error.code")
                        .value("MENU_NOT_FOUND"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/menu-items/{menuItemId}'].delete.summary")
                        .value("관리자 메뉴 비활성화"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/menu-items/{menuItemId}'].delete.security[0].bearerAuth").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/menu-items/{menuItemId}'].delete.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/AdminMenuItemApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/menu-items/{menuItemId}'].delete.responses['404'].content['application/json'].examples.notFound.value.error.code")
                        .value("MENU_NOT_FOUND"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/ingredients'].post.summary")
                        .value("관리자 ingredient 생성"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/ingredients'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/ingredients'].post.requestBody.required")
                        .value(true))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/ingredients'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/AdminIngredientApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/ingredients/{ingredientId}'].patch.summary")
                        .value("관리자 ingredient 수정"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/ingredients/{ingredientId}'].patch.security[0].bearerAuth").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/ingredients/{ingredientId}'].patch.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/AdminIngredientApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/ingredients/{ingredientId}'].delete.summary")
                        .value("관리자 ingredient 비활성화"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/ingredients/{ingredientId}'].delete.security[0].bearerAuth").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/ingredients/{ingredientId}'].delete.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/AdminIngredientApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/attribute-categories'].post.summary")
                        .value("관리자 attribute category 생성"))
                .andExpect(
                        jsonPath("$.paths['/api/v1/admin/attribute-categories'].post.security[0].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v1/admin/attribute-categories'].post.requestBody.required")
                        .value(true))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/attribute-categories'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/AdminAttributeCategoryApiResponse"))
                .andExpect(jsonPath("$.paths['/api/v1/admin/attribute-categories/{attributeCategoryId}'].patch.summary")
                        .value("관리자 attribute category 수정"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/attribute-categories/{attributeCategoryId}'].patch.security[0].bearerAuth").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/attribute-categories/{attributeCategoryId}'].patch.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/AdminAttributeCategoryApiResponse"))
                .andExpect(
                        jsonPath("$.paths['/api/v1/admin/attribute-categories/{attributeCategoryId}'].delete.summary")
                                .value("관리자 attribute category 비활성화"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/attribute-categories/{attributeCategoryId}'].delete.security[0].bearerAuth").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/admin/attribute-categories/{attributeCategoryId}'].delete.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/AdminAttributeCategoryApiResponse"))
                .andExpect(jsonPath(
                        "$.components.schemas.UpdateAdminAttributeCategoryRequest.properties.isActive.description")
                        .value("수정할 활성 여부입니다. null이면 활성 상태를 변경하지 않습니다."))
                .andExpect(jsonPath(
                        "$.components.schemas.CreateAdminAttributeCategoryRequest.properties.categoryType.description")
                        .value(org.hamcrest.Matchers.containsString("허용 값은 FLAVOR")))
                .andExpect(jsonPath("$.components.schemas.CreateAdminIngredientRequest.properties.allergen.description")
                        .value("알레르기 유발 재료 여부입니다."))
                .andExpect(jsonPath("$.components.schemas.UpdateAdminIngredientRequest.properties.isActive.description")
                        .value("수정할 활성 여부입니다. null이면 활성 상태를 변경하지 않습니다."))
                .andExpect(jsonPath("$.components.schemas.UpdateAdminMenuItemRequest.properties.isActive.description")
                        .value("수정할 활성 여부입니다. null이면 활성 상태를 변경하지 않습니다."))
                .andExpect(jsonPath("$.components.schemas.AdminIngredientResponse.properties.isActive.description")
                        .value("운영 기준 활성 여부입니다."))
                .andExpect(jsonPath("$.components.schemas.AdminMenuItemResponse.properties.isActive.description")
                        .value("운영 기준 활성 여부입니다."))
                .andExpect(
                        jsonPath("$.components.schemas.AdminAttributeCategoryResponse.properties.isActive.description")
                                .value("운영 기준 활성 여부입니다."));
    }

    @Test
    @DisplayName("OpenAPI 문서에 Member Agreement API의 envelope 응답 스키마가 노출된다")
    void exposesMemberAgreementApiMetadataInOpenApi() throws Exception {
        mockMvc.perform(get("/docs/openapi"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/member-agreements/required-status'].get.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/RequiredAgreementStatusApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/member-agreements/required-status'].get.responses['401'].content['application/json'].examples.tokenMissing.value.error.code")
                        .value("AUTH_TOKEN_MISSING"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/member-agreements/consents'].post.responses['200'].content['application/json'].schema.$ref")
                        .value("#/components/schemas/SubmitRequiredAgreementsApiResponse"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/member-agreements/consents'].post.responses['400'].content['application/json'].examples.requiredTypesMissing.value.error.code")
                        .value("MEMBER_AGREEMENT_REQUIRED_TYPES_MISSING"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/member-agreements/consents'].post.responses['401'].content['application/json'].examples.tokenMissing.value.error.code")
                        .value("AUTH_TOKEN_MISSING"))
                .andExpect(jsonPath(
                        "$.paths['/api/v1/member-agreements/consents'].post.responses['409'].content['application/json'].examples.versionMismatch.value.error.code")
                        .value("MEMBER_AGREEMENT_VERSION_MISMATCH"))
                .andExpect(jsonPath(
                        "$.components.schemas.SubmitRequiredAgreementsRequest.properties.agreements.description")
                        .value(org.hamcrest.Matchers.containsString("필수 약관 동의 목록")))
                .andExpect(jsonPath("$.components.schemas.AgreementConsentRequest.properties.agreementType.description")
                        .value("약관 종류입니다."))
                .andExpect(jsonPath(
                        "$.components.schemas.SubmitRequiredAgreementsResponse.properties.accessToken.description")
                        .value(org.hamcrest.Matchers.containsString("새 access token")));
    }
}
