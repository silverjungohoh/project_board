package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.awss3.service.AwsS3Service;
import com.study.boardserver.domain.board.dto.post.*;
import com.study.boardserver.domain.board.entity.Post;
import com.study.boardserver.domain.board.entity.PostImage;
import com.study.boardserver.domain.board.repository.PostImageRepository;
import com.study.boardserver.domain.board.repository.PostRepository;
import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.global.error.exception.MemberAuthException;
import com.study.boardserver.global.error.exception.BoardException;
import com.study.boardserver.global.error.type.BoardErrorCode;
import com.study.boardserver.global.error.type.MemberAuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final String DIR = "post";

    private final AwsS3Service awsS3Service;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    @Override
    public PostWriteResponse writePost(Member member, PostWriteRequest request, List<MultipartFile> files) {

        if(Objects.isNull(member)) {
            throw new MemberAuthException(MemberAuthErrorCode.MEMBER_NOT_FOUND);
        }

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .member(member)
                .build();

        postRepository.save(post);

        List<PostImageUrlResponse> imgResponse = new ArrayList<>();

        if(!Objects.isNull(files)) {
            List<String> imgUrls = awsS3Service.uploadFiles(files, DIR);

            for(String url : imgUrls) {
                PostImage image = PostImage.builder()
                        .imgUrl(url)
                        .post(post)
                        .createdAt(LocalDateTime.now())
                        .build();

                postImageRepository.save(image);
                post.addImage(image);

                PostImageUrlResponse imgUrl = PostImageUrlResponse.builder()
                        .imageId(image.getId())
                        .imageUrl(image.getImgUrl())
                        .build();

                imgResponse.add(imgUrl);
            }
        }

        return PostWriteResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .nickname(member.getNickname())
                .imageUrls(imgResponse)
                .createdAt(post.getCreatedAt())
                .build();
    }

    @Override
    public Map<String, String> deletePost(Member member, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        if(!Objects.equals(post.getMember().getEmail(), member.getEmail())) {
            throw new BoardException(BoardErrorCode.CANNOT_DELETE_POST);
        }

        List<PostImage> postImages = postImageRepository.findAllByPost(post);
        if(!postImages.isEmpty()) {
            removeImages(postImages);
        }

        postRepository.delete(post);
        return getMessage("게시물이 삭제되었습니다.");
    }

    @Override
    public PostImageUrlResponse uploadPostImage(Long postId, MultipartFile file) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_NOT_FOUND));

        String imgUrl = awsS3Service.uploadFile(file, DIR);

        PostImage image = PostImage.builder()
                .post(post)
                .imgUrl(imgUrl)
                .build();

        postImageRepository.save(image);

        return PostImageUrlResponse.builder()
                .imageId(image.getId())
                .imageUrl(image.getImgUrl())
                .build();
    }

    @Override
    public Map<String, String> deletePostImage(Long postId, Long postImageId) {

        if(!postRepository.existsById(postId)) {
            throw new BoardException(BoardErrorCode.POST_NOT_FOUND);
        }

        PostImage image = postImageRepository.findById(postImageId)
                .orElseThrow(() -> new BoardException(BoardErrorCode.POST_IMAGE_NOT_FOUND));

        awsS3Service.deleteFile(image.getImgUrl(), DIR);
        postImageRepository.delete(image);

        return getMessage("이미지가 삭제되었습니다.");
    }


    private void removeImages (List<PostImage> images) {
        for(PostImage image : images) {
            awsS3Service.deleteFile(image.getImgUrl(), DIR);
        }
    }

    private static Map<String, String> getMessage(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("message", message);
        return result;
    }
}
