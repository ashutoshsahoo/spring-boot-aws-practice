package com.ashu.aws.s3.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Services {

    String uploadFile(MultipartFile file);

    void delete(String fileName);

    ResponseEntity<ByteArrayResource> download(String fileName);

    List<String> listObjects();
}
