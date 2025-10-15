package com.back.pinco.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Table(name="users")
@EntityListeners(AuditingEntityListener.class)
@SequenceGenerator(
        name = "user_id_gen",
        sequenceName = "USER_SEQ",
        initialValue = 1,
        allocationSize = 50
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_gen")
    @Column(name = "user_id")
    private Long id;    //  고유 ID

    @Column(name = "email", nullable = false, unique = true)
    private String email;   // 이메일

    @Column(name ="password", nullable = false)
    private String password;    // 비밀번호

    @Column(name ="username", nullable = false, unique = true)
    private String username;    // 사용자명

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;    // 생성일
}
