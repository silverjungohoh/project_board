package com.study.boardserver.domain.board.repository;

import com.study.boardserver.domain.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
