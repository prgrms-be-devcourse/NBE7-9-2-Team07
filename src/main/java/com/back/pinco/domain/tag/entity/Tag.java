package com.back.pinco.domain.tag.entity;

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
@Table(name = "tags")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "tag_id_gen",
        sequenceName = "TAG_SEQ",
        initialValue = 1,
        allocationSize = 50
)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_id_gen")
    @Column(name = "tag_id")
    private Long id;    // 고유 ID

    @Column(name = "keyword", nullable = false, unique = true, length = 50)
    private String keyword;    // 키워드

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;    // 생성일

    @Column(name = "modified_at")
    @LastModifiedDate
    private LocalDateTime modifiedAt;    // 수정일

    public Tag(String keyword) {
        this.keyword = keyword;
    }
}