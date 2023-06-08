package com.study.boardserver.domain.security.handler;

import com.study.boardserver.domain.security.CustomUserDetails;
import com.study.boardserver.domain.security.jwt.JwtTokenProvider;
import com.study.boardserver.domain.security.oauth2.info.OAuth2UserInfo;
import com.study.boardserver.domain.security.oauth2.info.OAuth2UserInfoFactory;
import com.study.boardserver.domain.security.oauth2.repository.CookieAuthorizationRequestRepository;
import com.study.boardserver.domain.security.oauth2.type.ProviderType;
import com.study.boardserver.domain.security.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static com.study.boardserver.domain.security.oauth2.repository.CookieAuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

/**
 * oauth2 인증 성공 시 호출
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${oauth.authorizedRedirectUri}")
    private String redirectUri;
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieAuthorizationRequestRepository authorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            log.info("Response has already been committed.");
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtil.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new IllegalArgumentException("redirect URIs are not matched.");
        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        CustomUserDetails oAuth2User = (CustomUserDetails) authentication.getPrincipal();

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        ProviderType providerType = ProviderType.valueOf(authToken.getAuthorizedClientRegistrationId().toUpperCase());
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, oAuth2User.getAttributes());

        String email = userInfo.getEmail();
        String role = oAuth2User.getRole().name();

        String accessToken = jwtTokenProvider.issueAccessToken(email, role);
        String refreshToken = jwtTokenProvider.issueRefreshToken(email, role);

        return makeRedirectUrl(targetUrl, accessToken, refreshToken);
    }

    /**
     * 인증 요청 시 생성된 쿠키 삭제
     */
    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private String makeRedirectUrl(String targetUrl, String accessToken, String refreshToken) {
        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam(ACCESS_TOKEN, accessToken)
                .queryParam(REFRESH_TOKEN, refreshToken)
                .build().toUriString();
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);
        URI authorizedUri = URI.create(redirectUri);

        if (authorizedUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                && authorizedUri.getPort() == clientRedirectUri.getPort()) {
            return true;
        }
        return false;
    }
}
