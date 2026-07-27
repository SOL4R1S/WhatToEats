package com.whattoeat.domain.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.whattoeat.domain.follow.entity.Follow;
import com.whattoeat.domain.follow.repository.FollowRepository;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import com.whattoeat.global.exception.AlreadyFollowingException;
import com.whattoeat.global.exception.FollowNotFoundException;
import com.whattoeat.global.exception.SelfFollowNotAllowedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FollowServiceTest {

    @MockitoBean
    ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("팔로우 성공")
    void follow() {
        User follower = saveUser("follower");
        User following = saveUser("following");

        Follow follow = followService.follow(follower.getId(), following.getId());

        assertThat(follow.getId()).isNotNull();
        assertThat(follow.getFollower().getId()).isEqualTo(follower.getId());
        assertThat(follow.getFollowing().getId()).isEqualTo(following.getId());
        assertThat(followRepository.existsByFollower_IdAndFollowing_Id(follower.getId(), following.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("자기 자신은 팔로우할 수 없다")
    void followSelf() {
        User user = saveUser("user");

        assertThatThrownBy(() -> followService.follow(user.getId(), user.getId()))
                .isInstanceOf(SelfFollowNotAllowedException.class)
                .hasMessage("자기 자신을 팔로우할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 팔로우 중이면 실패한다")
    void followAlreadyFollowing() {
        User follower = saveUser("follower");
        User following = saveUser("following");
        followService.follow(follower.getId(), following.getId());

        assertThatThrownBy(() -> followService.follow(follower.getId(), following.getId()))
                .isInstanceOf(AlreadyFollowingException.class)
                .hasMessage("이미 팔로우 중인 사용자입니다.");
    }

    @Test
    @DisplayName("언팔로우 성공")
    void unfollow() {
        User follower = saveUser("follower");
        User following = saveUser("following");
        followService.follow(follower.getId(), following.getId());

        followService.unfollow(follower.getId(), following.getId());

        assertThat(followRepository.existsByFollower_IdAndFollowing_Id(follower.getId(), following.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("팔로우 관계가 없으면 언팔로우에 실패한다")
    void unfollowNotFound() {
        User follower = saveUser("follower");
        User following = saveUser("following");

        assertThatThrownBy(() -> followService.unfollow(follower.getId(), following.getId()))
                .isInstanceOf(FollowNotFoundException.class)
                .hasMessage("팔로우 관계가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("팔로잉 목록을 조회한다")
    void getFollowings() {
        User follower = saveUser("follower");
        User following1 = saveUser("following1");
        User following2 = saveUser("following2");
        followService.follow(follower.getId(), following1.getId());
        followService.follow(follower.getId(), following2.getId());

        Page<Follow> followings = followService.getFollowings(follower.getId(), PageRequest.of(0, 10));

        assertThat(followings.getTotalElements()).isEqualTo(2);
        assertThat(followings.getContent())
                .extracting(follow -> follow.getFollowing().getId())
                .containsExactlyInAnyOrder(following1.getId(), following2.getId());
    }

    @Test
    @DisplayName("팔로워 목록을 조회한다")
    void getFollowers() {
        User follower1 = saveUser("follower1");
        User follower2 = saveUser("follower2");
        User following = saveUser("following");
        followService.follow(follower1.getId(), following.getId());
        followService.follow(follower2.getId(), following.getId());

        Page<Follow> followers = followService.getFollowers(following.getId(), PageRequest.of(0, 10));

        assertThat(followers.getTotalElements()).isEqualTo(2);
        assertThat(followers.getContent())
                .extracting(follow -> follow.getFollower().getId())
                .containsExactlyInAnyOrder(follower1.getId(), follower2.getId());
    }

    private User saveUser(String name) {
        return userRepository.save(User.builder()
                .loginId(name)
                .password("password")
                .kakaoId(name + "-kakao")
                .nickname(name)
                .email(name + "@test.com")
                .role(Role.USER)
                .provider(Provider.LOCAL)
                .build());
    }
}
