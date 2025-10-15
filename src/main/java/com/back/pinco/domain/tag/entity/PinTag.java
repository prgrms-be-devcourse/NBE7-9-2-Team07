package com.back.pinco.domain.tag.entity;

import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.domain.post.entity.Post;
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
@Table(name = "pin_tags")
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
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;    // 게시글 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;    // 태그 ID

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;    // 생성일

    @Column(name = "modified_at")
    @LastModifiedDate
    private LocalDateTime modifiedAt;    // 수정일

    public PinTag(Pin pin, Post post, Tag tag) {
        this.pin = pin;
        this.post = post;
        this.tag = tag;
    }
}