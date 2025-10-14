package com.back.pinco.domain.post.entity;

import com.back.pinco.domain.pin.entity.Pin;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;            // 고유 ID

    @Column(name = "content", nullable = false)
    private String content;     // 내용

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;  // 최초 등록일

    @Column(name = "modified_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime modifiedAt;  // 마지막 수정일

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false, unique = true)
    private Pin pin;
}
