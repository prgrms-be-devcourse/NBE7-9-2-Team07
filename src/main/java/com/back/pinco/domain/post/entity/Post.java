package com.back.pinco.domain.post.entity;

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
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "post_id_gen",
        sequenceName = "POST_SEQ",
        initialValue = 1,
        allocationSize = 50
)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_id_gen")
    @Column(name = "post_id")
    private Long id;    // 고유 ID

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;    // 핀 ID

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;    // 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;    // 사용자

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;    // 생성일

    @Column(name = "modified_at")
    @LastModifiedDate
    private LocalDateTime modifiedAt;    // 수정일

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;    // 공개 여부

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;    // 삭제 여부

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;    // 삭제일

    public Post(Pin pin, String content, User user) {
        this.pin = pin;
        this.content = content;
        this.user = user;
    }
}