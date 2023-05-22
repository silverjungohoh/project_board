package com.study.boardserver.domain.mail.service;

import com.study.boardserver.global.error.exception.MemberException;
import com.study.boardserver.global.error.type.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private static final String SUBJECT = "[회원 인증] 이메일 인증 코드 발송 안내";
    private static final String ENCODING = "utf-8";
    private static final String FROM = "test20230521@gmail.com";
    private final JavaMailSender javaMailSender;

    @Override
    public boolean sendMail(String email, String authCode) {
        try {

            MimeMessage message = javaMailSender.createMimeMessage();
            makeAuthCode(email, authCode, message);
            javaMailSender.send(message);

        } catch (Exception e) {
            throw new MemberException(MemberErrorCode.FAIL_TO_SEND_EMAIL);
        }
        return true;
    }

    private static void makeAuthCode(String email, String authCode, MimeMessage message) throws MessagingException {
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true, ENCODING);

        String text = "인증 코드 : " + authCode;

        mimeMessageHelper.setFrom(FROM);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(SUBJECT);
        mimeMessageHelper.setText(text, true);
    }
}
