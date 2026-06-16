package com.chj.utils;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Component
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioUtil {

    private String endpoint;

    private String bucketName;

    private String accessKey;

    private String secretKey;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO bucket初始化失败: " + e.getMessage(), e);
        }
    }

    public String upload(MultipartFile file) throws Exception {
        String objectName = UUID.randomUUID()
                + file.getOriginalFilename().substring(
                        file.getOriginalFilename().lastIndexOf("."));

        InputStream inputStream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        String baseEndpoint = endpoint.endsWith("/")
                ? endpoint.substring(0, endpoint.length() - 1)
                : endpoint;
        return baseEndpoint + "/" + bucketName + "/" + objectName;
    }

    public String upload(InputStream inputStream, String fileName) throws Exception {
        String objectName="images/"+fileName;
        getMinioClient().putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream,-1,5*1024*1024)
                        .contentType("image/png")
                        .build()
        );
        String baseEndpoint = endpoint.endsWith("/")
                ? endpoint.substring(0, endpoint.length() - 1)
                : endpoint;
        return baseEndpoint + "/" + bucketName + "/" + objectName;
    }
}
