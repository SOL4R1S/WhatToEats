package com.whattoeat.global.security;

import com.whattoeat.domain.user.entity.User;
import com.whattoeat.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("[OAuth2] loadUser called. provider={}", userRequest.getClientRegistration().getRegistrationId());

        String providerType = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        String oauthUserId = "";
        String nickname = "";
        String profileImg = "";
        String email = "";

        User user = switch (providerType) {
            case "KAKAO" -> {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                log.info("[OAuth2] kakao attributes={}", attributes);
                Map<String, Object> attributesProperties = (Map<String, Object>) attributes.get("properties");
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

                oauthUserId = oAuth2User.getName();
                nickname = attributesProperties != null ? (String) attributesProperties.get("nickname") : null;
                profileImg = attributesProperties != null ? (String) attributesProperties.get("profile_image") : null;
                email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

                log.info("[OAuth2] extracted. oauthUserId={}, nickname={}, email={}", oauthUserId, nickname, email);
                yield userService.kakaoLoginOrSignUp(oauthUserId, nickname, profileImg, email);
            }
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + providerType);
        };
        return new KakaoOAuth2User(user);
    }

}
