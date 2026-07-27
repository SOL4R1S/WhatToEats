package com.whattoeat.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.whattoeat.domain.follow.repository.FollowRepository;
import com.whattoeat.domain.user.dto.UpdateProfileRequest;
import com.whattoeat.domain.user.dto.UserProfileResponse;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import com.whattoeat.global.exception.DuplicateEmailException;
import com.whattoeat.global.exception.PasswordMismatchException;
import com.whattoeat.global.exception.UserNotFoundException;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User createUser(Long id, String nickname, String profileImage) {
        return createUser(id, nickname, profileImage, null, Provider.LOCAL);
    }

    private User createUser(Long id, String nickname, String profileImage, String password, Provider provider) {
        User user = User.builder()
                .nickname(nickname)
                .email("test@example.com")
                .provider(provider)
                .role(Role.USER)
                .profileImage(profileImage)
                .password(password)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private UpdateProfileRequest updateRequest(String nickname, String profileImage) {
        return new UpdateProfileRequest(
                nickname, null, null, null);
    }

    private UpdateProfileRequest updateEmailRequest(String email) {
        return new UpdateProfileRequest(
                null, email, null, null);
    }

    private UpdateProfileRequest updatePasswordRequest(String currentPassword, String newPassword) {
        return new UpdateProfileRequest(
                null, null, currentPassword, newPassword);
    }

    // ===================== getUser 테스트 =====================

    @Test
    void getUser_본인_프로필_조회시_isOwnProfile이_true() {
        User user = createUser(1L, "testNickname", "profile.jpg");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(followRepository
                .existsByFollower_IdAndFollowing_Id(1L, 1L)).willReturn(false);

        UserProfileResponse response = userService.getUser(1L, 1L);

        assertThat(response.isOwnProfile()).isTrue();
        assertThat(response.isFollowing()).isFalse();
        assertThat(response.nickname()).isEqualTo("testNickname");
    }

    @Test
    void getUser_타인_프로필_조회시_팔로우_중이면_isFollowing이_true() {
        User user = createUser(2L, "otherNickname", "other.jpg");
        given(userRepository.findById(2L)).willReturn(Optional.of(user));
        given(followRepository
                .existsByFollower_IdAndFollowing_Id(1L, 2L)).willReturn(true);

        UserProfileResponse response = userService.getUser(2L, 1L);

        assertThat(response.isOwnProfile()).isFalse();
        assertThat(response.isFollowing()).isTrue();
    }

    @Test
    void getUser_타인_프로필_조회시_팔로우_안하면_isFollowing이_false() {
        User user = createUser(2L, "otherNickname", "other.jpg");
        given(userRepository.findById(2L)).willReturn(Optional.of(user));
        given(followRepository
                .existsByFollower_IdAndFollowing_Id(1L, 2L)).willReturn(false);

        UserProfileResponse response = userService.getUser(2L, 1L);

        assertThat(response.isOwnProfile()).isFalse();
        assertThat(response.isFollowing()).isFalse();
    }

    @Test
    void getUser_존재하지_않는_유저이면_예외발생() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(999L, 1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ===================== updateProfile 테스트 =====================

    @Test
    void updateProfile_닉네임을_변경한다() {
        User user = createUser(
                1L,
                "oldNickname",
                "old.jpg",
                "encodedPassword",
                Provider.LOCAL);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserProfileResponse response = userService
                .updateProfile(
                        1L,
                        1L,
                        updateRequest("newNickname", null));

        assertThat(response.nickname()).isEqualTo("newNickname");
    }

    @Test
    void updateProfile_이미지가_null이면_기존_이미지를_유지한다() {
        User user = createUser(
                1L,
                "nickname",
                "existing.jpg",
                "encodedPassword",
                Provider.LOCAL);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserProfileResponse response = userService
                .updateProfile(
                        1L,
                        1L,
                        updateRequest("newNickname", null));

        assertThat(response.nickname()).isEqualTo("newNickname");
        assertThat(response.profileImage()).isEqualTo("existing.jpg");
    }

    @Test
    void updateProfile_이메일을_변경한다() {
        User user = createUser(
                1L,
                "oldNickname",
                "old.jpg",
                "encodedPassword",
                Provider.LOCAL);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.existsByEmail("new@example.com")).willReturn(false);

        UserProfileResponse response = userService
                .updateProfile(1L, 1L, updateEmailRequest("new@example.com"));
        assertThat(response.email()).isEqualTo("new@example.com");
    }

    @Test
    void updateProfile_중복된_이메일이면_예외발생() {
        User user = createUser(
                1L,
                "nickname",
                "image.jpg",
                "encodedPassword",
                Provider.LOCAL
        );
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.existsByEmail("dup@example.com")).willReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(
                1L, 1L, updateEmailRequest("dup@example.com")))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("이미 사용 중인 이메일입니다.");
    }

    @Test
    void updateProfile_비밀번호를_변경한다() {
        User user = createUser(
                1L,
                "nickname",
                "image.jpg",
                "encodedOldPassword",
                Provider.LOCAL
        );
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(
                "oldPassword123", "encodedOldPassword")).willReturn(true);
        given(passwordEncoder.encode("newPassword123")).willReturn("encodedNewPassword");

        userService.updateProfile(
                        1L,
                        1L,
                        updatePasswordRequest(
                                "oldPassword123", "newPassword123"));

        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    void updateProfile_현재_비밀번호가_틀리면_예외발생(){
        User user = createUser(
                1L,
                "nickname",
                "image.jpg",
                "encodedPassword",
                Provider.LOCAL
        );
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(
                "wrongPassword", "encodedPassword")).willReturn(false);

        assertThatThrownBy(() -> userService.updateProfile(
                1L,1L, updatePasswordRequest(
                        "wrongPassword","newPassword123")))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessageContaining("현재 비밀번호가 일치하지 않습니다.");
    }

    @Test
    void updateProfile_카카오_사용자는_비밀번호_변경_불가(){
        User user = createUser(
                1L,
                "nickname",
                "image.jpg",
                null,
                Provider.KAKAO
        );
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateProfile(
                1L,1L, updatePasswordRequest(
                        "any","newPassword123")))
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessageContaining("카카오 계정은 비밀번호를 변경할 수 없습니다.");
    }

    @Test
    void updateProfile_타인_프로필_수정시_AccessDeniedException_발생() {
        assertThatThrownBy(() -> userService.updateProfile(
                2L, 1L, updateRequest("nickname",null)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateProfile_존재하지_않는_유저이면_예외발생() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(
                999L, 999L, updateRequest("nickname",null)))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }
}
