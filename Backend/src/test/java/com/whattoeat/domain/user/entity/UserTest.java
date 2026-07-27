package com.whattoeat.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

    private User createUser(String nickname, String profileImage) {
        return User.builder()
                .loginId("testuser")
                .password("password")
                .nickname(nickname)
                .email("test@example.com")
                .provider(Provider.LOCAL)
                .profileImage(profileImage)
                .build();
    }

    @Test
    void updateProfile_닉네임과_프로필이미지를_변경한다() {
        User user = createUser("oldNickname", "old.jpg");

        user.updateProfile("newNickname", "new.jpg");

        assertThat(user.getNickname()).isEqualTo("newNickname");
        assertThat(user.getProfileImage()).isEqualTo("new.jpg");
    }

    @Test
    void updateProfile_닉네임만_변경하고_이미지는_유지한다() {
        User user = createUser("oldNickname", "existing.jpg");

        user.updateProfile("newNickname", null);

        assertThat(user.getNickname()).isEqualTo("newNickname");
        assertThat(user.getProfileImage()).isEqualTo("existing.jpg");
    }

    @Test
    void updateProfile_기존_이미지가_없을때_null을_유지한다() {
        User user = createUser("nickname", null);

        user.updateProfile("nickname", null);

        assertThat(user.getProfileImage()).isNull();
    }
}
