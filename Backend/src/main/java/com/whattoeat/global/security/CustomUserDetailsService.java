package com.whattoeat.global.security;

import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    // GolbalExceptionHandler는 컨트롤러 이후 예외만 잡을수 있음
    // 아래 메서드는 필터에서 호출되기 때문에 직접 처리해야함
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
        return new CustomUserDetails(user);
    }

}
