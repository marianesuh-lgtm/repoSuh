package com.mrs.shakes.dto;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.mrs.shakes.entity.User;

import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User member; // 우리 DB의 Member 엔티티

    public CustomUserDetails(User member) {
        this.member = member;
    }

    // [중요] 사용자의 권한을 리턴 (DB의 role 필드 사용)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + member.getRole())
        );
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        // 식별자로 사용할 값을 리턴 (이메일 혹은 소셜 ID)
        return member.getEmail(); 
    }

    // 아래 설정들은 기본적으로 true로 설정해야 계정이 활성화됩니다.
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
