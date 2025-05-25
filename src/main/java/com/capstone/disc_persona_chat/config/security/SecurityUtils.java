package com.capstone.disc_persona_chat.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 현재 인증된 사용자의 정보를 쉽게 가져오기 위한 유틸리티 클래스
 */
@Component
public class SecurityUtils {

    /**
     * 현재 인증된 사용자의 ID를 가져옵니다.
     * 인증된 사용자가 없는 경우 예외를 발생시킵니다.
     *
     * @return 현재 인증된 사용자의 ID
     * @throws IllegalStateException 인증된 사용자가 없는 경우
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("현재 인증된 사용자가 없습니다.");
        }
        
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }
        
        throw new IllegalStateException("인증된 사용자 정보를 가져올 수 없습니다.");
    }
    
    /**
     * 현재 인증된 사용자의 이메일을 가져옵니다.
     *
     * @return 현재 인증된 사용자의 이메일
     * @throws IllegalStateException 인증된 사용자가 없는 경우
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("현재 인증된 사용자가 없습니다.");
        }
        
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUsername();
        }
        
        return authentication.getName();
    }
}
