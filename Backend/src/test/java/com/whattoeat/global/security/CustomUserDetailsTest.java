package com.whattoeat.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.whattoeat.domain.user.entity.Role;
import com.whattoeat.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CustomUserDetails 테스트")
class CustomUserDetailsTest {

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getLoginId()).thenReturn("testUser");
        when(mockUser.getPassword()).thenReturn("password");
        when(mockUser.getRole()).thenReturn(Role.USER);
        userDetails = new CustomUserDetails(mockUser);
    }

    @Test
    @DisplayName("getUserId는 유저의 id를 반환")
    void getUserId() {
        assertThat(userDetails.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getUsername은 loginId를 반환")
    void getUsername() {
        assertThat(userDetails.getUsername()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("getAuthorities는 ROLE_USER를 반환")
    void getAuthorities() {
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }
}
