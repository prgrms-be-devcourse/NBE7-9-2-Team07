package com.back.pinco.domain.likes.entity;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "pin_id"}))
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "like_id_gen",
        sequenceName = "LIKE_SEQ",
        initialValue = 1,
        allocationSize = 50
)
public class Likes {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "like_id_gen")
    @Column(name = "like_id")
    private Long id;    // 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;    // 사용자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;    // 핀 ID

    @Column(name = "is_liked", nullable = false)
    private Boolean isLiked = true;    // 좋아요 여부

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;    // 생성일

    @Column(name = "modified_at")
    @LastModifiedDate
    private LocalDateTime modifiedAt;    // 수정일

    public Likes(User user, Pin pin) {
        this.user = user;
        this.pin = pin;
        this.isLiked = true;
    }

    // 좋아요 토글
    public void toggleLike() {
        this.isLiked = !this.isLiked;
    }
}