package com.study.boardserver.domain.member.repository;

import com.study.boardserver.config.EmbeddedRedisConfig;
import com.study.boardserver.domain.member.entity.MemberAuthCode;
import com.study.boardserver.domain.member.repository.redis.MemberAuthCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@DataRedisTest
@Import(EmbeddedRedisConfig.class)
@ActiveProfiles("test")
public class MemberAuthCodeRepositoryTest {

    @Autowired
    private MemberAuthCodeRepository memberAuthCodeRepository;

    @BeforeEach
    void clear(){
        memberAuthCodeRepository.deleteAll();
    }

    @Test
    @DisplayName("회원 인증 코드 저장")
    void save() {

        String email = "test@test.com";
        String code = "abc123";
        Long expiredAt = 100L;

        MemberAuthCode authCode = MemberAuthCode.builder()
                .id(code)
                .email(email)
                .expiredAt(expiredAt)
                .build();

        memberAuthCodeRepository.save(authCode);

        MemberAuthCode find = memberAuthCodeRepository.findById(code).get();
        assertEquals(email, find.getEmail());
        assertEquals(code, find.getId());
        assertEquals(expiredAt, find.getExpiredAt());
    }
}
