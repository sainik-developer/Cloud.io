package com.cloudio.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AWSS3Services {

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;
    @Value("${amazonProperties.bucketName}")
    private String bucketName;

    private final AmazonS3 s3client;

    public Mono<String> uploadFileInS3(final Mono<FilePart> filePartMono) {
        return filePartMono.flatMap(filePart -> {
            try {
                final String s3key = UUID.randomUUID() + filePart.filename().replace(" ", "_");
                File tempFile = File.createTempFile(s3key, "tmp");
                //TODO wrong practice, will need to make it non blocking
                filePart.transferTo(tempFile).block();
                final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3key, tempFile).withCannedAcl(CannedAccessControlList.PublicRead);
                PutObjectResult putObjectResult = s3client.putObject(putObjectRequest);
                return Mono.just(endpointUrl + s3key);
            } catch (final IOException e) {
                return Mono.error(new RuntimeException());
            }
        });
    }

    public Boolean deleteFilesInS3(final String key) {
        try {
            s3client.deleteObject(bucketName, key);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    public S3Object getFile(final String bucketName, final String objectKey) {
        return s3client.getObject(bucketName, objectKey);
    }
}
