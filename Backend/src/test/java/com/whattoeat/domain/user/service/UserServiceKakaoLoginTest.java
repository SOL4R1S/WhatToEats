package com.whattoeat.domain.user.service;

import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(SpringExtension.class)
public class UserServiceKakaoLoginTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> captor;

    private final String KAKAO_ID = "123456789";
    private final String nickname = "nickname";
    private final String profileImg = "img.jpg";
    private final String email = "test@test.com";

    private User existuser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        existuser = User.builder()
                .kakaoId(KAKAO_ID)
                .nickname("old")
                .profileImage("old.jpg")
                .email(email)
                .provider(Provider.KAKAO)
                .role(Role.USER)
                .build();
    }

    @Test
    @DisplayName("신규 회원가입")
    void kakaoLoginOrSignUp_newUser() {
        given(userRepository.findByKakaoId(KAKAO_ID)).willReturn(Optional.empty());

        userService.kakaoLoginOrSignUp(KAKAO_ID, nickname, profileImg, email);

        then(userRepository).should().save(captor.capture());
        User savedUser = captor.getValue();
        assertThat(savedUser.getKakaoId()).isEqualTo(KAKAO_ID);
        assertThat(savedUser.getProvider()).isEqualTo(Provider.KAKAO);
        assertThat(savedUser.getNickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("기존 회원은 카카오 프로필로 덮어쓰지 않는다")
    void kakaoLoginOrSignUp_existingUser_updateProfile() {
        given(userRepository.findByKakaoId(KAKAO_ID)).willReturn(Optional.of(existuser));

        userService.kakaoLoginOrSignUp(KAKAO_ID, "new_nickname", "new.jpg", email);
        assertThat(existuser.getNickname()).isEqualTo("old");
        assertThat(existuser.getProfileImage()).isEqualTo("old.jpg");
    }

    @Test
    @DisplayName("프로필 이미지 null일 경우 기존 이미지 사용")
    void kakaoLoginOrSignUp_nullUser_keepProfileImg() {
        given(userRepository.findByKakaoId(KAKAO_ID)).willReturn(Optional.of(existuser));

        userService.kakaoLoginOrSignUp(KAKAO_ID, "new_nickname", null, email);

        assertThat(existuser.getNickname()).isEqualTo("old");
        assertThat(existuser.getProfileImage()).isEqualTo("old.jpg");
    }
}
