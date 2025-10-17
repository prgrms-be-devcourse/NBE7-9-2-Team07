package com.back.pinco.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(
        name = "users",
        indexes = @Index(name = "idx_user_email", columnList = "email")
)
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
    private Long id;    // 고유 ID

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;    // 이메일

    @Column(name = "password", nullable = false)
    private String password;    // 비밀번호

    @Column(name = "username", nullable = false, length = 50)
    private String userName;    // 사용자명

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;    // 생성일

    @Column(name = "modified_at")
    @LastModifiedDate
    private LocalDateTime modifiedAt;    // 수정일


    public User(String email, String password, String userName) {
        this.email = email;
        this.password = password;
        this.userName = userName;
    }

    // 사용자명 변경
    public void updateUserName(String userName) {
        this.userName = userName;
    }

    // 비밀번호 변경
    public void updatePassword(String password) {
        this.password = password;
    }
}