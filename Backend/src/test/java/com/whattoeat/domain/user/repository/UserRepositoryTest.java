package com.whattoeat.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import com.whattoeat.global.config.JpaConfig;

@DataJpaTest
@Import(JpaConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User createAndSave(String loginId, String kakaoId, String email, String nickname) {
        User user = User.builder()
                .loginId(loginId)
                .kakaoId(kakaoId)
                .nickname(nickname)
                .email(email)
                .provider(loginId != null ? Provider.LOCAL : Provider.KAKAO)
                .build();
        return entityManager.persistAndFlush(user);
    }

    @Test
    void findByLoginId_성공() {
        createAndSave("user1", null, "user1@test.com", "nick1");

        Optional<User> result = userRepository.findByLoginId("user1");

        assertThat(result).isPresent();
        assertThat(result.get().getLoginId()).isEqualTo("user1");
        assertThat(result.get().getEmail()).isEqualTo("user1@test.com");
    }

    @Test
    void findByLoginId_없으면_empty() {
        Optional<User> result = userRepository.findByLoginId("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void findByKakaoId_성공() {
        createAndSave(null, "kakao123", "kakao@test.com", "kakaoNick");

        Optional<User> result = userRepository.findByKakaoId("kakao123");

        assertThat(result).isPresent();
        assertThat(result.get().getKakaoId()).isEqualTo("kakao123");
    }

    @Test
    void findByKakaoId_없으면_empty() {
        Optional<User> result = userRepository.findByKakaoId("notFound");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_성공() {
        createAndSave("user2", null, "find@test.com", "nick2");

        Optional<User> result = userRepository.findByEmail("find@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("find@test.com");
    }

    @Test
    void findByEmail_없으면_empty() {
        Optional<User> result = userRepository.findByEmail("nobody@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByLoginId_존재하면_true() {
        createAndSave("existUser", null, "exist@test.com", "nick3");

        assertThat(userRepository.existsByLoginId("existUser")).isTrue();
    }

    @Test
    void existsByLoginId_없으면_false() {
        assertThat(userRepository.existsByLoginId("nobody")).isFalse();
    }

    @Test
    void existsByNickname_존재하면_true() {
        createAndSave("user3", null, "user3@test.com", "uniqueNick");

        assertThat(userRepository.existsByNickname("uniqueNick")).isTrue();
    }

    @Test
    void existsByNickname_없으면_false() {
        assertThat(userRepository.existsByNickname("unknownNick")).isFalse();
    }

    @Test
    void save_시_createdAt이_자동_설정된다() {
        User user = createAndSave("user4", null, "user4@test.com", "nick4");

        assertThat(user.getCreatedAt()).isNotNull();
    }
}
