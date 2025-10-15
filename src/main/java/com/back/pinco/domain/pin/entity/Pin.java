package com.back.pinco.domain.pin.entity;

import com.back.pinco.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "pins")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "pin_id_gen",
        sequenceName = "PIN_SEQ",
        initialValue = 1,
        allocationSize = 50
)
public class Pin {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pin_id_gen")
    @Column(name = "pin_id")
    private Long id;    // 고유 ID

    @Column(name = "point", nullable = false, columnDefinition = "geography(Point, 4326)")
    private Point point;    // 위치

    @Column(name = "create_at", nullable = false)
    @CreatedDate
    private LocalDateTime createAt;   // 생성일

    @Column(name="user_id", nullable = false)
    @OneToOne(mappedBy = "pin", fetch = FetchType.LAZY)
    private User user;               // 유저 ID

    @Column(name="is_public", nullable = false)
    private Boolean isPublic = true;          // 공개 여부

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false; // 삭제 여부

    @Column(name = "deleted_at")
    @LastModifiedDate
    private LocalDateTime deletedAt;   // 삭제일


    public Pin(Point point, User user) {
        this.point = point;
        this.user = user;
    }
}
