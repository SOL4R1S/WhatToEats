package com.whattoeat.domain.user.controller;

import com.whattoeat.domain.user.dto.UpdateProfileRequest;
import com.whattoeat.domain.user.dto.UserProfileResponse;
import com.whattoeat.domain.user.service.UserService;
import com.whattoeat.global.rsData.RsData;
import com.whattoeat.global.security.CustomUserDetails;
import com.whattoeat.global.upload.ImageUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ImageUploadService imageUploadService;

    //내 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<RsData<UserProfileResponse>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(RsData.success(
                userService.getUser(userDetails.getUserId(), userDetails.getUserId()),
                "내 프로필 조회 성공"));
    }

    //프로필 조회
    @GetMapping("/{id}")
    public ResponseEntity<RsData<UserProfileResponse>> getUser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(RsData.success(
                userService.getUser(id, userDetails.getUserId()),
                "프로필 조회 성공"));
    }

    //내 프로필 수정
    @PatchMapping("/me")
    public ResponseEntity<RsData<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UpdateProfileRequest request) {
        Long userId = userDetails.getUserId();
        UserProfileResponse response = userService.updateProfile(userId, userId, request);
        return ResponseEntity.ok(RsData.success(response, "프로필이 수정되었습니다."));
    }

    //프로필 이미지 변경
    @PatchMapping("/me/image")
    public ResponseEntity<RsData<UserProfileResponse>> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("image") MultipartFile image
    ) throws IOException {
        String imageUrl = imageUploadService.upload(image);
        UserProfileResponse response = userService.updateProfileImage(userDetails.getUserId(), imageUrl);
        return ResponseEntity.ok(RsData.success(response, "프로필 이미지가 변경되었습니다."));
    }

}
