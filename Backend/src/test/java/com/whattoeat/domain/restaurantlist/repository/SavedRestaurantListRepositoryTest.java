package com.whattoeat.domain.restaurantlist.repository;

import com.whattoeat.domain.restaurant.entity.MoodTag;
import com.whattoeat.domain.restaurantlist.entity.RestaurantList;
import com.whattoeat.domain.restaurantlist.entity.SavedRestaurantList;
import com.whattoeat.domain.user.entity.Provider;
import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class SavedRestaurantListRepositoryTest {

    @Autowired
    private SavedRestaurantListRepository savedRestaurantListRepository;

    @Autowired
    private RestaurantListRepository restaurantListRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 ID와 레스토랑 리스트 ID로 저장 여부를 확인할 수 있다")
    void existsByUserIdAndRestaurantListId_저장되어있으면_true() {
        // given
        User user = userRepository.save(createUser("user1@test.com", "사용자1"));
        User owner = userRepository.save(createUser("owner@test.com", "작성자"));

        RestaurantList restaurantList = restaurantListRepository.save(
                createRestaurantList(owner, "혼밥 맛집", "혼자 먹기 좋은 곳")
        );

        savedRestaurantListRepository.save(
                new SavedRestaurantList(user, restaurantList)
        );

        // when
        boolean result = savedRestaurantListRepository.existsByUserIdAndRestaurantListId(
                user.getId(),
                restaurantList.getId()
        );

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("저장하지 않은 리스트는 저장 여부가 false다")
    void existsByUserIdAndRestaurantListId_저장되어있지않으면_false() {
        // given
        User user = userRepository.save(createUser("user1@test.com", "사용자1"));
        User owner = userRepository.save(createUser("owner@test.com", "작성자"));

        RestaurantList restaurantList = restaurantListRepository.save(
                createRestaurantList(owner, "데이트 맛집", "데이트하기 좋은 곳")
        );

        // when
        boolean result = savedRestaurantListRepository.existsByUserIdAndRestaurantListId(
                user.getId(),
                restaurantList.getId()
        );

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자 ID와 레스토랑 리스트 ID로 저장 기록을 조회할 수 있다")
    void findByUserIdAndRestaurantListId_성공() {
        // given
        User user = userRepository.save(createUser("user1@test.com", "사용자1"));
        User owner = userRepository.save(createUser("owner@test.com", "작성자"));

        RestaurantList restaurantList = restaurantListRepository.save(
                createRestaurantList(owner, "친구 맛집", "친구랑 가기 좋은 곳")
        );

        SavedRestaurantList savedRestaurantList = savedRestaurantListRepository.save(
                new SavedRestaurantList(user, restaurantList)
        );

        // when
        Optional<SavedRestaurantList> result =
                savedRestaurantListRepository.findByUserIdAndRestaurantListId(
                        user.getId(),
                        restaurantList.getId()
                );

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedRestaurantList.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getRestaurantList().getId()).isEqualTo(restaurantList.getId());
    }

    @Test
    @DisplayName("내가 저장한 레스토랑 리스트 목록을 조회할 수 있다")
    void findByUserId_성공() {
        // given
        User user = userRepository.save(createUser("user1@test.com", "사용자1"));
        User owner = userRepository.save(createUser("owner@test.com", "작성자"));

        RestaurantList restaurantList1 = restaurantListRepository.save(
                createRestaurantList(owner, "혼밥 맛집", "혼자 먹기 좋은 곳")
        );

        RestaurantList restaurantList2 = restaurantListRepository.save(
                createRestaurantList(owner, "데이트 맛집", "데이트하기 좋은 곳")
        );

        savedRestaurantListRepository.save(new SavedRestaurantList(user, restaurantList1));
        savedRestaurantListRepository.save(new SavedRestaurantList(user, restaurantList2));

        // when
        Page<SavedRestaurantList> result =
                savedRestaurantListRepository.findByUserId(user.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("같은 사용자가 같은 레스토랑 리스트를 중복 저장할 수 없다")
    void 같은_사용자가_같은_리스트를_중복저장하면_예외() {
        // given
        User user = userRepository.save(createUser("user1@test.com", "사용자1"));
        User owner = userRepository.save(createUser("owner@test.com", "작성자"));

        RestaurantList restaurantList = restaurantListRepository.save(
                createRestaurantList(owner, "혼밥 맛집", "혼자 먹기 좋은 곳")
        );

        savedRestaurantListRepository.save(new SavedRestaurantList(user, restaurantList));

        // when & then
        assertThatThrownBy(() -> {
            savedRestaurantListRepository.saveAndFlush(
                    new SavedRestaurantList(user, restaurantList)
            );
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private User createUser(String email, String nickname) {
        return new User(
                null,           // loginId
                null,           // password
                null,           // kakaoId
                nickname,       // nickname
                null,           // profileImage
                email,          // email
                Role.USER,      // role
                Provider.LOCAL  // provider
        );
    }

    private RestaurantList createRestaurantList(User user, String title, String description) {
        return new RestaurantList(
                user,
                title,
                description,
                MoodTag.SOLO
        );
    }
}
