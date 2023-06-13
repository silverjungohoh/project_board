package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.board.dto.post.*;
import com.study.boardserver.domain.member.entity.Member;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface PostService {

    /**
     * 작성
     */
    PostWriteResponse writePost(Member member, PostWriteRequest request, List<MultipartFile> files);

    /**
     * 삭제
     */
    Map<String, String> deletePost(Member member, Long postId);

    /**
     * 이미지 업로드
     */
    PostImageUrlResponse uploadPostImage(Long postId, MultipartFile file);

    /**
     * 이미지 삭제
     */
    Map<String, String> deletePostImage(Long postId, Long postImageId);
}
