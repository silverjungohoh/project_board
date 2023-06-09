package com.study.boardserver.domain.security.oauth2.info;

import com.study.boardserver.domain.security.oauth2.type.ProviderType;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(ProviderType providerType, Map<String, Object> attributes) {
        switch (providerType) {
            case GOOGLE: return new GoogleOAuth2UserInfo(attributes);
            case NAVER: return new NaverOAuth2UserInfo(attributes);
            case KAKAO: return new KakaoOAuth2UserInfo(attributes);

            default: throw new IllegalArgumentException("소셜 타입이 유효하지 않습니다.");
        }
    }
}
