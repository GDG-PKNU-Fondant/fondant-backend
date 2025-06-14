package com.fondant.infra.s3.application;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${s3.images-bucket}")
    private String bucket;

    public String uploadReviewImage(MultipartFile file) {
        return uploadFile(file, "review");
    }

    private String uploadFile(MultipartFile file, String dirName) {
        String key = dirName + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try (InputStream input = file.getInputStream()) {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(input, file.getSize()));
            return s3Client.utilities().getUrl(b -> b.bucket(bucket).key(key)).toExternalForm();
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }}
}
