package com.almagest_dev.tacobank_core_server;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.io.File;

@SpringBootApplication
@EnableScheduling
public class TacobankCoreServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TacobankCoreServerApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@PostConstruct
	public void init() {
		// 애플리케이션 루트 디렉토리 하위에 log 폴더 생성
		File logDir = new File("log");
		if (!logDir.exists()) {
			logDir.mkdirs();
		}
	}

}
