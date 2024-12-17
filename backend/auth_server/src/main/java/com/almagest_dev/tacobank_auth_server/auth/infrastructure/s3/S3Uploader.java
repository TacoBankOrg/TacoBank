package com.almagest_dev.tacobank_auth_server.auth.infrastructure.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
@Slf4j
public class S3Uploader {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.folder}")
    private String s3Folder;

    private static final String LOCAL_LOG_PATH = "log";
    private final S3Client s3Client;

    public S3Uploader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void uploadLogsToS3() {
        try {
            LocalDateTime now = LocalDateTime.now();
            String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            File logDir = new File(LOCAL_LOG_PATH + "/" + currentMonth);
            if (!logDir.exists() || !logDir.isDirectory()) {
                log.warn("Log directory does not exist or is not a directory: {}", logDir.getPath());
                return;
            }

            for (File logFile : Objects.requireNonNull(logDir.listFiles())) {
                if (logFile.isFile()) {
                    String s3Key = String.format("%s/%s", s3Folder, logFile.getName());
                    s3Client.putObject(
                            PutObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(s3Key)
                                    .build(),
                            logFile.toPath()
                    );
                    log.info("Uploaded log file to S3: {}", s3Key);
                    Files.delete(logFile.toPath());
                    log.info("Deleted local log file: {}", logFile.getName());
                }
            }
        } catch (Exception e) {
            log.error("Error occurred during log upload to S3", e);
        }
    }
}