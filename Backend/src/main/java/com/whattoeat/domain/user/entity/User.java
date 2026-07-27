package com.whattoeat.domain.user.entity;

import com.whattoeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(name = "login_id", length = 50, unique = true)
    private String loginId;

    @Column(length = 255)
    private String password;

    @Column(name = "kakao_id", length = 255, unique = true)
    private String kakaoId;

    @Column(length = 100, nullable = false)
    private String nickname;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(length = 100, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Provider provider;

    @Builder
    public User(String loginId, String password, String kakaoId, String nickname,
                String profileImage, String email, Role role, Provider provider) {
        this.loginId = loginId;
        this.password = password;
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.email = email;
        this.role = role != null ? role : Role.USER;
        this.provider = provider;
    }

    public void updateProfile(String nickname, String profileImage) {
        if(nickname!=null) this.nickname = nickname;
        if (profileImage != null) this.profileImage = profileImage;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateLoginId(String loginId) {
        this.loginId = loginId;
    }
}
