package com.back.pinco.domain.pin.entity;

import com.back.pinco.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
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

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;   // 생성일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;               // 작성자

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;          // 공개 여부

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false; // 삭제 여부

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;   // 삭제일

    @Transient
    private int likeCount = 0; // 좋아요 수

    public Pin(Point point, User user) {
        this.point = point;
        this.user = user;
    }

    // 소프트 삭제
    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // 공개 여부 변경
    public void togglePublic() {
        this.isPublic = !this.isPublic;
    }

}