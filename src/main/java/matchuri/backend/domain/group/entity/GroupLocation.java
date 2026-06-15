package matchuri.backend.domain.group.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import matchuri.backend.domain.common.BaseEntity;

@Getter
@Entity
@Table(
        name = "group_locations",
        comment = "그룹 위치"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupLocation extends BaseEntity {

    public static final int ADDRESS_MAX_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(comment = "그룹 위치 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_room_id", nullable = false, comment = "그룹 방 ID")
    private GroupRoom room;

    @Column(precision = 10, scale = 7, comment = "위도")
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7, comment = "경도")
    private BigDecimal longitude;

    @Column(name = "radius_meters", comment = "반경 거리(미터)")
    private Integer radiusMeters;

    @Column(length = ADDRESS_MAX_LENGTH, comment = "주소 문자열")
    private String address;

    public GroupLocation(
            GroupRoom room,
            BigDecimal latitude,
            BigDecimal longitude,
            Integer radiusMeters,
            String address
    ) {
        this.room = room;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
        this.address = address;
    }

    public void update(
            BigDecimal latitude,
            BigDecimal longitude,
            Integer radiusMeters,
            String address
    ) {
        if (latitude != null && longitude != null) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        if (radiusMeters != null) {
            this.radiusMeters = radiusMeters;
        }
        if (address != null) {
            this.address = address;
        }
    }
}
