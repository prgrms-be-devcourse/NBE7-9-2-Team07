package com.back.pinco.domain.tag.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Tag {

    @Id
    @Column(name = "tag_id")

    @Column(name = "keyword", nullable = false, unique = true, length = 50)

    @Column(name = "created_at", nullable = false, updatable = false)

    public Tag(String keyword) {
        this.keyword = keyword;
    }
