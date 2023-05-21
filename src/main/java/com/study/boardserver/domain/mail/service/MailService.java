package com.study.boardserver.domain.mail.service;

public interface MailService {

    boolean sendMail(String email, String authCode);
}
