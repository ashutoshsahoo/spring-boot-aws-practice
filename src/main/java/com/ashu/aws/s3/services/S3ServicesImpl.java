package com.ashu.aws.s3.services;

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
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class S3ServicesImpl implements S3Services {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ServicesImpl.class);

    @Value("${aws.bucketName}")
    private String bucketName;

    private final S3Client s3Client = S3Client.builder().build();

    @Override
    public String uploadFile(MultipartFile multipartFile) {
        try {
            PutObjectResponse response = s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(multipartFile.getOriginalFilename()).build(),
                    RequestBody.fromBytes(multipartFile.getBytes()));
            LOGGER.info("===================== Upload File - Done! =====================");
            LOGGER.info("eTag : {}", response.eTag());
            return response.eTag();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }


    @Override
    public void delete(String fileName) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(fileName).build());
    }

    @Override
    public ResponseEntity<ByteArrayResource> download(String fileName) {

        try {
            LOGGER.info("================== downloading a file ===================");
            ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder().bucket(bucketName).key(fileName).build());
            if (objectAsBytes == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            LOGGER.info("Content-Type : {}", objectAsBytes.response().contentType());
            LOGGER.info("===================== Import File - Done! =====================");
            ByteArrayResource resource = new ByteArrayResource(objectAsBytes.asByteArray());
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                    .contentType(MediaType.parseMediaType(objectAsBytes.response().contentType()))
                    .contentLength(objectAsBytes.response().contentLength()).body(resource);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public List<String> listObjects() {
        List<String> objectList = new ArrayList<>();
        try {
            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsResponse res = s3Client.listObjects(listObjects);
            List<S3Object> objects = res.contents();

            for (S3Object myValue : objects) {
                LOGGER.info(" The name of the key is {}", myValue.key());
                LOGGER.info(" The object is {} KBs", myValue.size() / 1024);
                LOGGER.info(" The owner is {}", myValue.owner());
                objectList.add(myValue.key());
            }
        } catch (S3Exception e) {
            LOGGER.error(e.awsErrorDetails().errorMessage());
        }
        return objectList;
    }

}

