package com.whattoeat.global.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private final ClientRegistrationRepository clientRegistrationRepository;

    private DefaultOAuth2AuthorizationRequestResolver defaultResolver() {
        return new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authRequest = defaultResolver().resolve(request);
        return customize(authRequest, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegiId) {
        OAuth2AuthorizationRequest authRequest = defaultResolver().resolve(request, clientRegiId);
        return customize(authRequest, request);
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest authRequest,
                                                 HttpServletRequest request) {
        if (authRequest == null) return null;

        // 요청 파라미터에서 redirectUri 가져오기
        String redirectUri = request.getParameter("redirectUri");
        if (redirectUri == null || redirectUri.isBlank()) redirectUri = "http://localhost:3000";

        //CSRF 방지용 nonce 추가
        String originState = UUID.randomUUID().toString();

        // redirectUri#originState 결합
        String rawState = redirectUri + "#" + originState;

        // Base64 URL-safe 인코딩
        String encodeState = Base64.getUrlEncoder().
                encodeToString(rawState.getBytes(StandardCharsets.UTF_8));

        log.info("[OAuth2] authorization request built. redirectUri={}, state={}", redirectUri, encodeState);

        return OAuth2AuthorizationRequest.from(authRequest)
                .state(encodeState) //state 교체
                .build();
    }
}
