package com.study.boardserver.domain.board.repository;

import com.study.boardserver.domain.board.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
}
