package com.ashu.aws.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

@Service
public class S3ServicesImpl implements S3Services {

	private Logger logger = LoggerFactory.getLogger(S3ServicesImpl.class);

	@Value("${aws.bucketName}")
	private String bucketName;

	@Value("${aws.endpointUrl}")
	private String endpointUrl;

	private final AmazonS3 s3Client;

	public S3ServicesImpl(AmazonS3 s3Client) {
		super();
		this.s3Client = s3Client;
	}

	@Override
	public void uploadFile(String fileName, String filePath) {
		try {
			File file = Paths.get(filePath).toFile();
			PutObjectResult result = s3Client.putObject(
					new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
			logger.info("===================== Upload File - Done! =====================");
			logger.info("getContentMd5 : " + result.getContentMd5());
		} catch (AmazonServiceException ase) {
			logger.info("Caught an AmazonServiceException from PUT requests, rejected reasons:");
			logger.info("Error Message:    " + ase.getMessage());
			logger.info("HTTP Status Code: " + ase.getStatusCode());
			logger.info("AWS Error Code:   " + ase.getErrorCode());
			logger.info("Error Type:       " + ase.getErrorType());
			logger.info("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			logger.info("Caught an AmazonClientException: ");
			logger.info("Error Message: " + ace.getMessage());
		}

	}

	@Override
	public S3Object downloadFile(String fileName) {
		try {
			logger.info("================== downloading a file ===================");
			S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, fileName));
			logger.info("Content-Type : " + s3Object.getObjectMetadata().getContentType());
			// Utility.displayText(s3Object.getObjectContent());
			logger.info("===================== Import File - Done! =====================");
			return s3Object;
		} catch (AmazonServiceException ase) {
			logger.info("Caught an AmazonServiceException from GET requests, rejected reasons:");
			logger.info("Error Message:    " + ase.getMessage());
			logger.info("HTTP Status Code: " + ase.getStatusCode());
			logger.info("AWS Error Code:   " + ase.getErrorCode());
			logger.info("Error Type:       " + ase.getErrorType());
			logger.info("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			logger.info("Caught an AmazonClientException: ");
			logger.info("Error Message: " + ace.getMessage());
//		} catch (IOException ioe) {
//			logger.info("IOE Error Message: " + ioe.getMessage());
		}
		return null;
	}

	@Override
	public String uploadFile(MultipartFile multipartFile) {
		String fileName = null;
		try {
			File file = convertMulitipartToFile(multipartFile);
			fileName = generateFileName(multipartFile);
			uploadFile(fileName, file.getAbsolutePath());
			file.delete();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return fileName;
	}

	private String generateFileName(MultipartFile multipartFile) {
		return new Date().getTime() + "-" + multipartFile.getOriginalFilename().replace(" ", "-");
	}

	private File convertMulitipartToFile(MultipartFile multipartFile) throws IOException {
		File file = new File(multipartFile.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(multipartFile.getBytes());
		fos.close();
		return file;
	}

	@Override
	public void delete(String fileName) {
		s3Client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
	}

	@Override
	public ResponseEntity<ByteArrayResource> download(String fileName) {
		S3Object s3Object = downloadFile(fileName);
		if (s3Object == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		try {
			if (s3Object != null) {
				ByteArrayResource resource = new ByteArrayResource(IOUtils.toByteArray(s3Object.getObjectContent()));
				return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
						.contentType(MediaType.parseMediaType(s3Object.getObjectMetadata().getContentType()))
						.contentLength(s3Object.getObjectMetadata().getContentLength()).body(resource);
			}
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
