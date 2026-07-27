package com.whattoeat.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 1, max = 20) String nickname,
        @Email @Size(max = 100) String email,
        String currentPassword,
        @Size(min = 8, max = 20) String newPassword
) {
}
