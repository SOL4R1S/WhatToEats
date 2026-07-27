package com.whattoeat.domain.user.repository;

import com.whattoeat.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByKakaoId(String kakaoId);

    Optional<User> findByEmail(String email);

    boolean existsByLoginId(String loginId);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);
}
