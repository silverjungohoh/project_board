package com.study.boardserver.domain.board.repository;

import com.study.boardserver.domain.board.entity.Heart;
import com.study.boardserver.domain.board.entity.Post;
import com.study.boardserver.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeartRepository extends JpaRepository<Heart, Long> {

    boolean existsByPostAndMember(Post post, Member member);

    Optional<Heart> findByPostAndMember(Post post, Member member);

    Long countByPost(Post post);
}
