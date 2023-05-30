package com.study.boardserver.domain.security.jwt;

import com.study.boardserver.domain.security.jwt.redis.LogoutAccessTokenRepository;
import com.study.boardserver.domain.security.jwt.redis.RefreshToken;
import com.study.boardserver.domain.security.jwt.redis.RefreshTokenRepository;
import com.study.boardserver.global.error.exception.MemberAuthException;
import com.study.boardserver.global.error.type.MemberAuthErrorCode;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LogoutAccessTokenRepository logoutAccessTokenRepository;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @Value("${spring.jwt.valid.accessToken}")
    private Long accessTokenValid;

    @Value("${spring.jwt.valid.refreshToken}")
    private Long refreshTokenValid;

    /**
     * key Base64 형식으로 인코딩
     */
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * token 생성
     */
    public String createToken(String email, long expireTime, String role) {
        Claims claims = Jwts.claims().setSubject("token");

        claims.put("email", email);
        claims.put("role", role);

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * access token 발급
     */
    public String issueAccessToken(String email, String role) {
        return createToken(email, accessTokenValid, role);
    }

    /**
     * refresh token 발급
     */
    public String issueRefreshToken(String email, String role) {
        String refreshToken = createToken(email, refreshTokenValid, role);
        saveRefreshToken(email, refreshToken);
        return refreshToken;
    }

    /**
     * token 인증 정보 조회
     */
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUsername(token));

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token: {}", e.getMessage());
            throw new MemberAuthException(MemberAuthErrorCode.EXPIRED_ACCESS_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token: {}", e.getMessage());
            throw new MemberAuthException(MemberAuthErrorCode.UNSUPPORTED_ACCESS_TOKEN);
        } catch (SignatureException | MalformedJwtException | IllegalArgumentException e) {
            log.info("Incorrect JWT token: {}", e.getMessage());
            throw new MemberAuthException(MemberAuthErrorCode.INCORRECT_ACCESS_TOKEN);
        }
    }

    public String getUsername(String token) {

        Claims claims = extractClaims(token);

        return claims.get("email", String.class);
    }

    public String getUserRole(String token) {

        Claims claims = extractClaims(token);

        return claims.get("role", String.class);
    }

    public String resolveToken(HttpServletRequest request) {
        String headerAuth = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(BEARER_PREFIX)) {
            return headerAuth.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * access token 유효성 검증
     */
    public boolean validateAccessToken(String token) {
        return !extractClaims(token).getExpiration().before(new Date());
    }

    /**
     * refresh token 유효성 검증
     */
    public boolean validateRefreshToken(String token) {
        try {
            return !extractClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * token 남은 유효 시간 계산
     */
    public long getRemainingTime(String token) {
        Date expiration = extractClaims(token).getExpiration();
        Date now = new Date();
        return expiration.getTime() - now.getTime();
    }

    /**
     * access token 로그아웃 여부
     */
    public boolean isLogoutAccessToken(String token) {
        return logoutAccessTokenRepository.existsById(token);
    }


    /**
     * refresh token redis 저장
     */
    public void saveRefreshToken(String email, String token) {

        RefreshToken refreshToken = RefreshToken.builder()
                .id(email)
                .refreshToken(token)
                .expiration(refreshTokenValid)
                .build();

        refreshTokenRepository.save(refreshToken);
    }
}
