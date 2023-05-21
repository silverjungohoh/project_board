package com.study.boardserver.domain.member.service;

import com.study.boardserver.domain.mail.service.MailService;
import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.domain.member.repository.MemberRepository;
import com.study.boardserver.global.error.exception.MemberException;
import com.study.boardserver.global.error.type.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MailService mailService;

    @Override
    public Map<String, String> checkDuplicatedEmail(String email) {
        Optional<Member> optional = memberRepository.findByEmail(email);
        if(!optional.isEmpty()) {
            throw new MemberException(MemberErrorCode.DUPLICATED_EMAIL);
        }
        return getMessage("사용 가능한 이메일입니다.");
    }

    @Override
    public Map<String, String> checkDuplicatedNickname(String nickname) {
        Optional<Member> optional = memberRepository.findByNickname(nickname);
        if(!optional.isEmpty()) {
            throw new MemberException(MemberErrorCode.DUPLICATED_NICKNAME);
        }
        return getMessage("사용 가능한 닉네임입니다.");
    }

    @Override
    public Map<String, String> sendAuthCode(String email) {
        Optional<Member> optional = memberRepository.findByEmail(email);
        if (!optional.isEmpty()) {
            throw new MemberException(MemberErrorCode.DUPLICATED_EMAIL);
        }

        String authCode = UUID.randomUUID().toString().substring(0, 8);
        mailService.sendMail(email, authCode);

        return getMessage("이메일 인증 코드를 전송하였습니다.");
    }


    private static Map<String, String> getMessage(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("message", message);
        return result;
    }
}
