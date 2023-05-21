package com.study.boardserver.domain.member.repository.redis;

import com.study.boardserver.domain.member.entity.MemberAuthCode;
import org.springframework.data.repository.CrudRepository;

public interface MemberAuthCodeRepository extends CrudRepository<MemberAuthCode, String> {
}
