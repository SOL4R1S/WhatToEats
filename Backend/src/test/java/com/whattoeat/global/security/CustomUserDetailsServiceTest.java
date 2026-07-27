package com.whattoeat.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService 테스트")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("존재하는 userId로 조회하면 CustomUserDetails 반환")
    void v1() {
        given(userRepository.findById(1L)).willReturn(Optional.of(mock(User.class)));

        UserDetails result = customUserDetailsService.loadUserByUsername("1");

        assertThat(result).isInstanceOf(CustomUserDetails.class);
    }

    @Test
    @DisplayName("존재하지 않는 userId로 조회하면 UsernameNotFoundException 발생")
    void v2() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("999"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
