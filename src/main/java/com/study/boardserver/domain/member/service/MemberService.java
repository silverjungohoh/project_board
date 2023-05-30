package com.study.boardserver.domain.member.service;

import com.study.boardserver.domain.member.dto.login.LoginRequest;
import com.study.boardserver.domain.member.dto.login.LoginResponse;
import com.study.boardserver.domain.member.dto.reissue.ReissueTokenRequest;
import com.study.boardserver.domain.member.dto.reissue.ReissueTokenResponse;
import com.study.boardserver.domain.member.dto.signup.ConfirmAuthCodeRequest;
import com.study.boardserver.domain.member.dto.signup.SignUpRequest;
import com.study.boardserver.domain.member.dto.signup.SignUpResponse;

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

    /**
     * 이메일 인증 코드 확인
     */
    Map<String, String> confirmAuthCode (ConfirmAuthCodeRequest request);

    /**
     * 회원 가입
     */
    SignUpResponse signUp (SignUpRequest request);

    /**
     * 회원 로그인
     */
    LoginResponse login (LoginRequest request);

    /**
     * access token 재발급
     */
    ReissueTokenResponse reissueToken (ReissueTokenRequest request);
}
