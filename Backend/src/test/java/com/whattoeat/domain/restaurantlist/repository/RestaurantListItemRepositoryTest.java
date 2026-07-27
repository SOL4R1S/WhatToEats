package com.whattoeat.domain.restaurantlist.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.whattoeat.domain.restaurant.entity.Category;
import com.whattoeat.domain.restaurant.entity.MoodTag;
import com.whattoeat.domain.restaurant.entity.Restaurant;
import com.whattoeat.domain.restaurantlist.entity.RestaurantList;
import com.whattoeat.domain.restaurantlist.entity.RestaurantListItem;
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
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@Import(JpaConfig.class)
class RestaurantListItemRepositoryTest {

    @Autowired
    private RestaurantListItemRepository restaurantListItemRepository;

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

    private Restaurant createAndSaveRestaurant(String name) {
        try {
            var constructor = Restaurant.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            Restaurant restaurant = constructor.newInstance();

            ReflectionTestUtils.setField(restaurant, "name", name);
            ReflectionTestUtils.setField(restaurant, "category", Category.JAPANESE);
            ReflectionTestUtils.setField(restaurant, "address", "서울시 강남구 역삼동");
            ReflectionTestUtils.setField(restaurant, "roadAddress", "서울시 강남구 테헤란로 123");
            ReflectionTestUtils.setField(restaurant, "phone", "02-0000-0000");
            ReflectionTestUtils.setField(restaurant, "region1", "서울특별시");
            ReflectionTestUtils.setField(restaurant, "region2", "강남구");
            ReflectionTestUtils.setField(restaurant, "region3", "역삼동");
            ReflectionTestUtils.setField(restaurant, "lat", 37.5665);
            ReflectionTestUtils.setField(restaurant, "lng", 126.9780);
            ReflectionTestUtils.setField(restaurant, "kakaoPlaceId", "kakao-place-1");

            return entityManager.persistAndFlush(restaurant);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private RestaurantListItem createAndSaveItem(
            RestaurantList restaurantList,
            Restaurant restaurant,
            String memo,
            Integer orderIndex
    ) {
        RestaurantListItem item = new RestaurantListItem(
                restaurantList,
                restaurant,
                memo,
                orderIndex
        );

        return entityManager.persistAndFlush(item);
    }

    @Test
    void findListItem_성공() {
        User user = createAndSaveUser("user1", "nick1", "user1@test.com");

        RestaurantList restaurantList = createAndSaveRestaurantList(user, "맛집 리스트");
        Restaurant restaurant = createAndSaveRestaurant("초밥집");

        RestaurantListItem item = createAndSaveItem(
                restaurantList,
                restaurant,
                "한줄평",
                1
        );

        Optional<RestaurantListItem> result =
                restaurantListItemRepository.findListItem(
                        item.getId(),
                        restaurantList.getId(),
                        user.getId()
                );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(item.getId());
        assertThat(result.get().getRestaurantList().getId()).isEqualTo(restaurantList.getId());
        assertThat(result.get().getRestaurant().getId()).isEqualTo(restaurant.getId());
        assertThat(result.get().getMemo()).isEqualTo("한줄평");
        assertThat(result.get().getOrderIndex()).isEqualTo(1);
    }

    @Test
    void findListItem_다른_유저의_아이템이면_조회되지_않는다() {
        User user1 = createAndSaveUser("user1", "nick1", "user1@test.com");
        User user2 = createAndSaveUser("user2", "nick2", "user2@test.com");

        RestaurantList restaurantList = createAndSaveRestaurantList(user1, "user1 리스트");
        Restaurant restaurant = createAndSaveRestaurant("초밥집");

        RestaurantListItem item = createAndSaveItem(
                restaurantList,
                restaurant,
                "한줄평",
                1
        );

        Optional<RestaurantListItem> result =
                restaurantListItemRepository.findListItem(
                        item.getId(),
                        restaurantList.getId(),
                        user2.getId()
                );

        assertThat(result).isEmpty();
    }

    @Test
    void findListItem_다른_리스트의_아이템이면_조회되지_않는다() {
        User user = createAndSaveUser("user1", "nick1", "user1@test.com");

        RestaurantList list1 = createAndSaveRestaurantList(user, "리스트1");
        RestaurantList list2 = createAndSaveRestaurantList(user, "리스트2");

        Restaurant restaurant = createAndSaveRestaurant("초밥집");

        RestaurantListItem item = createAndSaveItem(
                list1,
                restaurant,
                "한줄평",
                1
        );

        Optional<RestaurantListItem> result =
                restaurantListItemRepository.findListItem(
                        item.getId(),
                        list2.getId(),
                        user.getId()
                );

        assertThat(result).isEmpty();
    }

    @Test
    void 같은_리스트에_같은_식당은_중복_저장할_수_없다() {
        User user = createAndSaveUser("user1", "nick1", "user1@test.com");

        RestaurantList restaurantList = createAndSaveRestaurantList(user, "맛집 리스트");
        Restaurant restaurant = createAndSaveRestaurant("초밥집");

        createAndSaveItem(
                restaurantList,
                restaurant,
                "첫 번째 한줄평",
                1
        );

        RestaurantListItem duplicate = new RestaurantListItem(
                restaurantList,
                restaurant,
                "중복 한줄평",
                2
        );

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .isInstanceOf(org.hibernate.exception.ConstraintViolationException.class);
    }

    @Test
    void save_시_createdAt이_자동_설정된다() {
        User user = createAndSaveUser("user1", "nick1", "user1@test.com");

        RestaurantList restaurantList = createAndSaveRestaurantList(user, "맛집 리스트");
        Restaurant restaurant = createAndSaveRestaurant("초밥집");

        RestaurantListItem item = createAndSaveItem(
                restaurantList,
                restaurant,
                "한줄평",
                1
        );

        assertThat(item.getCreatedAt()).isNotNull();
    }
}
