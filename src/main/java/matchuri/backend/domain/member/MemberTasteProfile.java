package matchuri.backend.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// TODO : 연관관계 매핑을 위한 뼈대 엔티티 먼저 정의
@Getter
@Entity
@Table(
        name = "member_taste_profiles",
        comment = "회원 취향 프로필",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_taste_profiles_member", columnNames = "member_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTasteProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "회원 취향 프로필 ID")
    private Long id;
}
