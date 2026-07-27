package com.whattoeat.domain.restaurantlist.controller;

import com.whattoeat.domain.restaurantlist.dto.SavedRestaurantListResponse;
import com.whattoeat.domain.restaurantlist.dto.SavedRestaurantListStatusResponse;
import com.whattoeat.domain.restaurantlist.service.SavedRestaurantListService;
import com.whattoeat.global.rsData.RsData;
import com.whattoeat.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/restaurant_lists")
public class SavedRestaurantListController {

    private final SavedRestaurantListService savedRestaurantListService;

    @PostMapping("/{id}/save")
    @Operation(summary = "맛집 리스트 저장")
    public ResponseEntity<RsData<Void>> save(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = userDetails.getUserId();

        savedRestaurantListService.save(userId, id);

        return ResponseEntity.ok(
                RsData.success(null, "레스토랑 리스트를 저장했습니다.")
        );
    }

    @DeleteMapping("/{id}/save")
    @Operation(summary = "맛집 리스트 저장 취소")
    public ResponseEntity<RsData<Void>> unsave(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = userDetails.getUserId();

        savedRestaurantListService.unsave(userId, id);

        return ResponseEntity.ok(
                RsData.success(null, "레스토랑 리스트 저장을 취소했습니다.")
        );
    }

    @GetMapping("/saved")
    @Operation(summary = "내가 저장한 리스트 조회")
    public ResponseEntity<RsData<Page<SavedRestaurantListResponse>>> findMySavedLists(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = userDetails.getUserId();

        Pageable pageable = PageRequest.of(page, size);

        Page<SavedRestaurantListResponse> response =
                savedRestaurantListService.findMySavedLists(userId, pageable);

        return ResponseEntity.ok(
                RsData.success(response, "저장한 레스토랑 리스트 조회가 완료되었습니다.")
        );
    }

    @GetMapping("/{id}/saved")
    public ResponseEntity<RsData<SavedRestaurantListStatusResponse>> isSaved(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = userDetails.getUserId();

        boolean saved = savedRestaurantListService.isSaved(userId, id);

        SavedRestaurantListStatusResponse response =
                SavedRestaurantListStatusResponse.of(id, saved);

        String message = saved
                ? "저장한 레스토랑 리스트입니다."
                : "저장하지 않은 레스토랑 리스트입니다.";

        return ResponseEntity.ok(
                RsData.success(response, message)
        );
    }
}
