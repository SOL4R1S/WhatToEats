package com.whattoeat.domain.restaurantlist.controller;

import com.whattoeat.domain.restaurantlist.dto.RestaurantListRequest;
import com.whattoeat.domain.restaurantlist.dto.RestaurantListResponse;
import com.whattoeat.domain.restaurantlist.entity.RestaurantList;
import com.whattoeat.domain.restaurantlist.entity.RestaurantListItem;
import com.whattoeat.domain.restaurantlist.service.RestaurantListService;
import com.whattoeat.global.rsData.RsData;
import com.whattoeat.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lists")
@RequiredArgsConstructor
public class RestaurantListController {
    private final RestaurantListService restaurantListService;

    // ============================== 내 정보 조회 =================================
    // 맛집 리스트 등록
    @PostMapping
    @Operation(summary = "맛집 리스트 등록")
    public RsData<RestaurantListResponse.RestaurantLists> createRestaurantList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
           @Valid @RequestBody RestaurantListRequest.RestaurantList req
    ) {
        Long userId = userDetails.getUserId();

        RestaurantList restaurantList = restaurantListService.create(
                userId,
                req.title(),
                req.description(),
                req.moodTag()
        );

        return RsData.success(
                new RestaurantListResponse.RestaurantLists(restaurantList),
                "맛집 리스트가 생성되었습니다."
        );
    }

    // 맛집 리스트 다건 조회
    @GetMapping
    @Operation(summary = "맛집 리스트 다건 조회")
    public RsData<RestaurantListResponse.RestaurantListsResponse> getRestaurantLists(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = userDetails.getUserId();

        Pageable pageable = PageRequest.of(page, size);

        Page<RestaurantList> result = restaurantListService.findAllByUserId(userId, pageable);

        List<RestaurantListResponse.RestaurantLists> lists = result.getContent()
                .stream()
                .map(RestaurantListResponse.RestaurantLists::new)
                .toList();

        return RsData.success(
                new RestaurantListResponse.RestaurantListsResponse(
                        lists,
                        result.getTotalPages(),
                        result.getTotalElements()
                ),
                "맛집 리스트 목록 조회가 완료되었습니다."
        );
    }

    // 맛집 리스트 단건 조회
    @GetMapping("/{id}")
    @Operation(summary = "맛집 리스트 단건 조회")
    public RsData<RestaurantListResponse.RestaurantListDetail> getRestaurantList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = userDetails.getUserId();

        RestaurantList restaurantList = restaurantListService.findByIdAndUserId(id, userId);

        return RsData.success(
                new RestaurantListResponse.RestaurantListDetail(restaurantList),
        "맛집 리스트 조회가 완료되었습니다."
        );
    }


    // ----------- RestaurantListItem -------------

    // 맛집 리스트 아이템 등록
    @PostMapping("/{id}/items")
    @Operation(summary = "맛집 리스트 아이템 등록")
    public RsData<RestaurantListResponse.RestaurantListItemDetail> createRestaurantListItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long listId,
            @Valid @RequestBody RestaurantListRequest.RestaurantListItem req
    ) {
        Long userId = userDetails.getUserId();

        RestaurantListItem item = restaurantListService.addItem(
                userId,
                listId,
                req.restaurantId(),
                req.memo(),
                req.orderIndex()
        );

        return RsData.success(
                new RestaurantListResponse.RestaurantListItemDetail(item),
                "맛집 리스트에 식당이 추가되었습니다."
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "식당 리스트 기본 정보 수정")
    public RsData<RestaurantListResponse.RestaurantListDetail> updateRestaurantList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RestaurantListRequest.RestaurantList req
    ) {
        Long userId = userDetails.getUserId();

        RestaurantList restaurantList = restaurantListService.update(
                id,
                userId,
                req.title(),
                req.description(),
                req.moodTag()
        );

        return RsData.success(
                new RestaurantListResponse.RestaurantListDetail(restaurantList),
                "리스트 정보가 변경되었습니다."
        );
    }


    @PutMapping("/{id}/items/{itemId}")
    @Operation(summary = "식당 리스트 아이템 수정(순서/메모)")
    public RsData<RestaurantListResponse.RestaurantListItemDetail> updateRestaurantListItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody RestaurantListRequest.RestaurantListItem req
    ) {
        Long userId = userDetails.getUserId();

        RestaurantListItem item = restaurantListService.updateItem(
                id, // listId
                itemId,
                userId,
                req.orderIndex(),
                req.memo()
        );

        return RsData.success(
                new RestaurantListResponse.RestaurantListItemDetail(item),
                "리스트 아이템 정보가 변경되었습니다."
        );
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Operation(summary = "맛집 리스트 아이템 삭제")
    public RsData<Void> deleteRestaurantListItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id, // listId
            @PathVariable Long itemId
    ) {
        Long userId = userDetails.getUserId();

        // 나중에 인증 추가 사용자일 경우에만 삭제가능하도록 수정
        restaurantListService.deleteItem(id, itemId, userId);

        return RsData.success(
                null,
                "리스트 아이템이 삭제되었습니다."
        );
    }

    // ============================== 전체 조회 =================================

    // 전체 맛집 리스트 다건 조회
    @GetMapping("/all")
    @Operation(summary = "전체 맛집 리스트 다건 조회")
    public RsData<RestaurantListResponse.RestaurantListsResponse> getAllRestaurantLists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<RestaurantList> result = restaurantListService.findAll(pageable);

        List<RestaurantListResponse.RestaurantLists> lists = result.getContent()
                        .stream()
                .map(RestaurantListResponse.RestaurantLists::new)
                .toList();

        return RsData.success(
                new RestaurantListResponse.RestaurantListsResponse(
                        lists,
                        result.getTotalPages(),
                        result.getTotalElements()
                ),
                "전체 맛집 리스트 목록 조회가 완료되었습니다."
        );
    }

    @GetMapping("/others")
    @Operation(summary = "다른 사용자의 맛집 리스트 다건 조회")
    public RsData<RestaurantListResponse.RestaurantListsResponse> getOtherRestaurantLists(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = userDetails.getUserId();

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<RestaurantList> result =
                restaurantListService.findAllExceptUser(userId, pageable);

        List<RestaurantListResponse.RestaurantLists> lists =
                result.getContent()
                        .stream()
                        .map(RestaurantListResponse.RestaurantLists::new)
                        .toList();

        return RsData.success(
                new RestaurantListResponse.RestaurantListsResponse(
                        lists,
                        result.getTotalPages(),
                        result.getTotalElements()
                ),
                "다른 사용자의 맛집 리스트 목록 조회가 완료되었습니다."
        );
    }

    // 전체 맛집 리스트 단건 조회
    @GetMapping("/all/{id}")
    @Operation(summary = "전체 맛집 리스트 단건 조회")
    public RsData<RestaurantListResponse.RestaurantListDetail> getAllRestaurantListsDetail(
            @PathVariable Long id
    ) {
        RestaurantList restaurantList = restaurantListService.findById(id);

        return RsData.success(
                new RestaurantListResponse.RestaurantListDetail(restaurantList),
                "전체 맛집 리스트 조회가 완료되었습니다."
        );
    }

    // ================= 리스트 복사 ====================
    @PostMapping("/{id}/copy")
    @Operation(summary = "맛집 리스트 복사")
    public RsData<RestaurantListResponse.RestaurantListDetail> copyRestaurantList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id
    ) {
        Long userId = userDetails.getUserId();
        RestaurantList copyList = restaurantListService.copyList(userId, id);

        return RsData.success(
                new RestaurantListResponse.RestaurantListDetail(copyList),
                "맛집 리스트가 복사되었습니다."
        );
    }

}
