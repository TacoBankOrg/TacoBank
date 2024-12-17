package com.almagest_dev.tacobank_core_server.infrastructure.external.naver.util;

import com.almagest_dev.tacobank_core_server.common.exception.NaverApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
public class NaverApiUtil {

    @Value("${naver.access.key}")
    private String NAVER_ACCESS_KEY;
    @Value("${naver.secret.key}")
    private String NAVER_SECRET_KEY;
    @Value("${naver.sms.service.id}")
    private String NAVER_SMS_SERVICE_ID;
    @Value("${naver.ocr.api.secret}")
    private String NAVER_OCR_SECERT;

    public String makeSignature(Long time) {
        log.info("NaverApiUtil::makeSignature START");
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/" + NAVER_SMS_SERVICE_ID + "/messages";
        String timestamp = time.toString();
        String accessKey = NAVER_ACCESS_KEY;
        String secretKey = NAVER_SECRET_KEY;

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(timestamp)
                .append(newLine)
                .append(accessKey)
                .toString();

        try {
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
            String encodeBase64String = Base64.encodeBase64String(rawHmac);

            log.info("NaverApiUtil::makeSignature END");
            return encodeBase64String;

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new NaverApiException("NaverApiUtil::makeSignature - 시그니처 생성 중 오류 발생", e);
        }
    }

    public HttpHeaders createSmsHeaders(Long time) {
        log.info("NaverApiUtil::createSmsHeaders START");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", time.toString());
        headers.set("x-ncp-iam-access-key", NAVER_ACCESS_KEY);
        headers.set("x-ncp-apigw-signature-v2", makeSignature(time));
        log.info("NaverApiUtil::createSmsHeaders END");

        return headers;
    }

    public HttpHeaders createOcrHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-OCR-SECRET", NAVER_OCR_SECERT);

        return headers;
    }
}
