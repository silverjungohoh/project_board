package com.study.boardserver.domain.board.repository;

import com.study.boardserver.domain.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
