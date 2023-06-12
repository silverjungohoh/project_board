package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.board.dto.post.PostWriteRequest;
import com.study.boardserver.domain.board.dto.post.PostWriteResponse;
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
}
