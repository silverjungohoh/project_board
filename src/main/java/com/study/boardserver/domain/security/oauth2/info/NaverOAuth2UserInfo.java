package com.study.boardserver.domain.security.oauth2.info;

import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo  {

    private final Map<String, Object> attributes;
    private final Map<String, Object> attributesResponse;

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.attributesResponse = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getProviderId() {
        return attributesResponse.get("id").toString();
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getEmail() {
        return attributesResponse.get("email").toString();
    }

    @Override
    public String getName() {
        return attributesResponse.get("name").toString();
    }

    @Override
    public String getImageUrl() {
        return attributesResponse.get("profile_image").toString();
    }
}
