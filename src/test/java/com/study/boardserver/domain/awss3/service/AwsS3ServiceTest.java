package com.study.boardserver.domain.awss3.service;

import com.study.boardserver.config.AwsS3MockConfig;
import com.study.boardserver.global.error.exception.ImageException;
import com.study.boardserver.global.error.type.ImageErrorCode;
import io.findify.s3mock.S3Mock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(AwsS3MockConfig.class)
@ActiveProfiles("test")
class AwsS3ServiceTest {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${test-path}")
    private String testPath;

    @Autowired
    private S3Mock s3Mock;

    @Autowired
    private AwsS3ServiceImpl awsS3Service;

    @AfterEach
    public void tearDown() {
        s3Mock.stop();
    }

    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadImage_Success() {

        String filename = "test_2023.png";
        String contentType = "image/png";
        String dir = "test-dir";

        String path = testPath+"/"+bucket+"/"+dir+"/";
        MockMultipartFile file = new MockMultipartFile("test", filename, contentType, "test".getBytes());

        String imgUrl = awsS3Service.uploadFile(file, dir);

        assertThat(imgUrl.substring(0, imgUrl.lastIndexOf("/") + 1)).isEqualTo(path);
    }

    @Test
    @DisplayName("이미지 업로드 실패")
    void uploadImage_Fail() {

        String filename = "test_2023.txt";
        String contentType = "image/png";
        String dir = "test-dir";

        MockMultipartFile file = new MockMultipartFile("test", filename, contentType, "test".getBytes());

        ImageException exception = assertThrows(ImageException.class,
                ()-> awsS3Service.uploadFile(file, dir));

        assertEquals(exception.getErrorCode(), ImageErrorCode.INVALID_IMAGE_TYPE);
    }
}