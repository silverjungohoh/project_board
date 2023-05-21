package com.study.boardserver.domain.member.service;

import java.util.Map;

public interface MemberService {

    /**
     * 회원가입 시 이메일 중복 체크
     */
    Map<String, String> checkDuplicatedEmail (String email);

    /**
     * 닉네임 중복 체크
     */
    Map<String, String> checkDuplicatedNickname (String nickname);

    /**
     * 이메일 인증 코드 발송
     */
    Map<String, String> sendAuthCode (String email);

}
