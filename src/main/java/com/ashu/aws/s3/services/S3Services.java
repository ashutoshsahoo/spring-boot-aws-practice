package com.ashu.aws.s3.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.S3Object;

public interface S3Services {

	void uploadFile(String fileName, String filePath);

	S3Object downloadFile(String fileName);

	String uploadFile(MultipartFile file);

	void delete(String fileName);

	ResponseEntity<ByteArrayResource> download(String fileName);
}
