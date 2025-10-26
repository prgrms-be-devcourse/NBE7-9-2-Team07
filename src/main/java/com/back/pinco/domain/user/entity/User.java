package com.back.pinco.domain.user.entity;

import com.back.pinco.domain.bookmark.entity.Bookmark;
import com.back.pinco.domain.likes.entity.Likes;
import com.back.pinco.domain.pin.entity.Pin;
import com.back.pinco.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

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
public class User extends BaseEntity {

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

    @Column(name = "api_key", unique = true, length = 64)
    private String apiKey; // apiKey

    // ✅ 유저가 작성한 게시글들 (Pin)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Pin> pins = new ArrayList<>();

    // ✅ 유저가 누른 좋아요들 (Like)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Likes> likes = new ArrayList<>();

    // ✅ 유저가 등록한 북마크들 (Bookmark)
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Bookmark> bookmarks = new ArrayList<>();

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
