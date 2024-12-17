package com.almagest_dev.tacobank_core_server.infrastructure.external.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogScheduler {

    private final S3Uploader s3Uploader;

    public LogScheduler(S3Uploader s3Uploader) {
        this.s3Uploader = s3Uploader;
    }

    @Scheduled(cron = "0 0 5 * * ?") // 매일 새벽 5시 실행
    public void scheduleLogUpload() {
        log.info("Starting daily log upload to S3...");
        s3Uploader.uploadLogsToS3();
        log.info("Daily log upload to S3 completed.");
    }
}
