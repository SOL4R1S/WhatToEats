package com.whattoeat.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class KakaoOAuth2UserTest {

    private User kakaoUser;
    private KakaoOAuth2User oAuth2User;

    @BeforeEach
    void setUp() {
        kakaoUser = User.builder()
                .kakaoId("123456789")
                .nickname("nickname")
                .profileImage("http://kakao.com/profile.jpg")
                .email("test@test.com")
                .provider(Provider.KAKAO)
                .role(Role.USER)
                .build();

        oAuth2User = new KakaoOAuth2User(kakaoUser);
    }

    @Test
    @DisplayName("내부 유저 객체 반환")
    void getUser_returns_inner_user() {
        assertThat(oAuth2User.getUser()).isSameAs(kakaoUser);
        assertThat(oAuth2User.getUser().getKakaoId()).isEqualTo("123456789");
        assertThat(oAuth2User.getUser().getNickname()).isEqualTo("nickname");
    }

    @Test
    @DisplayName("getAttributes 빈객체 반환")
    void getAttributes_returns_empty_map() {
        assertThat(oAuth2User.getAttributes()).isEmpty();
    }

    @Test
    @DisplayName("일반유저 ROLE USER")
    void getAuthorities_with_user_role() {
        Collection<? extends GrantedAuthority> authorities = oAuth2User.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.toString()).contains("ROLE_USER");
        assertThat(authorities.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("관리자 ROLE ADMIN")
    void getAuthorities_with_admin_role() {
        User adminUser = User.builder()
                .kakaoId("admin123")
                .nickname("admin")
                .email("admin@example.com")
                .provider(Provider.KAKAO)
                .role(Role.ADMIN)
                .build();
        KakaoOAuth2User adminOAuth2User = new KakaoOAuth2User(adminUser);

        Collection<? extends GrantedAuthority> authorities = adminOAuth2User.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities.toString()).contains("ROLE_ADMIN");
        assertThat(authorities.size()).isEqualTo(1);
    }
}
