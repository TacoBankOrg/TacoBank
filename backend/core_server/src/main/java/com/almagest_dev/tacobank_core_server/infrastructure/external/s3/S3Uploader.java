package com.almagest_dev.tacobank_core_server.infrastructure.external.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

@Component
@Slf4j
public class S3Uploader {

    @Value("${aws.s3.bucket-name}") // S3 버킷 이름 설정
    private String bucketName;

    @Value("${aws.s3.folder}") // S3 기본 폴더 경로 설정
    private String s3Folder;

    private static final String LOCAL_LOG_PATH = "log"; // 로컬 로그 파일의 기본 경로
    private final S3Client s3Client;

    // S3 클라이언트를 생성자로 주입받음
    public S3Uploader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // 로그 파일을 S3로 업로드하는 메서드
    public void uploadLogsToS3() {
        try {
            File logBaseDir = new File(LOCAL_LOG_PATH); // 로컬 로그 기본 디렉토리 객체 생성
            if (!logBaseDir.exists() || !logBaseDir.isDirectory()) { // 디렉토리가 존재하지 않거나 디렉토리가 아닐 경우
                log.warn("Base log directory does not exist or is not a directory: {}", logBaseDir.getPath());
                return; // 업로드 중단
            }

            // 로그의 날짜별 디렉토리 탐색
            for (File dateDir : Objects.requireNonNull(logBaseDir.listFiles())) {
                if (dateDir.isDirectory()) { // 날짜별 디렉토리일 경우
                    String dateFolder = dateDir.getName(); // 날짜 폴더 이름 추출 (예: "2024-12")

                    // 날짜 디렉토리 내부 탐색
                    for (File subDirOrFile : Objects.requireNonNull(dateDir.listFiles())) {
                        if (subDirOrFile.isDirectory()) { // 하위 디렉토리일 경우
                            // transfer 폴더 처리
                            if (subDirOrFile.getName().equals("transfer")) {
                                // transfer 폴더 내부 파일을 S3에 업로드
                                uploadFilesInDirectory(subDirOrFile, s3Folder + "/transfer");
                            } else if (subDirOrFile.getName().equals("core")) {
                                // core 폴더 내부 파일을 S3에 업로드
                                uploadFilesInDirectory(subDirOrFile, s3Folder);
                                log.info(s3Folder + "/"+ dateFolder);
                            }
                        } else if (subDirOrFile.isFile()) { // 일반 파일일 경우
                            // core 폴더의 파일을 S3에 업로드
                            String s3Key = String.format("%s/%s/%s", s3Folder, dateFolder, subDirOrFile.getName());
                            uploadFileToS3(subDirOrFile, s3Key);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 업로드 중 에러 발생 시 로깅
            log.error("Error occurred during log upload to S3", e);
        }
    }

    // 특정 디렉토리의 모든 파일을 S3에 업로드하는 메서드
    private void uploadFilesInDirectory(File directory, String s3Prefix) {
        try {
            for (File logFile : Objects.requireNonNull(directory.listFiles())) {
                if (logFile.isFile()) { // 파일일 경우
                    String s3Key = String.format("%s/%s", s3Prefix, logFile.getName()); // S3 경로 생성
                    uploadFileToS3(logFile, s3Key); // 파일 업로드
                }
            }
        } catch (Exception e) {
            // 디렉토리 업로드 중 에러 발생 시 로깅
            log.error("Error occurred during directory log upload to S3: {}", directory.getPath(), e);
        }
    }

    // 개별 파일을 S3에 업로드하는 메서드
    private void uploadFileToS3(File file, String s3Key) {
        try {
            // S3에 파일 업로드
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName) // S3 버킷 설정
                            .key(s3Key) // S3 파일 경로 설정
                            .build(),
                    file.toPath() // 로컬 파일 경로
            );
            log.info("Uploaded log file to S3: {}", s3Key); // 업로드 성공 로그

            // 업로드 후 로컬 파일 삭제
            Files.delete(file.toPath());
            log.info("Deleted local log file: {}", file.getName()); // 삭제 성공 로그
        } catch (Exception e) {
            // 파일 업로드 중 에러 발생 시 로깅
            log.error("Failed to upload file to S3: {}", file.getName(), e);
        }
    }
}