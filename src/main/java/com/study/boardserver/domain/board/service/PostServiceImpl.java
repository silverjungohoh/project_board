package com.study.boardserver.domain.board.service;

import com.study.boardserver.domain.awss3.service.AwsS3Service;
import com.study.boardserver.domain.board.dto.post.PostImageUrlResponse;
import com.study.boardserver.domain.board.dto.post.PostWriteRequest;
import com.study.boardserver.domain.board.dto.post.PostWriteResponse;
import com.study.boardserver.domain.board.entity.Post;
import com.study.boardserver.domain.board.entity.PostImage;
import com.study.boardserver.domain.board.repository.PostImageRepository;
import com.study.boardserver.domain.board.repository.PostRepository;
import com.study.boardserver.domain.member.entity.Member;
import com.study.boardserver.global.error.exception.MemberAuthException;
import com.study.boardserver.global.error.type.MemberAuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
}