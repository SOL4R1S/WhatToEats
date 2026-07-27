package com.whattoeat.global.security;

import com.whattoeat.domain.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class KakaoOAuth2User implements OAuth2User {
    private final User user;

    @Override
    public Map<String, Object> getAttributes(){
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name()));
    }

    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }
}
