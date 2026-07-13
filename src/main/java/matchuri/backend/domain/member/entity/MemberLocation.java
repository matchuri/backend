package matchuri.backend.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;

@Getter
@Entity
@Table(
        name = "member_locations",
        comment = "회원 개인 위치",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_locations_member", columnNames = "member_id")
        }
)
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberLocation extends BaseEntity {

    public static final int ADDRESS_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "회원 개인 위치 ID")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true, comment = "회원 ID")
    private Member member;

    @Column(nullable = false, precision = 10, scale = 7, comment = "위도")
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7, comment = "경도")
    private BigDecimal longitude;

    @Column(name = "radius_meters", nullable = false, comment = "반경 거리(미터)")
    private Integer radiusMeters;

    @Column(nullable = false, length = ADDRESS_MAX_LENGTH, comment = "주소 문자열")
    private String address;

    public MemberLocation(Member member, BigDecimal latitude, BigDecimal longitude, Integer radiusMeters,
                          String address) {
        this.member = member;
        update(latitude, longitude, radiusMeters, address);
    }

    public void update(BigDecimal latitude, BigDecimal longitude, Integer radiusMeters, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
        this.address = address.trim();
    }
}
