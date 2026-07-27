package com.whattoeat.domain.restaurantlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.whattoeat.domain.restaurant.entity.MoodTag;
import com.whattoeat.domain.restaurant.entity.Restaurant;
import com.whattoeat.domain.restaurant.repository.RestaurantRepository;
import com.whattoeat.domain.restaurantlist.entity.RestaurantList;
import com.whattoeat.domain.restaurantlist.entity.RestaurantListItem;
import com.whattoeat.domain.restaurantlist.repository.RestaurantListItemRepository;
import com.whattoeat.domain.restaurantlist.repository.RestaurantListRepository;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import com.whattoeat.global.exception.DuplicateRestaurantListItemException;
import com.whattoeat.global.exception.ListNotFoundException;
import com.whattoeat.global.exception.RestaurantListItemNotFoundException;
import com.whattoeat.global.exception.RestaurantNotFoundException;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RestaurantListItemServiceTest {
    @Mock
    private EntityManager entityManager;

    @Mock
    private RestaurantListRepository restaurantListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantListItemRepository restaurantListItemRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantListService restaurantListService;

    private User createUser() {
        return Mockito.mock(User.class);
    }

    private RestaurantList createRestaurantList(Long id, User user) {
        RestaurantList restaurantList = new RestaurantList(
                user,
                "맛집 리스트",
                "설명",
                MoodTag.DATE
        );

        ReflectionTestUtils.setField(restaurantList, "id", id);

        return restaurantList;
    }

    private Restaurant createRestaurant(Long id, String name) {
        Restaurant restaurant = Mockito.mock(Restaurant.class);

        given(restaurant.getId()).willReturn(id);
        given(restaurant.getName()).willReturn(name);

        return restaurant;
    }

    private RestaurantListItem createRestaurantListItem(
            Long id,
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

        ReflectionTestUtils.setField(item, "id", id);

        return item;
    }

    @Test
    void addItem_성공() {
        User user = createUser();
        RestaurantList restaurantList = createRestaurantList(1L, user);
        Restaurant restaurant = createRestaurant(10L, "초밥집");

        RestaurantListItem savedItem = createRestaurantListItem(
                100L,
                restaurantList,
                restaurant,
                "한줄평",
                1
        );

        given(restaurantListRepository.findByIdAndUserId(1L, 1L))
                .willReturn(Optional.of(restaurantList));

        given(restaurantRepository.findById(10L))
                .willReturn(Optional.of(restaurant));

        given(restaurantListItemRepository.save(any(RestaurantListItem.class)))
                .willReturn(savedItem);

        RestaurantListItem result = restaurantListService.addItem(
                1L,
                1L,
                10L,
                "한줄평",
                1
        );

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getRestaurantList().getId()).isEqualTo(1L);
        assertThat(result.getRestaurant().getId()).isEqualTo(10L);
        assertThat(result.getRestaurant().getName()).isEqualTo("초밥집");
        assertThat(result.getMemo()).isEqualTo("한줄평");
        assertThat(result.getOrderIndex()).isEqualTo(1);
    }

    @Test
    void addItem_리스트가_없으면_예외발생() {
        given(restaurantListRepository.findByIdAndUserId(999L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantListService.addItem(
                1L,
                999L,
                10L,
                "한줄평",
                1
        ))
                .isInstanceOf(ListNotFoundException.class);
    }

    @Test
    void addItem_식당이_없으면_예외발생() {
        User user = createUser();
        RestaurantList restaurantList = createRestaurantList(1L, user);

        given(restaurantListRepository.findByIdAndUserId(1L, 1L))
                .willReturn(Optional.of(restaurantList));

        given(restaurantRepository.findById(999L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantListService.addItem(
                1L,
                1L,
                999L,
                "한줄평",
                1
        ))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @Test
    void updateItem_성공() {
        User user = createUser();
        RestaurantList restaurantList = createRestaurantList(1L, user);
        Restaurant restaurant = Mockito.mock(Restaurant.class);

        RestaurantListItem item = createRestaurantListItem(
                100L,
                restaurantList,
                restaurant,
                "기존 한줄평",
                1
        );

        given(restaurantListRepository.findByIdAndUserId(1L, 1L))
                .willReturn(Optional.of(restaurantList));

        given(restaurantListItemRepository.findListItem(100L, 1L, 1L))
                .willReturn(Optional.of(item));

        RestaurantListItem result = restaurantListService.updateItem(
                1L,
                100L,
                1L,
                2,
                "수정된 한줄평"
        );

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getMemo()).isEqualTo("수정된 한줄평");
        assertThat(result.getOrderIndex()).isEqualTo(2);
    }

    @Test
    void updateItem_리스트가_없으면_예외발생() {
        given(restaurantListRepository.findByIdAndUserId(999L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantListService.updateItem(
                999L,
                100L,
                1L,
                2,
                "수정된 한줄평"
        ))
                .isInstanceOf(ListNotFoundException.class);
    }

    @Test
    void updateItem_아이템이_없으면_예외발생() {
        User user = createUser();
        RestaurantList restaurantList = createRestaurantList(1L, user);

        given(restaurantListRepository.findByIdAndUserId(1L, 1L))
                .willReturn(Optional.of(restaurantList));

        given(restaurantListItemRepository.findListItem(999L, 1L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantListService.updateItem(
                1L,
                999L,
                1L,
                2,
                "수정된 한줄평"
        ))
                .isInstanceOf(RestaurantListItemNotFoundException.class);
    }

    @Test
    void addItem_같은_리스트에_같은_식당이면_예외발생() {
        User user = createUser();
        RestaurantList restaurantList = createRestaurantList(1L, user);
        Restaurant restaurant = Mockito.mock(Restaurant.class);

        given(restaurantListRepository.findByIdAndUserId(1L, 1L))
                .willReturn(Optional.of(restaurantList));

        given(restaurantRepository.findById(10L))
                .willReturn(Optional.of(restaurant));

        given(restaurantListItemRepository.existsByRestaurantListIdAndRestaurantId(1L, 10L))
                .willReturn(true);

        assertThatThrownBy(() -> restaurantListService.addItem(
                1L,
                1L,
                10L,
                "중복 한줄평",
                2
        ))
                .isInstanceOf(DuplicateRestaurantListItemException.class);
    }

    @Test
    void copyList_성공_아이템도_복사된다() {
        // given
        Long userId = 1L;
        Long originalListId = 10L;
        Long copyListId = 20L;

        User user = Mockito.mock(User.class);

        RestaurantList originalList = Mockito.mock(RestaurantList.class);
        given(originalList.getTitle()).willReturn("혼밥 맛집");
        given(originalList.getDescription()).willReturn("혼자 먹기 좋은 곳");
        given(originalList.getMoodTag()).willReturn(MoodTag.SOLO);

        RestaurantList savedCopyList = Mockito.mock(RestaurantList.class);
        given(savedCopyList.getId()).willReturn(copyListId);

        RestaurantList fetchedCopyList = Mockito.mock(RestaurantList.class);

        Restaurant restaurant = Mockito.mock(Restaurant.class);

        RestaurantListItem originalItem = Mockito.mock(RestaurantListItem.class);
        given(originalItem.getRestaurant()).willReturn(restaurant);
        given(originalItem.getMemo()).willReturn("한줄평");
        given(originalItem.getOrderIndex()).willReturn(1);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(restaurantListRepository.findById(originalListId)).willReturn(Optional.of(originalList));
        given(restaurantListRepository.save(any(RestaurantList.class))).willReturn(savedCopyList);
        given(restaurantListItemRepository.findItemsByListId(originalListId)).willReturn(List.of(originalItem));
        given(restaurantListRepository.findByIdWithItems(copyListId)).willReturn(Optional.of(fetchedCopyList));

        ArgumentCaptor<RestaurantListItem> itemCaptor = ArgumentCaptor.forClass(RestaurantListItem.class);

        // when
        RestaurantList result = restaurantListService.copyList(userId, originalListId);

        // then
        assertThat(result).isEqualTo(fetchedCopyList);

        verify(restaurantListItemRepository).save(itemCaptor.capture());
        verify(restaurantListItemRepository).flush();
        verify(entityManager).clear();
        verify(restaurantListRepository).findByIdWithItems(copyListId);

        RestaurantListItem copiedItem = itemCaptor.getValue();

        assertThat(copiedItem.getRestaurant()).isEqualTo(restaurant);
        assertThat(copiedItem.getMemo()).isEqualTo("한줄평");
        assertThat(copiedItem.getOrderIndex()).isEqualTo(1);
    }

}
