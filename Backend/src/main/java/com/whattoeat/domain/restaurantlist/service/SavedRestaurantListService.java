package com.whattoeat.domain.restaurantlist.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SavedRestaurantListService {

    private final UserRepository userRepository;
    private final RestaurantListRepository restaurantListRepository;
    private final SavedRestaurantListRepository savedRestaurantListRepository;

    // 레스토랑 리스트 저장(연결)
    public void save(Long userId, Long restaurantListId) {
        // 저장하는 사용자 존재하는지 확인
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        // 저장하려는 원본 레스토랑 리스트가 존재하는지 확인
        RestaurantList restaurantList = restaurantListRepository.findById(restaurantListId).orElseThrow(() -> new ListNotFoundException(restaurantListId));

        if(restaurantList.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 리스트는 저장할 수 없습니다.");
        }

        // 같은 사용자가 같은 리스트 이미 저장했는지 확인 (중복 방지)
        if(savedRestaurantListRepository.existsByUserIdAndRestaurantListId(userId, restaurantListId)) {
            throw new AlreadySavedRestaurantListException();
        }

        // 사용자의 원본 리스트를 연결하는 저장 기록 생성
        SavedRestaurantList savedRestaurantList = new SavedRestaurantList(user, restaurantList);

        // 저장 기록 DB에 저장
        savedRestaurantListRepository.save(savedRestaurantList);
    }


    // 레스토랑 리스트 저장 취소
    public void unsave(Long userId, Long restaurantListId) {
        // 현재 사용자가 해당 레스토랑 리스트를 저장한 기록이 있는지 조회
        SavedRestaurantList savedRestaurantList = savedRestaurantListRepository.findByUserIdAndRestaurantListId(userId, restaurantListId)
                .orElseThrow(() -> new ListNotFoundException(restaurantListId));

        // 저장 기록만 삭제 (원본 레스토랑 리스트는 그대로 유지)
        savedRestaurantListRepository.delete(savedRestaurantList);
    }

    // 내가 저장한 레스토랑 리스트 목록 조회
    @Transactional(readOnly = true)
    public Page<SavedRestaurantListResponse> findMySavedLists(Long userId, Pageable pageable) {
        return savedRestaurantListRepository.findByUserId(userId, pageable)
                .map(SavedRestaurantListResponse::from);
    }

    // 특정 레스토랑 리스트를 현재 사용자가 저장했는지 확인 (프론트에서 상세 화면 "저장됨/저장안됨" 상태를 표시할때 사용)
    @Transactional(readOnly = true)
    public boolean isSaved(Long userId, Long restaurantListId) {
        return savedRestaurantListRepository.existsByUserIdAndRestaurantListId(userId, restaurantListId);
    }

}
