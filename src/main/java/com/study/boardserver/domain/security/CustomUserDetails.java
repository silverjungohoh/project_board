package com.study.boardserver.domain.security;

import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.domain.member.type.MemberRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@NoArgsConstructor
public class CustomUserDetails implements UserDetails, OAuth2User {

    private String username;
    private String password;
    private MemberRole role;
    private Member member;
    private Map<String, Object> attributes;

    public CustomUserDetails(Member member) {
        this.username = member.getEmail();
        this.password = member.getPassword();
        this.member = member;
        this.role = member.getRole();
    }

    public CustomUserDetails(Member member, Map<String, Object> attributes) {
        this.username = member.getEmail();
        this.password = member.getPassword();
        this.member = member;
        this.role = member.getRole();
        this.attributes = attributes;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(member.getRole().name()));
    }

    @Override
    public String getName() {
        return username;
    }
}
