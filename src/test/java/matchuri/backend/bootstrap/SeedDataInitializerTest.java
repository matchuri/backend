package matchuri.backend.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import matchuri.backend.domain.menu.entity.CategoryType;
import matchuri.backend.domain.menu.repository.AttributeCategoryRepository;
import matchuri.backend.domain.menu.repository.IngredientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import matchuri.backend.domain.member.repository.MemberRepository;

@SpringBootTest(properties = {
        "matchuri.seed.enabled=true",
        "matchuri.seed.sample-members-enabled=true"
})
@ActiveProfiles({"test", "local"})
class SeedDataInitializerTest {

    @Autowired
    private SeedDataInitializer seedDataInitializer;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AttributeCategoryRepository attributeCategoryRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Test
    @DisplayName("local 프로필에서는 참조 데이터와 샘플 회원 시드가 멱등하게 초기화된다")
    void initializesReferenceAndSampleDataIdempotently() throws Exception {
        long initialCount = memberRepository.count();
        long initialAttributeCategoryCount = attributeCategoryRepository.count();
        long initialIngredientCount = ingredientRepository.count();

        assertThat(memberRepository.existsByLoginId("tester01")).isTrue();
        assertThat(memberRepository.existsByLoginId("tester02")).isTrue();
        assertThat(attributeCategoryRepository.existsByCategoryTypeAndCode(CategoryType.FLAVOR, "SPICY")).isTrue();
        assertThat(attributeCategoryRepository.existsByCategoryTypeAndCode(CategoryType.TEMPERATURE, "HOT")).isTrue();
        assertThat(ingredientRepository.existsByCode("PEANUT")).isTrue();
        assertThat(ingredientRepository.existsByCode("EGG")).isTrue();

        seedDataInitializer.run(new DefaultApplicationArguments(new String[0]));

        assertThat(memberRepository.count()).isEqualTo(initialCount);
        assertThat(attributeCategoryRepository.count()).isEqualTo(initialAttributeCategoryCount);
        assertThat(ingredientRepository.count()).isEqualTo(initialIngredientCount);
        assertThat(memberRepository.existsByLoginId("tester01")).isTrue();
        assertThat(memberRepository.existsByLoginId("tester02")).isTrue();
        assertThat(attributeCategoryRepository.existsByCategoryTypeAndCode(CategoryType.FLAVOR, "SPICY")).isTrue();
        assertThat(ingredientRepository.existsByCode("PEANUT")).isTrue();
    }
}
