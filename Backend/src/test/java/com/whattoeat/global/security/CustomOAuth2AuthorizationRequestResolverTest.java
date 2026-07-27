package com.whattoeat.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomOAuth2AuthorizationRequestResolverTest {
    private CustomOAuth2AuthorizationRequestResolver resolver;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        ClientRegistration kakao = ClientRegistration.withRegistrationId("kakao")
                .clientId("test-cid")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .scope("profile_nickname","profile_image","account_email")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userNameAttributeName("id")
                .clientName("kakao").build();

        ClientRegistrationRepository repo = new InMemoryClientRegistrationRepository(kakao);
        resolver = new CustomOAuth2AuthorizationRequestResolver(repo);
        request = new MockHttpServletRequest();
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setScheme("http");
    }

    @Test
    @DisplayName("요청 생성")
    void resolve(){
        request.setParameter("redirectUri", "https://localhost:3000/mypage");

        OAuth2AuthorizationRequest result = resolver.resolve(request, "kakao");

        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo("test-cid");
    }

    @Test
    @DisplayName("state redirectUri 인코딩")
    void state_redirectUri(){
        String customRedirectUri = "http://localhost:5173/feed/123";
        request.setParameter("redirectUri", customRedirectUri);
        OAuth2AuthorizationRequest result = resolver.resolve(request, "kakao");
        String decode = new String(
                Base64.getUrlDecoder().decode(result.getState()),
                StandardCharsets.UTF_8
        );
        assertThat(decode.split("#",2)[0]).isEqualTo(customRedirectUri);
    }

    @Test
    @DisplayName("redirectUri 없으면 기본값으로")
    void default_redirectUri(){
        OAuth2AuthorizationRequest result = resolver.resolve(request, "kakao");
        String decode = new String(
                Base64.getUrlDecoder().decode(result.getState()),
                StandardCharsets.UTF_8
        );
        assertThat(decode.split("#",2)[0]).isEqualTo("http://localhost:3000");

    }
}
