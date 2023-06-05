package com.study.boardserver.domain.awss3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.study.boardserver.domain.awss3.type.FileExtension;
import com.study.boardserver.global.error.exception.ImageException;
import com.study.boardserver.global.error.type.ImageErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AwsS3ServiceImpl implements AwsS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3 amazonS3;

    /**
     * 이미지 파일 업로드
     */
    @Override
    public String uploadFile(MultipartFile file, String dir) {

        String fileName = createFileName(file.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket + "/" + dir, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new ImageException(ImageErrorCode.FAIL_TO_UPLOAD_IMAGE);
        }
        return amazonS3.getUrl(bucket, dir + "/" + fileName).toString();
    }


    /**
     * 복수의 이미지 파일 업로드
     */
    @Override
    public List<String> uploadFiles(List<MultipartFile> multipartFiles, String dir) {
        List<String> imgUrlList = new ArrayList<>();

        multipartFiles.forEach(file -> {
            String imgUrl = uploadFile(file, dir);
            imgUrlList.add(imgUrl);
        });

        return imgUrlList;
    }

    /**
     * 업로드 된 파일 삭제
     */
    @Override
    public void deleteFile(String imgUrl, String dir) {
        amazonS3.deleteObject(bucket, dir + "/" + imgUrl.substring(imgUrl.lastIndexOf("/") + 1));
    }

    /**
     * 파일 업로드 시 파일명 생성
     */
    private String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String fileName) {

        List<String> extensions = Stream.of(FileExtension.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();

        if (!extensions.contains(fileExtension)) {
            throw new ImageException(ImageErrorCode.INVALID_IMAGE_TYPE);
        }

        return fileName.substring(fileName.lastIndexOf("."));
    }
}
