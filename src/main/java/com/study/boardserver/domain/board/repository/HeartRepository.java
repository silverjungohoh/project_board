package com.study.boardserver.domain.board.repository;

import com.study.boardserver.domain.board.entity.Heart;
import com.study.boardserver.domain.board.entity.Post;
import com.study.boardserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeartRepository extends JpaRepository<Heart, Long> {

    boolean existsByPostAndMember(Post post, Member member);
}
