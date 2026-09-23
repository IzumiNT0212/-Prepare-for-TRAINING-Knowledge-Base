package com.ojt.knowledgebase.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void checkConnection() {

        try {

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                throw new RuntimeException(
                        "MinIO bucket does not exist: " + bucketName
                );
            }

            System.out.println(
                    "Connected to MinIO bucket: " + bucketName
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not connect to MinIO",
                    e
            );
        }
    }

    public void upload(
            String objectName,
            MultipartFile file) {

        try {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(file.getContentType())
                            .build()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not upload file to MinIO",
                    e
            );
        }
    }

    public InputStream download(
            String objectName) {

        try {

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not download file from MinIO",
                    e
            );
        }
    }

    public void delete(
            String objectName) {

        try {

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not delete file from MinIO",
                    e
            );
        }
    }
}