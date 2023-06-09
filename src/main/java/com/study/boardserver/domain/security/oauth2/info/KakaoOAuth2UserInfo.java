package com.study.boardserver.domain.security.oauth2.info;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final Map<String, Object> attributesAccount;
    private final Map<String, Object> attributesProfile;


    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.attributesAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.attributesProfile = (Map<String, Object>) attributesAccount.get("profile");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        return attributesAccount.get("email").toString();
    }

    @Override
    public String getName() {
        return attributesAccount.get("nickname").toString();
    }

    @Override
    public String getImageUrl() {
        return attributesProfile.get("profile_image_url").toString();
    }
}