package com.study.boardserver.domain.member.repository.redis;

import com.study.boardserver.domain.member.entity.MemberAuthCode;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MemberAuthCodeRepository extends CrudRepository<MemberAuthCode, String> {

    Optional<MemberAuthCode> findByIdAndEmail(String id, String email);
}
