package com.study.boardserver.domain.awss3.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AwsS3Service {

    String uploadFile (MultipartFile file, String dir);

    List<String> uploadFiles (List<MultipartFile> multipartFiles, String dir);

    void deleteFile (String fileName, String dir);
}
