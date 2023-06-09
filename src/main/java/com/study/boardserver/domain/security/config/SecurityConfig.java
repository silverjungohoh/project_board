package com.study.boardserver.domain.security.config;

import com.study.boardserver.domain.security.handler.CustomAccessDeniedHandler;
import com.study.boardserver.domain.security.handler.CustomAuthenticationEntryPoint;
import com.study.boardserver.domain.security.handler.OAuth2AuthenticationFailureHandler;
import com.study.boardserver.domain.security.handler.OAuth2AuthenticationSuccessHandler;
import com.study.boardserver.domain.security.jwt.JwtAuthenticationFilter;
import com.study.boardserver.domain.security.jwt.JwtExceptionFilter;
import com.study.boardserver.domain.security.oauth2.repository.CookieAuthorizationRequestRepository;
import com.study.boardserver.domain.security.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

import static com.study.boardserver.domain.member.type.MemberRole.ROLE_ADMIN;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtExceptionFilter jwtExceptionFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler authenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler authenticationFailureHandler;
    private final CookieAuthorizationRequestRepository authorizationRequestRepository;


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/h2-console/**", "/swagger-ui/**", "/docs/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.httpBasic().disable()
                .csrf().disable();

        http.formLogin().disable();
        http.logout().disable();

        http.headers().frameOptions().sameOrigin();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .antMatchers("/api/members/sign-up/email", "/api/members/nickname"
                        , "/api/members/email-auth", "/api/members/email-authentication"
                        , "/api/members/sign-up", "/api/members/auth/login", "/api/members/auth/token").permitAll()
                .antMatchers("/api/admin/**").hasAuthority(ROLE_ADMIN.name())
                .anyRequest().authenticated();

        http.oauth2Login()
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler)

                .authorizationEndpoint()
                .baseUri("/login/oauth2/authorization")
                .authorizationRequestRepository(authorizationRequestRepository)

                .and()
                .userInfoEndpoint()
                .userService(oAuth2UserService);

        http.exceptionHandling()
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)

                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}