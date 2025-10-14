package com.back.pinco.domain.pin.entity;

import com.back.pinco.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "pins")
@EntityListeners(AuditingEntityListener.class)
public class Pin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pin_id")
    private Long id;

    @Column(name = "latitude", nullable = false)
    private Double latitude;    // 위도

    @Column(name = "longitude", nullable = false)
    private Double longitude;   // 경도

    @Column(name = "create_at", nullable = false)
    @CreatedDate
    private LocalDateTime createAt;   // 생성일

    // Pin 삭제 시 Post도 함께 삭제
    @OneToOne(mappedBy = "pin", cascade = CascadeType.ALL, orphanRemoval = true)
    private Post post;  // null 가능
}
