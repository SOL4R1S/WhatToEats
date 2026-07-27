package com.whattoeat.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;


@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.kakao.client-id=test-client-id",
        "spring.security.oauth2.client.registration.kakao.client-secret=test-client-secret",
        "spring.security.oauth2.client.registration.kakao.authorization-grant-type=authorization_code",
        "spring.security.oauth2.client.registration.kakao.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}",
        "spring.security.oauth2.client.provider.kakao.authorization-uri=https://kauth.kakao.com/oauth/authorize",
        "spring.security.oauth2.client.provider.kakao.token-uri=https://kauth.kakao.com/oauth/token",
        "spring.security.oauth2.client.provider.kakao.user-info-uri=https://kauth.kakao.com/v2/user/me",
        "spring.security.oauth2.client.provider.kakao.user-name-attribute=id",
        "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLW9ubHktMTIzNDU2Nzg=",
        "jwt.access-expiration=3600000",
        "jwt.refresh-expiration=604800000"
})
public class SecurityConfigTest {
    @Autowired
    private MockMvcTester mockMvc;

    @Test
    @DisplayName("카카오 OAuth 시작 엔드포인트 302 리다이렉트")
    void oauth2LoginEndpointRedirects() throws Exception {
        mockMvc.get().uri("/oauth2/authorization/kakao")
                .assertThat()
                .hasStatus3xxRedirection();
    }

    @Test
    @DisplayName("auth 경로 인증 없이 접근 가능")
    void authPermitAll() {
        mockMvc.get().uri("/api/v1/auth/reissue")
                .assertThat()
                .hasStatus4xxClientError();
    }

    @Test
    @DisplayName("보호된 경로는 인증 없이 401")
    void protectedEndpointRequiresAuth(){
        mockMvc.get().uri("/api/v1/feeds")
                .assertThat()
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

}
