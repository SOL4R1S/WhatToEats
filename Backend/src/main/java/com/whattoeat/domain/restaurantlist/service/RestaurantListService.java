package com.whattoeat.domain.restaurantlist.service;

import com.whattoeat.domain.restaurant.entity.MoodTag;
import com.whattoeat.domain.restaurant.entity.Restaurant;
import com.whattoeat.domain.restaurant.repository.RestaurantRepository;
import com.whattoeat.domain.restaurantlist.entity.RestaurantList;
import com.whattoeat.domain.restaurantlist.entity.RestaurantListItem;
import com.whattoeat.domain.restaurantlist.repository.RestaurantListItemRepository;
import com.whattoeat.domain.restaurantlist.repository.RestaurantListRepository;
import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import com.whattoeat.global.exception.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantListService {
    private final RestaurantListRepository restaurantListRepository;
    private final UserRepository userRepository;
    private final RestaurantListItemRepository restaurantListItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final EntityManager entityManager;

    public RestaurantList create(Long userId, String title, String description, MoodTag moodTag) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        RestaurantList restaurantList = new RestaurantList(
                user,
                title,
                description,
                moodTag
        );

        return restaurantListRepository.save(restaurantList);
    }

    // 맛집 리스트 다건 조회
    @Transactional(readOnly = true)
    public Page<RestaurantList> findAllByUserId(Long userId, Pageable pageable) {
        return restaurantListRepository.findByUserIdOrderByIdDesc(userId, pageable);
    }


    // 맛집 리스트 단건 조회
    @Transactional(readOnly = true)
    public RestaurantList findByIdAndUserId(Long id, Long userId) {
        return restaurantListRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ListNotFoundException(id));
    }

    public RestaurantListItem addItem(
            Long userId,
            Long listId,
            Long restaurantId,
            String memo,
            Integer orderIndex
    ) {
        RestaurantList restaurantList = restaurantListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ListNotFoundException(listId));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        // 식당리스트아이템에 식당 중복으로 넣는지 체크 / 수정때는 메모, 순서만 바꾸기 때문에 삭제 후 다시 추가해야함
        if(restaurantListItemRepository.existsByRestaurantListIdAndRestaurantId(listId, restaurantId)) {
            throw new DuplicateRestaurantListItemException(restaurantId);
        }

        int nextOrderIndex;

        // 인덱스 값 없을 경우 처리 N + 1
        if(orderIndex == null) {
            Integer maxOrderIndex = restaurantListItemRepository.findMaxOrderIndexByListId(listId);
            nextOrderIndex = maxOrderIndex == null ? 1 : maxOrderIndex + 1;
        } else {
            nextOrderIndex = orderIndex;

            restaurantListItemRepository.incOrderIndex(
                    listId,
                    nextOrderIndex
            );
        }

        RestaurantListItem restaurantListItem = new RestaurantListItem(
                restaurantList,
                restaurant,
                memo,
                nextOrderIndex
        );

        return restaurantListItemRepository.save(restaurantListItem);
    }

    public RestaurantListItem updateItem(
            Long listId,
            Long itemId,
            Long userId,
            Integer orderIndex,
            String memo
    ) {
        // 식당 있는지 확인
        restaurantListRepository.findByIdAndUserId(listId, userId)
                .orElseThrow(() -> new ListNotFoundException(listId));

        RestaurantListItem item =  restaurantListItemRepository.findListItem(itemId, listId, userId)
                .orElseThrow(() -> new RestaurantListItemNotFoundException(itemId));

        item.updateListItem(orderIndex, memo);

        return item;
    }

    // 식당 리스트 아이템 삭제
    public void deleteItem(Long listId, Long itemId, Long userId) {
        RestaurantListItem item = restaurantListItemRepository.findListItem(itemId, listId, userId)
                .orElseThrow(() -> new RestaurantListItemNotFoundException(itemId));

        restaurantListItemRepository.delete(item);
    }

    // ============================== 전체 조회 =================================
    // 전체 맛집 리스트 다건 조회
    @Transactional(readOnly = true)
    public Page<RestaurantList> findAll(Pageable pageable) {
        return restaurantListRepository.findAll(pageable);
    }

    // 전체 식당 리스트 단건 조회
    @Transactional(readOnly = true)
    public RestaurantList findById(Long id) {
        return restaurantListRepository.findById(id)
                .orElseThrow(() -> new ListNotFoundException(id));
    }

    // ================= 리스트 저장 ====================

    // ================= 리스트 복사 ====================
    public RestaurantList copyList(Long userId, Long id) {
        // 복사하는 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 원본 리스트 조회 - 다른 사람 리스트도 복사할 수 있어야해서 userId 조건을 걸지는 않음
        RestaurantList originalList = restaurantListRepository.findById(id)
                .orElseThrow(() -> new ListNotFoundException(id));

        // 리스트 복사
        RestaurantList copyList = restaurantListRepository.save(
                new RestaurantList(
                        user,
                        originalList.getTitle(),
                        originalList.getDescription(),
                        originalList.getMoodTag()
                )
        );

        // 원본 리스트 아이템 조회
        List<RestaurantListItem> originalListItems = restaurantListItemRepository.findItemsByListId(id);

        // 원본 아이템들을 새 리스트의 아이템으로 복사
        for(RestaurantListItem originalListItem : originalListItems) {
            RestaurantListItem copyListItem = new RestaurantListItem(
                    copyList,
                    originalListItem.getRestaurant(),
                    originalListItem.getMemo(),
                    originalListItem.getOrderIndex()
            );

            restaurantListItemRepository.save(copyListItem);
        }
        restaurantListItemRepository.flush();
        entityManager.clear();

        return restaurantListRepository.findByIdWithItems(copyList.getId())
                .orElseThrow(() -> new ListNotFoundException(copyList.getId()));
    }

    public RestaurantList update(
            Long listId,
            Long userId,
            String title,
            String description,
            MoodTag moodTag
    ) {
        RestaurantList restaurantList = restaurantListRepository.findById(listId)
                .orElseThrow(() ->
                        new ListNotFoundException(listId)
                );

        if (!restaurantList.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 리스트만 수정할 수 있습니다.");
        }

        restaurantList.update(
                title,
                description,
                moodTag
        );

        return restaurantList;
    }

    @Transactional(readOnly = true)
    public Page<RestaurantList> findAllExceptUser(
            Long userId,
            Pageable pageable
    ) {
        return restaurantListRepository.findByUserIdNot(userId, pageable);
    }
}
