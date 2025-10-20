package com.back.pinco.domain.likes.entity;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.user.entity.User;
import com.back.pinco.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@NoArgsConstructor
@Getter
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_like_user_pin",
                columnNames = {"user_id", "pin_id"}
        ),
        indexes = {
                @Index(name = "idx_like_user", columnList = "user_id"),
                @Index(name = "idx_like_pin", columnList = "pin_id"),
                @Index(name = "idx_like_status", columnList = "is_liked")
        }
)
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "like_id_gen",
        sequenceName = "LIKE_SEQ",
        initialValue = 1,
        allocationSize = 50
)
public class Likes extends BaseEntity {

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
    private Boolean liked = true;    // 좋아요 여부


    public Likes(User user, Pin pin) {
        this.user = user;
        this.pin = pin;
        this.liked = true;
    }

    public void toggleLike() {
        this.liked = !this.liked;
    }

}