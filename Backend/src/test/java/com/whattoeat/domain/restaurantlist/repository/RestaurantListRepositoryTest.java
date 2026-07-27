package com.whattoeat.domain.restaurantlist.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.whattoeat.domain.restaurant.entity.MoodTag;
import com.whattoeat.domain.restaurantlist.entity.RestaurantList;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.global.config.JpaConfig;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import(JpaConfig.class)
class RestaurantListRepositoryTest {

    @Autowired
    private RestaurantListRepository restaurantListRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User createAndSaveUser(String loginId, String nickname, String email) {
        User user = User.builder()
                .loginId(loginId)
                .nickname(nickname)
                .email(email)
                .provider(Provider.LOCAL)
                .role(Role.USER)
                .build();

        return entityManager.persistAndFlush(user);
    }

    private RestaurantList createAndSaveRestaurantList(User user, String title) {
        RestaurantList restaurantList = new RestaurantList(
                user,
                title,
                "설명",
                MoodTag.DATE
        );

        return entityManager.persistAndFlush(restaurantList);
    }

    @Test
    void findByUserIdOrderByIdDesc_성공() {
        User user = createAndSaveUser("user1", "nick1", "user1@test.com");

        createAndSaveRestaurantList(user, "리스트1");
        createAndSaveRestaurantList(user, "리스트2");

        Page<RestaurantList> result =
                restaurantListRepository.findByUserIdOrderByIdDesc(
                        user.getId(),
                        PageRequest.of(0, 10)
                );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting("title")
                .containsExactly("리스트2", "리스트1");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    void findByUserIdOrderByIdDesc_다른_유저의_리스트는_조회되지_않는다() {
        User user1 = createAndSaveUser("user1", "nick1", "user1@test.com");
        User user2 = createAndSaveUser("user2", "nick2", "user2@test.com");

        createAndSaveRestaurantList(user1, "user1 리스트");
        createAndSaveRestaurantList(user2, "user2 리스트");

        Page<RestaurantList> result =
                restaurantListRepository.findByUserIdOrderByIdDesc(
                        user1.getId(),
                        PageRequest.of(0, 10)
                );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("user1 리스트");

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    void findByIdAndUserId_성공() {
        User user = createAndSaveUser("user1", "nick1", "user1@test.com");
        RestaurantList restaurantList = createAndSaveRestaurantList(user, "리스트1");

        Optional<RestaurantList> result =
                restaurantListRepository.findByIdAndUserId(restaurantList.getId(), user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("리스트1");
    }

    @Test
    void findByIdAndUserId_다른_유저면_조회되지_않는다() {
        User user1 = createAndSaveUser("user1", "nick1", "user1@test.com");
        User user2 = createAndSaveUser("user2", "nick2", "user2@test.com");

        RestaurantList restaurantList = createAndSaveRestaurantList(user1, "리스트1");

        Optional<RestaurantList> result =
                restaurantListRepository.findByIdAndUserId(restaurantList.getId(), user2.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void save_시_createdAt이_자동_설정된다() {
        User user = createAndSaveUser("user1", "nick1", "user1@test.com");
        RestaurantList restaurantList = createAndSaveRestaurantList(user, "리스트1");

        assertThat(restaurantList.getCreatedAt()).isNotNull();
    }
}
