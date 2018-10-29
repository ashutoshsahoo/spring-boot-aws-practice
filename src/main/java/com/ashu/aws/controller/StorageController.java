package com.ashu.aws.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ashu.aws.services.S3Services;

@RestController
@RequestMapping(path = "/storage")
public class StorageController {

	private final S3Services s3Services;

	public StorageController(S3Services s3Services) {
		super();
		this.s3Services = s3Services;
	}

	@PostMapping
	public String uploadFile(@RequestPart("file") MultipartFile file) {
		return s3Services.uploadFile(file);
	}

	@GetMapping("/{fileName}")
	public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable("fileName") String fileName) {
		return s3Services.download(fileName);
	}

	@DeleteMapping("/{fileName}")
	public void deleteFile(@PathVariable("fileName") String fileName) {
		s3Services.delete(fileName);
	}

}
