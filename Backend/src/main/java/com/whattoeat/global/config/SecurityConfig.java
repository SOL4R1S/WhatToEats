package com.whattoeat.global.config;

import com.whattoeat.global.jwt.JwtAuthenticationFilter;
import com.whattoeat.global.security.CustomOAuth2AuthorizationRequestResolver;
import com.whattoeat.global.security.CustomOAuth2LoginSuccessHandler;
import com.whattoeat.global.security.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2LoginSuccessHandler customOAuth2LoginSuccessHandler;
    private final CustomOAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver;

    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2.
                        userInfoEndpoint(userinfo -> userinfo.userService(customOAuth2UserService))
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(customOAuth2AuthorizationRequestResolver))
                        .successHandler(customOAuth2LoginSuccessHandler))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    @Profile("!dev")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/signup",
                                "/api/v1/auth/oauth/exchange",
                                "/api/v1/auth/reissue",
                                "/api/v1/auth/logout",
                                "/api/v1/restaurants",
                                "/api/v1/restaurants/**",
                                "/uploads/**",
                                "/oauth2/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userinfo -> userinfo.userService(customOAuth2UserService))
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestResolver(customOAuth2AuthorizationRequestResolver))
                        .successHandler(customOAuth2LoginSuccessHandler)
                        .failureHandler((_, response, exception) -> {
                            log.error("[OAuth2] login failed: {}", exception.getMessage(), exception);
                            response.sendRedirect("/login?error");
                        })
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((_, response, _) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", frontendUrl));
        config.setAllowedMethods(List.of("GET", "PUT", "POST", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
