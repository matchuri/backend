package matchuri.backend.application.seed.coverage;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import matchuri.backend.application.seed.ReferenceSeedData;
import matchuri.backend.catalog.entity.CategoryType;
import matchuri.backend.recommendation.algorithm.RecommendationTargetType;
import matchuri.backend.recommendation.algorithm.input.MenuRecommendationInput;
import matchuri.backend.recommendation.algorithm.input.MenuRecommendationProfile;
import matchuri.backend.recommendation.algorithm.input.RecommendationContextSnapshot;
import matchuri.backend.recommendation.algorithm.input.TasteProfileSnapshot;
import matchuri.backend.recommendation.algorithm.output.MenuRecommendationCandidateResult;
import matchuri.backend.recommendation.algorithm.personal.PersonalMenuRecommendationAlgorithmV1;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

public final class MenuSeedCoverageAudit {

    private static final String DEFAULT_SEED = "seed/reference-data.json";
    private static final String DEFAULT_FIXTURES = "seed/recommendation-coverage-fixtures.json";
    private static final Path DEFAULT_OUTPUT = Path.of("build/reports/menu-seed-coverage.json");
    private static final int MINIMUM_CANDIDATES = 6;
    private static final JsonMapper JSON = JsonMapper.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();

    private MenuSeedCoverageAudit() {
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }

    public static int run(String[] args) {
        return run(args, new PrintStream(System.out, true, StandardCharsets.UTF_8),
                new PrintStream(System.err, true, StandardCharsets.UTF_8));
    }

    public static int run(String[] args, PrintStream out, PrintStream err) {
        try {
            Options options = Options.parse(args);
            Path output = options.output().toAbsolutePath().normalize();
            requireDistinctOutput(output, options.seed());
            requireDistinctOutput(output, options.fixtures());
            ReferenceSeedData seed = read(options.seed(), DEFAULT_SEED, ReferenceSeedData.class);
            Fixtures fixtures = read(options.fixtures(), DEFAULT_FIXTURES, Fixtures.class);
            Report report = audit(seed, fixtures);
            Files.createDirectories(output.getParent());
            Files.writeString(output, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(report) + "\n",
                    StandardCharsets.UTF_8);
            print(report, out);
            out.println("JSON report: " + output);
            return options.strict() && report.hasSupportedShortfalls() ? 1 : 0;
        } catch (Exception exception) {
            err.println("Menu seed coverage audit failed: " + exception.getMessage());
            return 2;
        }
    }

    public static Report audit(ReferenceSeedData seed, Fixtures fixtures) {
        require(fixtures != null, "fixtures must not be null");
        require(fixtures.minimumCandidates() >= MINIMUM_CANDIDATES, "minimumCandidates must be at least 6");
        require(fixtures.batchSize() == 3, "batchSize must be 3");
        requireList(fixtures.profiles(), "profiles");
        requireList(fixtures.constraintScenarios(), "constraintScenarios");
        requireList(fixtures.unsupportedProfiles(), "unsupportedProfiles");
        SeedIndex index = index(seed);
        Map<String, ProfileFixture> profiles = new LinkedHashMap<>();
        List<CategoryReference> flavors = categoriesOfType(index, CategoryType.FLAVOR);
        List<CategoryReference> methods = categoriesOfType(index, CategoryType.COOKING_METHOD);
        require(!flavors.isEmpty() && !methods.isEmpty(), "FLAVOR and COOKING_METHOD categories are required");
        for (CategoryReference flavor : flavors) {
            for (CategoryReference method : methods) {
                String code = "BASE_" + flavor.code() + "_" + method.code();
                putUnique(profiles, code, new ProfileFixture(code, flavor.code() + " + " + method.code(),
                        List.of(flavor, method)), "generated base profile code");
            }
        }
        int baseCount = profiles.size();
        for (ProfileFixture profile : fixtures.profiles()) {
            validateProfile(profile, index);
            putUnique(profiles, profile.code(), profile, "profile code");
        }
        Set<String> allProfileCodes = new HashSet<>(profiles.keySet());
        for (UnsupportedProfileFixture profile : fixtures.unsupportedProfiles()) {
            require(profile != null, "unsupported profile must not be null");
            requireText(profile.code(), "unsupported profile code");
            requireText(profile.name(), "unsupported profile name");
            requireText(profile.reason(), "unsupported profile reason");
            preferenceIds(profile.preferredAttributes(), index);
            addUnique(allProfileCodes, profile.code(), "profile code");
        }
        for (ConstraintScenarioFixture scenario : fixtures.constraintScenarios()) {
            validateScenario(scenario, profiles, allProfileCodes, index);
        }

        PersonalMenuRecommendationAlgorithmV1 algorithm = new PersonalMenuRecommendationAlgorithmV1();
        List<ProfileReport> supported = new ArrayList<>();
        int position = 0;
        for (ProfileFixture profile : profiles.values()) {
            supported.add(evaluate(profile.code(), profile, position++ < baseCount ? "BASE" : "FIXTURE",
                    noConstraints(profile.code()), index, fixtures, algorithm));
        }
        List<ProfileReport> diagnostics = fixtures.constraintScenarios().stream()
                .map(scenario -> evaluate(scenario.code(), profiles.get(scenario.profileCode()), "CONSTRAINT",
                        scenario, index, fixtures, algorithm))
                .toList();
        return new Report(algorithm.type().name(), algorithm.version(), fixtures.minimumCandidates(),
                fixtures.batchSize(), index.menus().size(), baseCount, List.copyOf(supported), diagnostics,
                List.copyOf(fixtures.unsupportedProfiles()), metadata(index));
    }

    private static SeedIndex index(ReferenceSeedData seed) {
        require(seed != null, "seed must not be null");
        requireList(seed.attributeCategories(), "attributeCategories");
        requireList(seed.ingredients(), "ingredients");
        requireList(seed.menuItems(), "menuItems");
        requireList(seed.menuAttributeCategories(), "menuAttributeCategories");
        requireList(seed.menuIngredients(), "menuIngredients");
        Map<CategoryReference, Long> attributeIds = new LinkedHashMap<>();
        Map<CategoryReference, String> attributeNames = new LinkedHashMap<>();
        Map<Long, CategoryType> categoryTypes = new LinkedHashMap<>();
        for (var attribute : seed.attributeCategories()) {
            require(attribute != null && attribute.categoryType() != null, "attribute categoryType is required");
            requireText(attribute.code(), "attribute code");
            requireText(attribute.name(), "attribute name");
            CategoryReference key = new CategoryReference(attribute.categoryType(), attribute.code());
            long id = attributeIds.size() + 1L;
            putUnique(attributeIds, key, id, "attribute type/code");
            attributeNames.put(key, attribute.name());
            categoryTypes.put(id, attribute.categoryType());
        }
        Map<String, Long> ingredientIds = new LinkedHashMap<>();
        for (var ingredient : seed.ingredients()) {
            require(ingredient != null, "ingredient must not be null");
            requireText(ingredient.code(), "ingredient code");
            requireText(ingredient.name(), "ingredient name");
            putUnique(ingredientIds, ingredient.code(), ingredientIds.size() + 1L, "ingredient code");
        }
        Map<String, Long> menuIds = new LinkedHashMap<>();
        Map<String, String> menuNames = new LinkedHashMap<>();
        Map<String, Set<Long>> menuAttributes = new LinkedHashMap<>();
        Map<String, Set<Long>> menuIngredients = new LinkedHashMap<>();
        for (var menu : seed.menuItems()) {
            require(menu != null, "menu must not be null");
            requireText(menu.code(), "menu code");
            requireText(menu.name(), "menu name");
            putUnique(menuIds, menu.code(), menuIds.size() + 1L, "menu code");
            menuNames.put(menu.code(), menu.name());
            menuAttributes.put(menu.code(), new LinkedHashSet<>());
            menuIngredients.put(menu.code(), new LinkedHashSet<>());
        }
        require(!menuIds.isEmpty(), "menuItems must not be empty");
        for (var mapping : seed.menuAttributeCategories()) {
            require(mapping != null && mapping.categoryType() != null, "menu attribute mapping type is required");
            lookup(menuIds, mapping.menuCode(), "menu attribute menuCode");
            long attributeId = lookup(attributeIds,
                    new CategoryReference(mapping.categoryType(), mapping.categoryCode()), "menu attribute reference");
            addUnique(menuAttributes.get(mapping.menuCode()), attributeId, "menu attribute mapping " + mapping);
        }
        for (var mapping : seed.menuIngredients()) {
            require(mapping != null, "menu ingredient mapping must not be null");
            lookup(menuIds, mapping.menuCode(), "menu ingredient menuCode");
            long ingredientId = lookup(ingredientIds, mapping.ingredientCode(), "menu ingredient reference");
            addUnique(menuIngredients.get(mapping.menuCode()), ingredientId, "menu ingredient mapping " + mapping);
        }
        List<MenuRecommendationProfile> menus = menuIds.entrySet().stream()
                .map(entry -> new MenuRecommendationProfile(entry.getValue(), entry.getKey(),
                        menuNames.get(entry.getKey()), List.copyOf(menuAttributes.get(entry.getKey())),
                        List.copyOf(menuIngredients.get(entry.getKey()))))
                .toList();
        Map<Long, MenuRecommendationProfile> menusById = new LinkedHashMap<>();
        menus.forEach(menu -> menusById.put(menu.menuId(), menu));
        return new SeedIndex(attributeIds, attributeNames, categoryTypes, ingredientIds, menuIds, menus, menusById);
    }

    private static void validateProfile(ProfileFixture profile, SeedIndex index) {
        require(profile != null, "profile must not be null");
        requireText(profile.code(), "profile code");
        requireText(profile.name(), "profile name");
        preferenceIds(profile.preferredAttributes(), index);
    }

    private static void validateScenario(
            ConstraintScenarioFixture scenario,
            Map<String, ProfileFixture> profiles,
            Set<String> allCodes,
            SeedIndex index
    ) {
        require(scenario != null, "constraint scenario must not be null");
        requireText(scenario.code(), "constraint scenario code");
        requireText(scenario.name(), "constraint scenario name");
        addUnique(allCodes, scenario.code(), "profile/scenario code");
        lookup(profiles, scenario.profileCode(), "constraint profileCode");
        codeIds(scenario.restrictionIngredientCodes(), index.ingredientIds(), "restrictionIngredientCodes");
        codeIds(scenario.dislikedMenuCodes(), index.menuIds(), "dislikedMenuCodes");
        codeIds(scenario.recentSelectedMenuCodes(), index.menuIds(), "recentSelectedMenuCodes");
        codeIds(scenario.recentlySkippedMenuCodes(), index.menuIds(), "recentlySkippedMenuCodes");
    }

    private static List<Long> preferenceIds(List<CategoryReference> attributes, SeedIndex index) {
        requireList(attributes, "preferredAttributes");
        require(!attributes.isEmpty(), "preferredAttributes must not be empty");
        Set<CategoryReference> distinct = new HashSet<>();
        List<Long> ids = new ArrayList<>();
        for (CategoryReference attribute : attributes) {
            require(attribute != null && attribute.categoryType() != null, "preferred attribute type is required");
            requireText(attribute.code(), "preferred attribute code");
            addUnique(distinct, attribute, "preferred attribute");
            ids.add(lookup(index.attributeIds(), attribute, "preferred attribute reference"));
        }
        return List.copyOf(ids);
    }

    private static List<Long> codeIds(List<String> codes, Map<String, Long> ids, String label) {
        requireList(codes, label);
        Set<String> distinct = new HashSet<>();
        List<Long> result = new ArrayList<>();
        for (String code : codes) {
            requireText(code, label);
            addUnique(distinct, code, label);
            result.add(lookup(ids, code, label));
        }
        return List.copyOf(result);
    }

    private static ProfileReport evaluate(
            String code,
            ProfileFixture profile,
            String source,
            ConstraintScenarioFixture scenario,
            SeedIndex index,
            Fixtures fixtures,
            PersonalMenuRecommendationAlgorithmV1 algorithm
    ) {
        TasteProfileSnapshot participant = new TasteProfileSnapshot(1L, code,
                preferenceIds(profile.preferredAttributes(), index),
                codeIds(scenario.restrictionIngredientCodes(), index.ingredientIds(), "restrictionIngredientCodes"),
                codeIds(scenario.dislikedMenuCodes(), index.menuIds(), "dislikedMenuCodes"));
        List<Long> recent = codeIds(scenario.recentSelectedMenuCodes(), index.menuIds(), "recentSelectedMenuCodes");
        List<Long> skipped = codeIds(scenario.recentlySkippedMenuCodes(), index.menuIds(), "recentlySkippedMenuCodes");
        List<MenuRecommendationCandidateResult> all = recommend(algorithm, index, participant, recent, skipped,
                index.menus().size());
        List<String> fullMatchCodes = all.stream().filter(candidate -> candidate.score() == 100.0)
                .map(candidate -> index.menusById().get(candidate.menuId()).menuCode()).toList();
        List<MenuRecommendationCandidateResult> first = recommend(algorithm, index, participant, recent, skipped,
                fixtures.batchSize());
        Set<Long> rerollSkipped = new LinkedHashSet<>(skipped);
        first.forEach(candidate -> rerollSkipped.add(candidate.menuId()));
        List<MenuRecommendationCandidateResult> reroll = recommend(algorithm, index, participant, recent,
                List.copyOf(rerollSkipped), fixtures.batchSize());
        BatchReport firstBatch = batch(first, index);
        BatchReport rerollBatch = batch(reroll, index);
        Set<String> combined = new LinkedHashSet<>(firstBatch.menuCodes());
        combined.addAll(rerollBatch.menuCodes());
        boolean distinct = combined.size() == first.size() + reroll.size();
        int shortfall = Math.max(0, fixtures.minimumCandidates() - fullMatchCodes.size());
        boolean passes = shortfall == 0 && firstBatch.fullMatchCount() == fixtures.batchSize()
                && rerollBatch.fullMatchCount() == fixtures.batchSize() && distinct;
        return new ProfileReport(code, source.equals("CONSTRAINT") ? scenario.name() : profile.name(), source,
                List.copyOf(profile.preferredAttributes()), List.copyOf(scenario.restrictionIngredientCodes()),
                List.copyOf(scenario.dislikedMenuCodes()), List.copyOf(scenario.recentSelectedMenuCodes()),
                List.copyOf(scenario.recentlySkippedMenuCodes()), fullMatchCodes.size(), fullMatchCodes, shortfall,
                firstBatch, rerollBatch, distinct, combined.size(), passes);
    }

    private static List<MenuRecommendationCandidateResult> recommend(
            PersonalMenuRecommendationAlgorithmV1 algorithm,
            SeedIndex index,
            TasteProfileSnapshot participant,
            List<Long> recent,
            List<Long> skipped,
            int limit
    ) {
        return algorithm.recommend(new MenuRecommendationInput(RecommendationTargetType.PERSONAL,
                List.of(participant), index.menus(), RecommendationContextSnapshot.of("{}"), limit,
                recent, skipped, Map.of(), index.categoryTypes())).candidates();
    }

    private static BatchReport batch(List<MenuRecommendationCandidateResult> candidates, SeedIndex index) {
        List<CandidateReport> rows = candidates.stream().map(candidate -> {
            MenuRecommendationProfile menu = index.menusById().get(candidate.menuId());
            return new CandidateReport(menu.menuCode(), menu.menuName(), candidate.rankNo(), candidate.score());
        }).toList();
        return new BatchReport(rows.size(), (int) rows.stream().filter(row -> row.score() == 100.0).count(),
                rows.stream().map(CandidateReport::menuCode).toList(), rows);
    }

    private static Metadata metadata(SeedIndex index) {
        List<AttributeCoverage> attributes = index.attributeIds().entrySet().stream()
                .map(entry -> {
                    int count = (int) index.menus().stream()
                            .filter(menu -> menu.attributeCategoryIds().contains(entry.getValue())).count();
                    return new AttributeCoverage(entry.getKey().categoryType(), entry.getKey().code(),
                            index.attributeNames().get(entry.getKey()), count, count < MINIMUM_CANDIDATES);
                }).toList();
        List<MenuMetadataCoverage> menus = index.menus().stream().map(menu -> {
            Set<CategoryType> missing = EnumSet.allOf(CategoryType.class);
            menu.attributeCategoryIds().forEach(id -> missing.remove(index.categoryTypes().get(id)));
            return new MenuMetadataCoverage(menu.menuCode(), menu.menuName(), List.copyOf(missing));
        }).toList();
        return new Metadata(index.menus().size(), index.attributeIds().size(), index.ingredientIds().size(),
                attributes, attributes.stream().filter(AttributeCoverage::rare).toList(), menus);
    }

    private static List<CategoryReference> categoriesOfType(SeedIndex index, CategoryType type) {
        return index.attributeIds().keySet().stream().filter(category -> category.categoryType() == type).toList();
    }

    private static ConstraintScenarioFixture noConstraints(String profileCode) {
        return new ConstraintScenarioFixture(profileCode, profileCode, profileCode,
                List.of(), List.of(), List.of(), List.of());
    }

    private static <T> T read(Path path, String classpath, Class<T> type) throws IOException {
        if (path != null) {
            try (InputStream input = Files.newInputStream(path)) {
                return JSON.readValue(input, type);
            }
        }
        try (InputStream input = MenuSeedCoverageAudit.class.getClassLoader().getResourceAsStream(classpath)) {
            require(input != null, "missing classpath resource: " + classpath);
            return JSON.readValue(input, type);
        }
    }

    private static void requireDistinctOutput(Path output, Path input) throws IOException {
        if (input != null) {
            Path normalizedInput = input.toAbsolutePath().normalize();
            boolean sameFile = output.equals(normalizedInput)
                    || (Files.exists(output) && Files.exists(normalizedInput) && Files.isSameFile(output, normalizedInput));
            require(!sameFile, "output must not overwrite an input file: " + input);
        }
    }

    private static void print(Report report, PrintStream out) {
        out.printf(Locale.ROOT, "Menu seed coverage: %s %s, menus=%d, minimum=%d, batch=%d, base profiles=%d%n",
                report.algorithmType(), report.algorithmVersion(), report.menuCount(), report.minimumCandidates(),
                report.batchSize(), report.generatedBaseProfileCount());
        out.println("Temporary IDs follow seed declaration order; no selection history is used.");
        report.supportedProfiles().forEach(row -> printProfile("SUPPORTED", row, report.batchSize(), out));
        report.diagnosticProfiles().forEach(row -> printProfile("DIAGNOSTIC ONLY", row, report.batchSize(), out));
        out.println("Unsupported profiles: " + report.unsupportedProfiles().size());
        if (report.unsupportedProfiles().isEmpty()) {
            out.println("UNSUPPORTED []");
        }
        report.unsupportedProfiles().forEach(row -> out.printf("UNSUPPORTED %s %s %s reason=%s%n",
                row.code(), row.name(), row.preferredAttributes(), row.reason()));
        report.metadata().attributeCoverage().forEach(row -> out.printf("ATTRIBUTE %s:%s %s menus=%d rare=%s%n",
                row.categoryType(), row.code(), row.name(), row.menuCount(), row.rare()));
        report.metadata().menuCoverage().forEach(row -> out.printf("MENU %s %s missingCategoryTypes=%s%n",
                row.menuCode(), row.menuName(), row.missingCategoryTypes()));
        out.println("Supported coverage: " + (report.hasSupportedShortfalls() ? "SHORTFALL" : "PASS"));
    }

    private static void printProfile(String label, ProfileReport row, int batchSize, PrintStream out) {
        out.printf("%s %s %s preferences=%s fullMatchCount=%d shortfall=%d passes=%s%n", label, row.code(),
                row.name(), row.preferredAttributes(), row.fullMatchCount(), row.shortfall(), row.passes());
        out.println("  100-point menus=" + row.fullMatchMenuCodes());
        out.printf("  first=%d/%d %s, reroll after SKIP=%d/%d %s, distinct=%s (%d menus)%n",
                row.firstBatch().fullMatchCount(), batchSize, row.firstBatch().candidates(),
                row.rerollBatch().fullMatchCount(), batchSize, row.rerollBatch().candidates(),
                row.initialAndRerollDistinct(), row.distinctRecommendedMenuCount());
        if (row.source().equals("CONSTRAINT")) {
            out.printf("  restrictions=%s disliked=%s recentSelected=%s recentlySkipped=%s%n",
                    row.restrictionIngredientCodes(), row.dislikedMenuCodes(), row.recentSelectedMenuCodes(),
                    row.recentlySkippedMenuCodes());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void requireText(String value, String label) {
        require(value != null && !value.isBlank() && value.equals(value.trim()), label + " must be nonblank and trimmed");
    }

    private static void requireList(List<?> values, String label) {
        require(values != null, label + " must not be null");
    }

    private static <K, V> V lookup(Map<K, V> map, K key, String label) {
        require(key != null && map.containsKey(key), "unknown " + label + ": " + key);
        return map.get(key);
    }

    private static <K, V> void putUnique(Map<K, V> map, K key, V value, String label) {
        require(!map.containsKey(key), "duplicate " + label + ": " + key);
        map.put(key, value);
    }

    private static <T> void addUnique(Set<T> values, T value, String label) {
        require(values.add(value), "duplicate " + label + ": " + value);
    }

    public record Fixtures(
            int minimumCandidates, int batchSize, List<ProfileFixture> profiles,
            List<ConstraintScenarioFixture> constraintScenarios, List<UnsupportedProfileFixture> unsupportedProfiles
    ) {
    }

    public record ProfileFixture(String code, String name, List<CategoryReference> preferredAttributes) {
    }

    public record CategoryReference(CategoryType categoryType, String code) {
    }

    public record ConstraintScenarioFixture(
            String code, String name, String profileCode, List<String> restrictionIngredientCodes,
            List<String> dislikedMenuCodes, List<String> recentSelectedMenuCodes, List<String> recentlySkippedMenuCodes
    ) {
    }

    public record UnsupportedProfileFixture(
            String code, String name, List<CategoryReference> preferredAttributes, String reason
    ) {
    }

    public record Report(
            String algorithmType, String algorithmVersion, int minimumCandidates, int batchSize, int menuCount,
            int generatedBaseProfileCount, List<ProfileReport> supportedProfiles, List<ProfileReport> diagnosticProfiles,
            List<UnsupportedProfileFixture> unsupportedProfiles, Metadata metadata
    ) {
        public boolean hasSupportedShortfalls() {
            return supportedProfiles.stream().anyMatch(profile -> !profile.passes());
        }
    }

    public record ProfileReport(
            String code, String name, String source, List<CategoryReference> preferredAttributes,
            List<String> restrictionIngredientCodes, List<String> dislikedMenuCodes, List<String> recentSelectedMenuCodes,
            List<String> recentlySkippedMenuCodes, int fullMatchCount, List<String> fullMatchMenuCodes, int shortfall,
            BatchReport firstBatch, BatchReport rerollBatch, boolean initialAndRerollDistinct,
            int distinctRecommendedMenuCount, boolean passes
    ) {
    }

    public record BatchReport(int count, int fullMatchCount, List<String> menuCodes, List<CandidateReport> candidates) {
    }

    public record CandidateReport(String menuCode, String menuName, int rankNo, double score) {
    }

    public record Metadata(
            int menuCount, int attributeCount, int ingredientCount, List<AttributeCoverage> attributeCoverage,
            List<AttributeCoverage> rareAttributes, List<MenuMetadataCoverage> menuCoverage
    ) {
    }

    public record AttributeCoverage(CategoryType categoryType, String code, String name, int menuCount, boolean rare) {
    }

    public record MenuMetadataCoverage(String menuCode, String menuName, List<CategoryType> missingCategoryTypes) {
    }

    private record SeedIndex(
            Map<CategoryReference, Long> attributeIds, Map<CategoryReference, String> attributeNames,
            Map<Long, CategoryType> categoryTypes, Map<String, Long> ingredientIds, Map<String, Long> menuIds,
            List<MenuRecommendationProfile> menus, Map<Long, MenuRecommendationProfile> menusById
    ) {
    }

    private record Options(boolean strict, Path seed, Path fixtures, Path output) {
        private static Options parse(String[] args) {
            require(args != null, "args must not be null");
            boolean strict = false;
            Path seed = null;
            Path fixtures = null;
            Path output = DEFAULT_OUTPUT;
            Set<String> seen = new HashSet<>();
            for (int index = 0; index < args.length; index++) {
                String option = args[index];
                require(option != null, "argument must not be null");
                addUnique(seen, option, "option");
                if (option.equals("--strict")) {
                    strict = true;
                } else {
                    require(Arrays.asList("--seed", "--fixtures", "--output").contains(option), "unknown option: " + option);
                    require(index + 1 < args.length, "missing value for " + option);
                    String value = args[++index];
                    requireText(value, option);
                    require(!value.startsWith("--"), "missing value for " + option);
                    switch (option) {
                        case "--seed" -> seed = Path.of(value);
                        case "--fixtures" -> fixtures = Path.of(value);
                        case "--output" -> output = Path.of(value);
                        default -> throw new IllegalArgumentException("unknown option: " + option);
                    }
                }
            }
            return new Options(strict, seed, fixtures, output);
        }
    }
}
