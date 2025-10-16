package com.back.pinco.domain.pin.entity;

import com.back.pinco.domain.tag.entity.Tag;
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
@Table(
        name = "pins",
        indexes = @Index(name = "idx_pin_point", columnList = "point")
)
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

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;    // 내용

    // 이미지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;    // 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;    // 태그

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;    // 좋아요 수

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;    // 공개 여부

    @Column(name = "create_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;    // 생성일

    @Column(name = "modified_at")
    @LastModifiedDate
    private LocalDateTime modifiedAt;    // 수정일

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;    // 삭제 여부

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;    // 삭제일


    public Pin(Point point, User user) {
        this.point = point;
        this.user = user;
    }

    // 소프트 삭제
    public void setIsDeleted() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // 공개 여부 변경
    public void togglePublic() {
        this.isPublic = !this.isPublic;
    }

}