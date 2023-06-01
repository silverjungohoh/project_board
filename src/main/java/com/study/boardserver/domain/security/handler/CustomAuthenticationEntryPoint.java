package com.study.boardserver.domain.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.boardserver.global.error.response.ErrorResponse;
import com.study.boardserver.global.error.type.MemberAuthErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.info("UnAuthorized!!! message : " + authException.getMessage());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        String responseBody = objectMapper.writeValueAsString(ErrorResponse.builder()
                .status(MemberAuthErrorCode.FAIL_TO_AUTHENTICATION.getStatus().value())
                .message(MemberAuthErrorCode.FAIL_TO_AUTHENTICATION.getMessage())
                .build());

        PrintWriter writer = response.getWriter();
        writer.write(responseBody);
        writer.flush();
    }
}
