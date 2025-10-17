package com.back.pinco.domain.bookmark.entity;

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
@Table(name = "bookmarks", uniqueConstraints = @UniqueConstraint(name = "uk_bookmark_user_pin", columnNames = {"user_id", "pin_id"}), indexes = {@Index(name = "idx_bookmark_user", columnList = "user_id"), @Index(name = "idx_bookmark_pin", columnList = "pin_id")})
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(name = "bookmark_id_gen", sequenceName = "BOOKMARK_SEQ", initialValue = 1, allocationSize = 50)
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookmark_id_gen")
    @Column(name = "bookmark_id")
    private Long id;    // 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;    // 사용자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;    // 핀 ID

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;    // 생성일

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE; // 삭제 여부

    @Column(name = "deleted_at")
    @LastModifiedDate
    private LocalDateTime deletedAt;    // 삭제일


    public Bookmark(User user, Pin pin) {
        this.user = user;
        this.pin = pin;
    }

    // 소프트 삭제
    public void setIsDeleted() {
        this.isDeleted = Boolean.TRUE;
        this.deletedAt = LocalDateTime.now();
    }

    // 북마크 복구
    public void restore() {
        this.isDeleted = Boolean.FALSE;
        this.deletedAt = null;
    }

}