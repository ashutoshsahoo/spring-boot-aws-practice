package com.ashu.aws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

import com.ashu.aws.services.S3Services;

//@Component
public class S3ClientCLR implements CommandLineRunner {

	@Autowired
	private S3Services s3Services;

	@Value("${app.uploadFilePath}")
	private String uploadFilePath;

	@Value("${app.fileName}")
	private String fileName;

	@Override
	public void run(String... args) throws Exception {
		System.out.println("---------------- START UPLOAD FILE ----------------");
		s3Services.uploadFile(fileName, uploadFilePath);
		System.out.println("---------------- START DOWNLOAD FILE ----------------");
		s3Services.downloadFile(fileName);
	}

}
