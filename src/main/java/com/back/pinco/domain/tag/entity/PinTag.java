package com.back.pinco.domain.tag.entity;

import com.back.pinco.domain.pin.entity.Pin;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(
        name = "pin_tags",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pin_tag",
                columnNames = {"pin_id", "tag_id"}
        ),
        indexes = {
                @Index(name = "idx_pin_tag_pin", columnList = "pin_id"),
                @Index(name = "idx_pin_tag_tag", columnList = "tag_id"),
                @Index(name = "idx_pin_tag_deleted", columnList = "is_deleted")
        }
)
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "pin_tag_id_gen",
        sequenceName = "PIN_TAG_SEQ",
        initialValue = 1,
        allocationSize = 50
)
public class PinTag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pin_tag_id_gen")
    @Column(name = "pin_tag_id")
    private Long id;    // 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;    // 핀 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;    // 태그 ID

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;    // 삭제 여부

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;    // 생성일

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;    // 삭제일


    public PinTag(Pin pin, Tag tag) {
        this.pin = pin;
        this.tag = tag;
    }

    // 소프트 삭제
    public void setIsDeleted() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // 태그 복구
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }

    public PinTag(Pin pin, Tag tag, boolean isDeleted) {
        this.pin = pin;
        this.tag = tag;
        this.isDeleted = isDeleted;
    }
}