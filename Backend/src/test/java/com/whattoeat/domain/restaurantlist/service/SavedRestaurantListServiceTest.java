package com.whattoeat.domain.restaurantlist.service;

import com.whattoeat.domain.restaurant.entity.MoodTag;
import com.whattoeat.domain.restaurantlist.dto.SavedRestaurantListResponse;
import com.whattoeat.domain.restaurantlist.entity.RestaurantList;
import com.whattoeat.domain.restaurantlist.entity.SavedRestaurantList;
import com.whattoeat.domain.restaurantlist.repository.RestaurantListRepository;
import com.whattoeat.domain.restaurantlist.repository.SavedRestaurantListRepository;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import com.whattoeat.global.exception.AlreadySavedRestaurantListException;
import com.whattoeat.global.exception.ListNotFoundException;
import com.whattoeat.global.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SavedRestaurantListServiceTest {

    @Mock
    private SavedRestaurantListRepository savedRestaurantListRepository;

    @Mock
    private RestaurantListRepository restaurantListRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SavedRestaurantListService savedRestaurantListService;

    @Test
    void save_성공() {
        // given
        Long userId = 1L;
        Long restaurantListId = 10L;

        User user = Mockito.mock(User.class);
        User owner = Mockito.mock(User.class);
        RestaurantList restaurantList = Mockito.mock(RestaurantList.class);

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        given(restaurantListRepository.findById(restaurantListId))
                .willReturn(Optional.of(restaurantList));

        given(restaurantList.getUser())
                .willReturn(owner);

        given(owner.getId())
                .willReturn(2L);

        given(savedRestaurantListRepository
                .existsByUserIdAndRestaurantListId(userId, restaurantListId))
                .willReturn(false);

        // when
        savedRestaurantListService.save(userId, restaurantListId);

        // then
        then(savedRestaurantListRepository)
                .should()
                .save(any(SavedRestaurantList.class));
    }

    @Test
    void save_이미_저장한_리스트면_예외() {
        // given
        Long userId = 1L;
        Long restaurantListId = 10L;

        User user = Mockito.mock(User.class);
        User owner = Mockito.mock(User.class);
        RestaurantList restaurantList = Mockito.mock(RestaurantList.class);

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        given(restaurantListRepository.findById(restaurantListId))
                .willReturn(Optional.of(restaurantList));

        given(restaurantList.getUser())
                .willReturn(owner);

        given(owner.getId())
                .willReturn(2L);

        given(savedRestaurantListRepository
                .existsByUserIdAndRestaurantListId(userId, restaurantListId))
                .willReturn(true);

        // when & then
        assertThatThrownBy(
                () -> savedRestaurantListService.save(userId, restaurantListId)
        )
                .isInstanceOf(AlreadySavedRestaurantListException.class)
                .hasMessage("이미 저장한 리스트입니다.");

        then(savedRestaurantListRepository)
                .should(never())
                .save(any(SavedRestaurantList.class));
    }

    @Test
    void save_사용자가_없으면_예외() {
        // given
        Long userId = 1L;
        Long restaurantListId = 10L;

        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                () -> savedRestaurantListService.save(userId, restaurantListId)
        )
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: 1");

        then(restaurantListRepository)
                .should(never())
                .findById(anyLong());

        then(savedRestaurantListRepository)
                .should(never())
                .save(any(SavedRestaurantList.class));
    }

    @Test
    void save_레스토랑_리스트가_없으면_예외() {
        // given
        Long userId = 1L;
        Long restaurantListId = 10L;

        User user = Mockito.mock(User.class);

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        given(restaurantListRepository.findById(restaurantListId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                () -> savedRestaurantListService.save(userId, restaurantListId)
        )
                .isInstanceOf(ListNotFoundException.class)
                .hasMessage("리스트를 찾을 수 없습니다: 10");

        then(savedRestaurantListRepository)
                .should(never())
                .save(any(SavedRestaurantList.class));
    }

    @Test
    void unsave_성공() {
        // given
        Long userId = 1L;
        Long restaurantListId = 10L;

        SavedRestaurantList savedRestaurantList =
                Mockito.mock(SavedRestaurantList.class);

        given(savedRestaurantListRepository
                .findByUserIdAndRestaurantListId(userId, restaurantListId))
                .willReturn(Optional.of(savedRestaurantList));

        // when
        savedRestaurantListService.unsave(userId, restaurantListId);

        // then
        then(savedRestaurantListRepository)
                .should()
                .delete(savedRestaurantList);
    }

    @Test
    void unsave_저장한_리스트가_아니면_예외() {
        // given
        Long userId = 1L;
        Long restaurantListId = 10L;

        given(savedRestaurantListRepository
                .findByUserIdAndRestaurantListId(userId, restaurantListId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                () -> savedRestaurantListService.unsave(userId, restaurantListId)
        )
                .isInstanceOf(ListNotFoundException.class)
                .hasMessage("리스트를 찾을 수 없습니다: 10");

        then(savedRestaurantListRepository)
                .should(never())
                .delete(any(SavedRestaurantList.class));
    }

    @Test
    void isSaved_저장되어_있으면_true() {
        // given
        Long userId = 1L;
        Long restaurantListId = 10L;

        given(savedRestaurantListRepository
                .existsByUserIdAndRestaurantListId(userId, restaurantListId))
                .willReturn(true);

        // when
        boolean result =
                savedRestaurantListService.isSaved(userId, restaurantListId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isSaved_저장되어_있지_않으면_false() {
        // given
        Long userId = 1L;
        Long restaurantListId = 10L;

        given(savedRestaurantListRepository
                .existsByUserIdAndRestaurantListId(userId, restaurantListId))
                .willReturn(false);

        // when
        boolean result =
                savedRestaurantListService.isSaved(userId, restaurantListId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void findMySavedLists_성공() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        User owner = Mockito.mock(User.class);
        RestaurantList restaurantList = Mockito.mock(RestaurantList.class);
        SavedRestaurantList savedRestaurantList =
                Mockito.mock(SavedRestaurantList.class);

        Page<SavedRestaurantList> page =
                new PageImpl<>(
                        List.of(savedRestaurantList),
                        pageable,
                        1
                );

        given(savedRestaurantListRepository.findByUserId(userId, pageable))
                .willReturn(page);

        given(savedRestaurantList.getRestaurantList())
                .willReturn(restaurantList);

        given(savedRestaurantList.getCreatedAt())
                .willReturn(null);

        given(restaurantList.getId())
                .willReturn(10L);

        given(restaurantList.getUser())
                .willReturn(owner);

        given(restaurantList.getTitle())
                .willReturn("혼밥 맛집");

        given(restaurantList.getDescription())
                .willReturn("혼자 먹기 좋은 곳");

        given(restaurantList.getMoodTag())
                .willReturn(MoodTag.SOLO);

        given(restaurantList.getItems())
                .willReturn(List.of());

        given(owner.getId())
                .willReturn(2L);

        given(owner.getNickname())
                .willReturn("작성자");

        // when
        Page<SavedRestaurantListResponse> result =
                savedRestaurantListService.findMySavedLists(userId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);

        SavedRestaurantListResponse response =
                result.getContent().get(0);

        assertThat(response.listId()).isEqualTo(10L);
        assertThat(response.userId()).isEqualTo(2L);
        assertThat(response.nickname()).isEqualTo("작성자");
        assertThat(response.title()).isEqualTo("혼밥 맛집");
        assertThat(response.description()).isEqualTo("혼자 먹기 좋은 곳");
        assertThat(response.moodTag()).isEqualTo(MoodTag.SOLO);
        assertThat(response.items()).isEmpty();
        assertThat(response.savedAt()).isNull();
    }
}
