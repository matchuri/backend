package matchuri.backend.application.seed.coverage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import matchuri.backend.application.seed.ReferenceSeedData;
import matchuri.backend.application.seed.coverage.MenuSeedCoverageAudit.CategoryReference;
import matchuri.backend.application.seed.coverage.MenuSeedCoverageAudit.ConstraintScenarioFixture;
import matchuri.backend.application.seed.coverage.MenuSeedCoverageAudit.Fixtures;
import matchuri.backend.application.seed.coverage.MenuSeedCoverageAudit.ProfileFixture;
import matchuri.backend.application.seed.coverage.MenuSeedCoverageAudit.Report;
import matchuri.backend.application.seed.coverage.MenuSeedCoverageAudit.UnsupportedProfileFixture;
import matchuri.backend.catalog.entity.CategoryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class MenuSeedCoverageAuditTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String BASE_PROFILE = "BASE_SPICY_SOUP";

    @TempDir
    Path temporaryDirectory;

    @Test
    @DisplayName("완전 일치 메뉴 6개는 최초 3개와 SKIP 후 다른 3개를 추천하여 지원 기준을 통과한다")
    void sixFullMatchesPassWithDistinctFirstAndRerollBatches() {
        Report report = MenuSeedCoverageAudit.audit(seed(6, 0), fixtures());
        var profile = report.supportedProfiles().getFirst();

        assertThat(report.generatedBaseProfileCount()).isEqualTo(1);
        assertThat(profile.code()).isEqualTo(BASE_PROFILE);
        assertThat(profile.fullMatchCount()).isEqualTo(6);
        assertThat(profile.shortfall()).isZero();
        assertThat(profile.firstBatch().menuCodes()).containsExactly("M1", "M2", "M3");
        assertThat(profile.rerollBatch().menuCodes()).containsExactly("M4", "M5", "M6");
        assertThat(profile.firstBatch().fullMatchCount()).isEqualTo(3);
        assertThat(profile.rerollBatch().fullMatchCount()).isEqualTo(3);
        assertThat(profile.initialAndRerollDistinct()).isTrue();
        assertThat(profile.distinctRecommendedMenuCount()).isEqualTo(6);
        assertThat(profile.passes()).isTrue();
        assertThat(report.hasSupportedShortfalls()).isFalse();
    }

    @Test
    @DisplayName("완전 일치 메뉴 5개는 부족분 1개와 리롤 후보 2개를 보고하고 지원 기준에서 실패한다")
    void fiveFullMatchesReportOneShortfallAndTwoRerollCandidates() {
        Report report = MenuSeedCoverageAudit.audit(seed(5, 0), fixtures());
        var profile = report.supportedProfiles().getFirst();

        assertThat(profile.fullMatchCount()).isEqualTo(5);
        assertThat(profile.shortfall()).isEqualTo(1);
        assertThat(profile.firstBatch().count()).isEqualTo(3);
        assertThat(profile.rerollBatch().menuCodes()).containsExactly("M4", "M5");
        assertThat(profile.rerollBatch().fullMatchCount()).isEqualTo(2);
        assertThat(profile.initialAndRerollDistinct()).isTrue();
        assertThat(profile.passes()).isFalse();
        assertThat(report.hasSupportedShortfalls()).isTrue();
    }

    @Test
    @DisplayName("실제 개인 알고리즘이 부분 점수 후보를 6개 반환해도 완전 일치 후보 수는 0개이다")
    void partialCandidatesDoNotFillFullMatchCoverage() {
        Report report = MenuSeedCoverageAudit.audit(seed(0, 6), fixtures());
        var profile = report.supportedProfiles().getFirst();

        assertThat(profile.fullMatchCount()).isZero();
        assertThat(profile.fullMatchMenuCodes()).isEmpty();
        assertThat(profile.shortfall()).isEqualTo(6);
        assertThat(profile.firstBatch().count()).isEqualTo(3);
        assertThat(profile.rerollBatch().count()).isEqualTo(3);
        assertThat(profile.firstBatch().candidates()).allSatisfy(candidate ->
                assertThat(candidate.score()).isEqualTo(50.0));
        assertThat(profile.firstBatch().fullMatchCount()).isZero();
        assertThat(profile.rerollBatch().fullMatchCount()).isZero();
        assertThat(report.hasSupportedShortfalls()).isTrue();
    }

    @Test
    @DisplayName("제약 시나리오는 네 제외 정책을 모두 지키며 제약으로 생긴 부족은 지원 프로필 판정을 바꾸지 않는다")
    void diagnosticConstraintsExcludeExactlyWithoutChangingSupportedCoverage() {
        ReferenceSeedData original = seed(6, 0);
        ReferenceSeedData constrained = new ReferenceSeedData(original.attributeCategories(), original.ingredients(),
                original.menuItems(), original.menuAttributeCategories(),
                List.of(new ReferenceSeedData.MenuIngredientSeed("M1", "MILK")));
        Fixtures fixtures = new Fixtures(6, 3, List.of(), List.of(combinedConstraints()), List.of());

        Report report = MenuSeedCoverageAudit.audit(constrained, fixtures);
        var diagnostic = report.diagnosticProfiles().getFirst();

        assertThat(report.supportedProfiles().getFirst().passes()).isTrue();
        assertThat(report.hasSupportedShortfalls()).isFalse();
        assertThat(diagnostic.fullMatchMenuCodes()).containsExactly("M5", "M6");
        assertThat(diagnostic.firstBatch().menuCodes()).containsExactly("M5", "M6");
        assertThat(diagnostic.rerollBatch().menuCodes()).isEmpty();
        assertThat(diagnostic.shortfall()).isEqualTo(4);
        assertThat(diagnostic.passes()).isFalse();
        assertThat(diagnostic.restrictionIngredientCodes()).containsExactly("MILK");
        assertThat(diagnostic.dislikedMenuCodes()).containsExactly("M2");
        assertThat(diagnostic.recentSelectedMenuCodes()).containsExactly("M3");
        assertThat(diagnostic.recentlySkippedMenuCodes()).containsExactly("M4");
    }

    @Test
    @DisplayName("복수 선호는 실제 유형별 OR로 만점을 계산하고 다른 유형이 빠진 메뉴는 완전 일치에 포함하지 않는다")
    void multiSelectFixturesUseCategoryOrAndRequireEverySelectedType() {
        ReferenceSeedData original = seed(6, 0);
        List<ReferenceSeedData.AttributeCategorySeed> categories = new ArrayList<>(original.attributeCategories());
        categories.add(category(CategoryType.FLAVOR, "NUTTY"));
        categories.add(category(CategoryType.COOKING_METHOD, "GRILLED"));
        categories.add(category(CategoryType.FOOD_CATEGORY, "JAPANESE"));
        categories.add(category(CategoryType.TEXTURE, "CRISPY"));
        categories.add(category(CategoryType.TEMPERATURE, "COLD"));
        List<ReferenceSeedData.MenuAttributeCategorySeed> mappings = original.menuAttributeCategories().stream()
                .filter(mapping -> !mapping.menuCode().equals("M6") || mapping.categoryType() != CategoryType.TEXTURE)
                .toList();
        ReferenceSeedData varied = new ReferenceSeedData(categories, original.ingredients(), original.menuItems(),
                mappings, original.menuIngredients());
        ProfileFixture profile = new ProfileFixture("MULTI", "유형별 대안", List.of(
                reference(CategoryType.FLAVOR, "SPICY"), reference(CategoryType.FLAVOR, "NUTTY"),
                reference(CategoryType.COOKING_METHOD, "SOUP"), reference(CategoryType.COOKING_METHOD, "GRILLED"),
                reference(CategoryType.FOOD_CATEGORY, "KOREAN"), reference(CategoryType.FOOD_CATEGORY, "JAPANESE"),
                reference(CategoryType.TEXTURE, "SOFT"), reference(CategoryType.TEXTURE, "CRISPY"),
                reference(CategoryType.TEMPERATURE, "HOT"), reference(CategoryType.TEMPERATURE, "COLD")));

        Report report = MenuSeedCoverageAudit.audit(varied,
                new Fixtures(6, 3, List.of(profile), List.of(), List.of()));
        var result = report.supportedProfiles().stream().filter(row -> row.code().equals("MULTI")).findFirst()
                .orElseThrow();

        assertThat(result.fullMatchMenuCodes()).containsExactly("M1", "M2", "M3", "M4", "M5");
        assertThat(result.firstBatch().candidates()).allSatisfy(candidate ->
                assertThat(candidate.score()).isEqualTo(100.0));
        assertThat(result.rerollBatch().candidates().getLast().menuCode()).isEqualTo("M6");
        assertThat(result.rerollBatch().candidates().getLast().score()).isEqualTo(80.0);
        assertThat(result.shortfall()).isEqualTo(1);
    }

    @Test
    @DisplayName("메타데이터는 사용하지 않은 속성과 희소 속성 및 메뉴의 누락 유형을 보고한다")
    void metadataReportsUnusedRareAttributesAndMissingMenuTypes() {
        ReferenceSeedData original = seed(5, 1);
        List<ReferenceSeedData.AttributeCategorySeed> categories = new ArrayList<>(original.attributeCategories());
        categories.add(category(CategoryType.FLAVOR, "NUTTY"));
        ReferenceSeedData varied = new ReferenceSeedData(categories, original.ingredients(), original.menuItems(),
                original.menuAttributeCategories(), original.menuIngredients());

        var metadata = MenuSeedCoverageAudit.audit(varied, fixtures()).metadata();

        assertThat(metadata.menuCount()).isEqualTo(6);
        assertThat(metadata.attributeCount()).isEqualTo(6);
        assertThat(metadata.ingredientCount()).isEqualTo(2);
        assertThat(metadata.rareAttributes()).extracting(attribute -> attribute.code())
                .containsExactlyInAnyOrder("SOUP", "NUTTY");
        assertThat(metadata.attributeCoverage().stream().filter(attribute -> attribute.code().equals("NUTTY")))
                .singleElement().satisfies(attribute -> assertThat(attribute.menuCount()).isZero());
        assertThat(metadata.menuCoverage().stream().filter(menu -> menu.menuCode().equals("M6")))
                .singleElement().satisfies(menu ->
                        assertThat(menu.missingCategoryTypes()).containsExactly(CategoryType.COOKING_METHOD));
        assertThat(metadata.menuCoverage().stream().filter(menu -> menu.menuCode().equals("M1")))
                .singleElement().satisfies(menu -> assertThat(menu.missingCategoryTypes()).isEmpty());
    }

    @Test
    @DisplayName("카탈로그의 중복 메뉴 코드와 중복 속성 키 및 잘못된 메뉴·속성 참조는 입력 오류이다")
    void rejectsDuplicateAndUnknownCatalogReferences() {
        ReferenceSeedData original = seed(6, 0);
        List<ReferenceSeedData.MenuItemSeed> duplicateMenus = new ArrayList<>(original.menuItems());
        duplicateMenus.add(original.menuItems().getFirst());
        assertInvalid(new ReferenceSeedData(original.attributeCategories(), original.ingredients(), duplicateMenus,
                original.menuAttributeCategories(), original.menuIngredients()), fixtures(), "duplicate menu code");

        List<ReferenceSeedData.AttributeCategorySeed> duplicateAttributes = new ArrayList<>(original.attributeCategories());
        duplicateAttributes.add(original.attributeCategories().getFirst());
        assertInvalid(new ReferenceSeedData(duplicateAttributes, original.ingredients(), original.menuItems(),
                original.menuAttributeCategories(), original.menuIngredients()), fixtures(), "duplicate attribute type/code");

        assertInvalid(new ReferenceSeedData(original.attributeCategories(), original.ingredients(), original.menuItems(),
                List.of(new ReferenceSeedData.MenuAttributeCategorySeed("UNKNOWN", CategoryType.FLAVOR, "SPICY")),
                List.of()), fixtures(), "unknown menu attribute menuCode");
        assertInvalid(new ReferenceSeedData(original.attributeCategories(), original.ingredients(), original.menuItems(),
                List.of(new ReferenceSeedData.MenuAttributeCategorySeed("M1", CategoryType.FLAVOR, "SOUP")),
                List.of()), fixtures(), "unknown menu attribute reference");
    }

    @Test
    @DisplayName("fixture의 중복 코드와 잘못된 속성·시나리오 프로필·제외 메뉴 참조는 입력 오류이다")
    void rejectsDuplicateAndUnknownFixtureReferences() {
        ProfileFixture valid = new ProfileFixture("CUSTOM", "대표 취향", basePreferences());
        assertInvalid(seed(6, 0), new Fixtures(6, 3, List.of(valid, valid), List.of(), List.of()),
                "duplicate profile code");
        assertInvalid(seed(6, 0), new Fixtures(6, 3, List.of(new ProfileFixture("UNKNOWN_ATTRIBUTE", "미지 속성",
                List.of(reference(CategoryType.FLAVOR, "UNKNOWN")))), List.of(), List.of()),
                "unknown preferred attribute reference");
        assertInvalid(seed(6, 0), new Fixtures(6, 3, List.of(), List.of(new ConstraintScenarioFixture(
                "UNKNOWN_PROFILE", "미지 프로필", "UNKNOWN", List.of(), List.of(), List.of(), List.of())), List.of()),
                "unknown constraint profileCode");
        assertInvalid(seed(6, 0), new Fixtures(6, 3, List.of(), List.of(new ConstraintScenarioFixture(
                "UNKNOWN_MENU", "미지 메뉴", BASE_PROFILE, List.of(), List.of("UNKNOWN"), List.of(), List.of())), List.of()),
                "unknown dislikedMenuCodes");
    }

    @Test
    @DisplayName("선호가 없는 지원 프로필은 만점 후보 커버리지 대상으로 허용하지 않는다")
    void rejectsSupportedProfileWithoutPreferences() {
        assertInvalid(seed(6, 0), new Fixtures(6, 3,
                List.of(new ProfileFixture("EMPTY", "선호 없음", List.of())), List.of(), List.of()),
                "preferredAttributes must not be empty");
    }

    @Test
    @DisplayName("미지원 프로필은 사유가 필수이며 유효한 사유는 결과에 그대로 남긴다")
    void unsupportedProfilesRequireAndPreserveReason() {
        UnsupportedProfileFixture unsupported = new UnsupportedProfileFixture("UNSUPPORTED", "지원 제외 예시",
                basePreferences(), "식사 메뉴 범위에서 제외하기로 결정한 사례");
        Report report = MenuSeedCoverageAudit.audit(seed(6, 0),
                new Fixtures(6, 3, List.of(), List.of(), List.of(unsupported)));

        assertThat(report.unsupportedProfiles()).containsExactly(unsupported);
        assertThat(report.supportedProfiles()).hasSize(1);
        assertInvalid(seed(6, 0), new Fixtures(6, 3, List.of(), List.of(), List.of(
                new UnsupportedProfileFixture("NO_REASON", "사유 없음", basePreferences(), " "))),
                "unsupported profile reason");
    }

    @Test
    @DisplayName("필수 기본 행렬 유형이 없으면 빈 행렬을 성공으로 보고하지 않고 입력 오류로 처리한다")
    void rejectsMissingBaseMatrixCategoryType() {
        ReferenceSeedData original = seed(6, 0);
        ReferenceSeedData withoutMethods = new ReferenceSeedData(original.attributeCategories().stream()
                .filter(category -> category.categoryType() != CategoryType.COOKING_METHOD).toList(),
                original.ingredients(), original.menuItems(), original.menuAttributeCategories().stream()
                .filter(mapping -> mapping.categoryType() != CategoryType.COOKING_METHOD).toList(), List.of());

        assertInvalid(withoutMethods, fixtures(), "FLAVOR and COOKING_METHOD categories are required");
    }

    @Test
    @DisplayName("CLI 일반 모드는 부족한 커버리지도 종료 0으로 보고하고 지정 경로에 JSON을 생성한다")
    void normalCliWritesShortfallReportAndReturnsZero() throws IOException {
        CliExecution execution = runCli(seed(5, 0), fixtures(), false);

        assertThat(execution.exitCode()).isZero();
        assertThat(execution.stderr()).isEmpty();
        assertThat(execution.stdout()).contains("Supported coverage: SHORTFALL");
        assertThat(execution.report()).exists();
        Report saved = JSON.readValue(Files.readString(execution.report()), Report.class);
        assertThat(saved.hasSupportedShortfalls()).isTrue();
        assertThat(saved.supportedProfiles().getFirst().shortfall()).isEqualTo(1);
    }

    @Test
    @DisplayName("CLI strict 모드는 지원 프로필이 부족하면 JSON을 남기고 종료 1을 반환한다")
    void strictCliReturnsOneForSupportedShortfall() throws IOException {
        CliExecution execution = runCli(seed(5, 0), fixtures(), true);

        assertThat(execution.exitCode()).isEqualTo(1);
        assertThat(execution.report()).exists();
        assertThat(execution.stderr()).isEmpty();
    }

    @Test
    @DisplayName("CLI strict 모드는 6개의 완전 일치 메뉴가 확보되면 종료 0을 반환한다")
    void strictCliReturnsZeroWhenSupportedCoveragePasses() throws IOException {
        CliExecution execution = runCli(seed(6, 0), fixtures(), true);

        assertThat(execution.exitCode()).isZero();
        assertThat(execution.stdout()).contains("Supported coverage: PASS");
        assertThat(execution.stderr()).isEmpty();
    }

    @Test
    @DisplayName("CLI strict 모드는 제약 진단 행만 부족한 경우 지원 커버리지 통과를 유지한다")
    void strictCliDoesNotFailForDiagnosticOnlyShortfall() throws IOException {
        Fixtures diagnosticFixture = new Fixtures(6, 3, List.of(), List.of(new ConstraintScenarioFixture(
                "DIAGNOSTIC", "최근 선택 제외", BASE_PROFILE, List.of(), List.of(), List.of("M1"), List.of())), List.of());
        CliExecution execution = runCli(seed(6, 0), diagnosticFixture, true);

        assertThat(execution.exitCode()).isZero();
        Report saved = JSON.readValue(Files.readString(execution.report()), Report.class);
        assertThat(saved.hasSupportedShortfalls()).isFalse();
        assertThat(saved.diagnosticProfiles().getFirst().shortfall()).isEqualTo(1);
        assertThat(execution.stdout()).contains("DIAGNOSTIC ONLY", "Supported coverage: PASS");
    }

    @Test
    @DisplayName("CLI는 잘못된 JSON 입력을 종료 2로 구분하고 성공 보고서를 생성하지 않는다")
    void cliReturnsTwoForInvalidJsonInput() throws IOException {
        Path seedFile = temporaryDirectory.resolve("invalid-seed.json");
        Path fixtureFile = temporaryDirectory.resolve("fixtures.json");
        Path reportFile = temporaryDirectory.resolve("invalid-report.json");
        Files.writeString(seedFile, "{ invalid JSON }");
        Files.writeString(fixtureFile, JSON.writeValueAsString(fixtures()));

        CliExecution execution = runArguments(new String[]{"--seed", seedFile.toString(), "--fixtures",
                fixtureFile.toString(), "--output", reportFile.toString()}, reportFile);

        assertThat(execution.exitCode()).isEqualTo(2);
        assertThat(execution.stderr()).contains("Menu seed coverage audit failed:");
        assertThat(reportFile).doesNotExist();
    }

    @Test
    @DisplayName("CLI 출력 경로가 입력 시드와 정규화 후 같으면 종료 2로 거절하고 원본을 보존한다")
    void cliRejectsOutputOverwritingInputSeed() throws IOException {
        Path seedFile = temporaryDirectory.resolve("seed.json");
        Path fixtureFile = temporaryDirectory.resolve("fixtures.json");
        String originalSeed = JSON.writeValueAsString(seed(6, 0));
        Files.writeString(seedFile, originalSeed);
        Files.writeString(fixtureFile, JSON.writeValueAsString(fixtures()));
        Path equivalentOutput = temporaryDirectory.resolve("unused/../seed.json");

        CliExecution execution = runArguments(new String[]{"--seed", seedFile.toString(), "--fixtures",
                fixtureFile.toString(), "--output", equivalentOutput.toString()}, equivalentOutput);

        assertThat(execution.exitCode()).isEqualTo(2);
        assertThat(execution.stderr()).contains("output must not overwrite an input file");
        assertThat(Files.readString(seedFile)).isEqualTo(originalSeed);
    }

    @Test
    @DisplayName("실제 선정 카탈로그와 fixture의 참조 및 다섯 유형 태그를 검증하고 모든 기본 조합을 빠짐없이 진단한다")
    void curatedClasspathCatalogHasValidReferencesAndCompleteCategoryTags() throws IOException {
        ReferenceSeedData seed = classpathJson("seed/reference-data.json", ReferenceSeedData.class);
        Fixtures fixtures = classpathJson("seed/recommendation-coverage-fixtures.json", Fixtures.class);

        Report report = MenuSeedCoverageAudit.audit(seed, fixtures);
        List<String> expectedBaseCodes = seed.attributeCategories().stream()
                .filter(category -> category.categoryType() == CategoryType.FLAVOR)
                .flatMap(flavor -> seed.attributeCategories().stream()
                        .filter(category -> category.categoryType() == CategoryType.COOKING_METHOD)
                        .map(method -> "BASE_" + flavor.code() + "_" + method.code()))
                .toList();

        assertThat(report.generatedBaseProfileCount()).isEqualTo(expectedBaseCodes.size());
        assertThat(report.supportedProfiles().stream().filter(profile -> profile.source().equals("BASE")))
                .extracting(profile -> profile.code()).containsExactlyInAnyOrderElementsOf(expectedBaseCodes);
        assertThat(report.supportedProfiles()).extracting(profile -> profile.code())
                .containsAll(fixtures.profiles().stream().map(ProfileFixture::code).toList());
        assertThat(report.diagnosticProfiles()).extracting(profile -> profile.code())
                .containsExactlyElementsOf(fixtures.constraintScenarios().stream()
                        .map(ConstraintScenarioFixture::code).toList());
        assertThat(report.metadata().menuCoverage()).hasSize(seed.menuItems().size());
        assertThat(report.metadata().menuCoverage()).allSatisfy(menu ->
                assertThat(menu.missingCategoryTypes()).as(menu.menuCode()).isEmpty());
    }

    @Test
    @DisplayName("기준 조리 구성의 가공 재료는 추적 대상 원료를 함께 매핑한다")
    void referenceRecipesIncludeIngredientSources() throws IOException {
        ReferenceSeedData seed = classpathJson("seed/reference-data.json", ReferenceSeedData.class);
        Map<String, Set<String>> ingredientsByMenu = seed.menuIngredients().stream().collect(Collectors.groupingBy(
                ReferenceSeedData.MenuIngredientSeed::menuCode,
                Collectors.mapping(ReferenceSeedData.MenuIngredientSeed::ingredientCode, Collectors.toSet())));
        Map<String, Set<String>> sources = Map.of(
                "TOFU", Set.of("SOYBEAN"), "CHEESE", Set.of("MILK"), "BLUE_CHEESE", Set.of("MILK"),
                "KIMCHI", Set.of("FISH", "SHRIMP"));

        ingredientsByMenu.forEach((menu, ingredients) -> sources.forEach((ingredient, requiredSources) -> {
            if (ingredients.contains(ingredient)) {
                assertThat(ingredients).as("%s의 %s 원료", menu, ingredient).containsAll(requiredSources);
            }
        }));
    }

    private ReferenceSeedData seed(int fullMatches, int partialMatches) {
        List<ReferenceSeedData.AttributeCategorySeed> categories = List.of(
                category(CategoryType.FLAVOR, "SPICY"), category(CategoryType.COOKING_METHOD, "SOUP"),
                category(CategoryType.FOOD_CATEGORY, "KOREAN"), category(CategoryType.TEXTURE, "SOFT"),
                category(CategoryType.TEMPERATURE, "HOT"));
        List<ReferenceSeedData.MenuItemSeed> menus = new ArrayList<>();
        List<ReferenceSeedData.MenuAttributeCategorySeed> mappings = new ArrayList<>();
        for (int index = 1; index <= fullMatches + partialMatches; index++) {
            String code = "M" + index;
            menus.add(new ReferenceSeedData.MenuItemSeed(code, "메뉴 " + index, "검증용 메뉴"));
            for (var category : categories) {
                if (index <= fullMatches || category.categoryType() != CategoryType.COOKING_METHOD) {
                    mappings.add(new ReferenceSeedData.MenuAttributeCategorySeed(code,
                            category.categoryType(), category.code()));
                }
            }
        }
        return new ReferenceSeedData(categories, List.of(
                new ReferenceSeedData.IngredientSeed("MILK", "우유", true, 10),
                new ReferenceSeedData.IngredientSeed("EGG", "계란", true, 20)), menus, mappings, List.of());
    }

    private Fixtures fixtures() {
        return new Fixtures(6, 3, List.of(), List.of(), List.of());
    }

    private List<CategoryReference> basePreferences() {
        return List.of(reference(CategoryType.FLAVOR, "SPICY"), reference(CategoryType.COOKING_METHOD, "SOUP"));
    }

    private ReferenceSeedData.AttributeCategorySeed category(CategoryType type, String code) {
        return new ReferenceSeedData.AttributeCategorySeed(type, code, code, 10);
    }

    private CategoryReference reference(CategoryType type, String code) {
        return new CategoryReference(type, code);
    }

    private ConstraintScenarioFixture combinedConstraints() {
        return new ConstraintScenarioFixture("COMBINED", "네 종류 제외", BASE_PROFILE,
                List.of("MILK"), List.of("M2"), List.of("M3"), List.of("M4"));
    }

    private void assertInvalid(ReferenceSeedData seed, Fixtures fixtures, String message) {
        assertThatThrownBy(() -> MenuSeedCoverageAudit.audit(seed, fixtures))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(message);
    }

    private CliExecution runCli(ReferenceSeedData seed, Fixtures fixtures, boolean strict) throws IOException {
        Path directory = Files.createTempDirectory(temporaryDirectory, "cli-");
        Path seedFile = directory.resolve("seed.json");
        Path fixtureFile = directory.resolve("fixtures.json");
        Path reportFile = directory.resolve("reports/coverage.json");
        Files.writeString(seedFile, JSON.writeValueAsString(seed));
        Files.writeString(fixtureFile, JSON.writeValueAsString(fixtures));
        List<String> arguments = new ArrayList<>(List.of("--seed", seedFile.toString(), "--fixtures",
                fixtureFile.toString(), "--output", reportFile.toString()));
        if (strict) {
            arguments.add("--strict");
        }
        return runArguments(arguments.toArray(String[]::new), reportFile);
    }

    private CliExecution runArguments(String[] arguments, Path reportFile) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        int exitCode;
        try (PrintStream out = new PrintStream(output, true, StandardCharsets.UTF_8);
             PrintStream err = new PrintStream(error, true, StandardCharsets.UTF_8)) {
            exitCode = MenuSeedCoverageAudit.run(arguments, out, err);
        }
        return new CliExecution(exitCode, output.toString(StandardCharsets.UTF_8),
                error.toString(StandardCharsets.UTF_8), reportFile);
    }

    private <T> T classpathJson(String resourceName, Class<T> type) throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertThat(input).as(resourceName).isNotNull();
            return JSON.readValue(input, type);
        }
    }

    private record CliExecution(int exitCode, String stdout, String stderr, Path report) {
    }
}
