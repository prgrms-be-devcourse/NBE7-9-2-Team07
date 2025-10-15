package com.back.pinco.domain.pin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "pins")
@EntityListeners(AuditingEntityListener.class)
public class Pin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pin_id")
    private Long id;

    @Column(name = "point", nullable = false, columnDefinition = "geography(Point, 4326)")
    private Point point;

    @Column(name = "create_at", nullable = false)
    @CreatedDate
    private LocalDateTime createAt;   // 생성일


    public Pin(Point point) {
        this.point = point;
    }
}
