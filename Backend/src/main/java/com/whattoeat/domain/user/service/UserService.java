package com.whattoeat.domain.user.service;

import com.whattoeat.domain.follow.repository.FollowRepository;
import com.whattoeat.domain.user.dto.UpdateProfileRequest;
import com.whattoeat.domain.user.dto.UserProfileResponse;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import com.whattoeat.global.exception.DuplicateEmailException;
import com.whattoeat.global.exception.DuplicateNicknameException;
import com.whattoeat.global.exception.PasswordMismatchException;
import com.whattoeat.global.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getUser(Long targetId, Long currentUserId) {
        User user = userRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException(targetId));

        boolean isFollowing = followRepository
                .existsByFollower_IdAndFollowing_Id(currentUserId, targetId);
        return UserProfileResponse.from(user, currentUserId, isFollowing);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long id, Long currentUserId, UpdateProfileRequest request) {

        if (!id.equals(currentUserId)) {
            throw new AccessDeniedException("본인의 프로필만 수정 가능합니다.");
        }

        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (request.nickname() != null && !request.nickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.nickname())) {
                throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
            }
        }

        user.updateProfile(request.nickname(),user.getProfileImage());

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
            }
            user.updateEmail(request.email());

            if (user.getProvider() == Provider.LOCAL) {
                user.updateLoginId(request.email());
            }
        }

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (user.getProvider() == Provider.KAKAO) {
                throw new PasswordMismatchException("카카오 계정은 비밀번호를 변경할 수 없습니다.");
            }
            if (request.currentPassword() == null ||
                    !passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                throw new PasswordMismatchException("현재 비밀번호가 일치하지 않습니다.");
            }
            user.updatePassword(passwordEncoder.encode(request.newPassword()));
        }

        return UserProfileResponse.from(user, currentUserId, false);
    }

    @Transactional
    public User kakaoLoginOrSignUp(String kakaoId, String nickname, String profileImg, String email) {
        return userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .kakaoId(kakaoId)
                                .nickname(nickname)
                                .profileImage(profileImg)
                                .email(email)
                                .provider(Provider.KAKAO)
                                .role(Role.USER)
                                .build()
                ));
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional
    public UserProfileResponse updateProfileImage(Long userId, String imageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.updateProfile(user.getNickname(), imageUrl);

        return UserProfileResponse.from(user, userId, false);
    }

}
