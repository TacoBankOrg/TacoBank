package com.almagest_dev.tacobank_core_server.application.service;

import com.almagest_dev.tacobank_core_server.common.exception.OcrFailedException;
import com.almagest_dev.tacobank_core_server.common.utils.JsonUtil;
import com.almagest_dev.tacobank_core_server.domain.receipt.model.Receipt;
import com.almagest_dev.tacobank_core_server.domain.receipt.model.ReceiptOcrLog;
import com.almagest_dev.tacobank_core_server.domain.receipt.model.ReceiptProduct;
import com.almagest_dev.tacobank_core_server.domain.receipt.repository.ReceiptMemberRepository;
import com.almagest_dev.tacobank_core_server.domain.receipt.repository.ReceiptOcrLogRepository;
import com.almagest_dev.tacobank_core_server.domain.receipt.repository.ReceiptProductRepository;
import com.almagest_dev.tacobank_core_server.domain.receipt.repository.ReceiptRepository;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.ocr.BaseDetail;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.ocr.NaverReceiptOcrResponseDto;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.ocr.ReceiptResult;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.client.NaverOcrApiClient;
import com.almagest_dev.tacobank_core_server.presentation.dto.receipt.ReceiptOcrResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptOcrService {
    private final ReceiptRepository receiptRepository;
    private final ReceiptProductRepository receiptProductRepository;
    private final ReceiptMemberRepository receiptMemberRepository;
    private final ReceiptOcrLogRepository receiptOcrLogRepository;

    private final NaverOcrApiClient naverOcrApiClient;

    private final JsonUtil jsonUtil;
    private final String OCR_VERSION = "V2";

    /**
     * OCR 처리 비즈니스 로직
     * @param file MultipartFile (사용자 업로드 파일)
     * @return ReceiptOcrResponseDto (OCR 결과)
     */
    public ReceiptOcrResponseDto processReceiptOcr(MultipartFile file) {
        log.info("ReceiptOcrService::processReceiptOcr START");

        // 파일 검증
        validateFile(file);

        // 파일 확장자 추출
        String imageName = file.getOriginalFilename();
        String format = extractFileExtension(imageName);

        // OCR Request 생성
        String requestId = UUID.randomUUID().toString();
        // message 메타데이터 생성
        Map<String, Object> message = Map.of(
                "version", OCR_VERSION,
                "requestId", requestId,
                "timestamp", System.currentTimeMillis(),
                "images", List.of(
                        Map.of(
                                "format", format,
                                "name", imageName
                        )
                )
        );
        log.info("ReceiptOcrService::processReceiptOcr message - {}", message);
        String jsonRequest = jsonUtil.toJsonString(message);

        // ReceiptOcrLog INSERT
        ReceiptOcrLog receiptOcrLog = ReceiptOcrLog.createReceiptOcrLog(OCR_VERSION, requestId, jsonRequest);
        receiptOcrLogRepository.save(receiptOcrLog);

        // OCR API 호출
        try {
            byte[] imageData = file.getBytes();
            log.info("ReceiptOcrService::processReceiptOcr Naver OCR API 호출 START");
            NaverReceiptOcrResponseDto apiResponse = naverOcrApiClient.sendOcrReceipt(imageData, imageName, format, jsonRequest);
            log.info("ReceiptOcrService::processReceiptOcr Naver OCR API 호출 END");
            log.info("ReceiptOcrService::processReceiptOcr Naver OCR API Response - {}", apiResponse);

            // 결과 확인
            var image = apiResponse.getImages().get(0);
            if (image == null || image.getReceipt() == null || image.getReceipt().getResult() == null) {
                throw new OcrFailedException("OCR 응답에 영수증 결과가 없습니다.");
            }

            // 응답 UPDATE
            String jsonResponse = jsonUtil.toJsonString(apiResponse);
            receiptOcrLog.updateReceiptOcrLog(
                    apiResponse.getTimestamp(),
                    image.getUid() != null          ? image.getUid() : "",
                    image.getName() != null         ? image.getName() : "",
                    image.getInferResult() != null  ? image.getInferResult() : "",
                    image.getMessage() != null      ? image.getMessage() : "",
                    image.getReceipt().getMeta().getEstimatedLanguage() != null
                            ? image.getReceipt().getMeta().getEstimatedLanguage() : "",
                    jsonUtil.toJsonString(apiResponse) != null ? jsonResponse : ""
            );
            receiptOcrLogRepository.save(receiptOcrLog);
            log.info("ReceiptOcrService::processReceiptOcr Response UPDATE");


            // 인식 결과가 FAILURE | ERROR 일 때
            if (!"SUCCESS".equals(image.getInferResult())) {
                throw new OcrFailedException(String.format("ReceiptOcrService::processReceiptOcr 인식 실패 - %s", jsonResponse));
            }

            // Receipt, ReceiptProduct 객체 세팅
            Receipt receipt = Receipt.createReceipt(requestId);
            List<ReceiptProduct> productDetails = new ArrayList<>();
            for (ReceiptResult.SubResult.Item item : image.getReceipt().getResult().getSubResults().get(0).getItems()) {
                ReceiptProduct product = ReceiptProduct.createReceiptProduct(
                        receipt,
                        item.getName().getText(),
                        Integer.parseInt(item.getCount().getText()),
                        parsePrice(item.getPrice().getUnitPrice()),
                        parsePrice(item.getPrice().getPrice())
                );
                productDetails.add(product);
            }
            // Receipt, ReceiptProduct INSERT
            List<ReceiptProduct> savedReceiptProducts = saveReceiptAndProduct(receipt, productDetails);
            log.info("ReceiptOcrService::processReceiptOcr Receipt, ReceiptProduct INSERT");
            // 영수증 총액 세팅
            int receiptTotalPrice = (image.getReceipt().getResult().getTotalPrice() != null && image.getReceipt().getResult().getTotalPrice().getPrice() != null)
                        ? parsePrice(image.getReceipt().getResult().getTotalPrice().getPrice()) : 0;

            // ReceiptOcrResponse로 변환
            ReceiptOcrResponseDto response = convertToReceiptResponseDto(savedReceiptProducts, receiptTotalPrice);
            log.info("ReceiptOcrService::processReceiptOcr ReceiptOcrResponse 변환 - {}", response);

            log.info("ReceiptOcrService::processReceiptOcr END");
            return response;

        } catch (IOException e) {
            throw new OcrFailedException("ReceiptOcrService::processReceiptOcr - 영수증 이미지 getByte() 실패", e);
        }
    }

    /**
     * Naver OCR Response를 반환할 응답(ReceiptOcrResponseDto)로 변환
     */
    private ReceiptOcrResponseDto convertToReceiptResponseDto(List<ReceiptProduct> receiptProducts, int receiptTotalPrice) {
        List<ReceiptOcrResponseDto.Item> dtoItems = new ArrayList<>();

        int number = 1; // 품목 번호는 순서대로 매김
        int sumPrice = 0; // 품목별 가격 합계
        for (ReceiptProduct product : receiptProducts) {
            // 품목 총액
            int totalPrice = (product.getTotalPrice() != null) ? product.getTotalPrice() : 0;
            sumPrice += totalPrice;

            ReceiptOcrResponseDto.Item dtoItem = ReceiptOcrResponseDto.Item.builder()
                    .productId(product.getId())
                    .number(number++) // 품목 번호
                    .name(product.getName() != null ? product.getName() : "") // 이름이 없으면 ""으로
                    .totalPrice(totalPrice)
                    .build();

            dtoItems.add(dtoItem);
        }

        // 영수증 총액 (영수증 총액이 인식되지 않는 경우, 품목별 합계로 할당)
        int totalAmount = receiptTotalPrice > 0 ? receiptTotalPrice : sumPrice > 0 ? sumPrice : 0;

        return ReceiptOcrResponseDto.builder()
                .receiptId(receiptProducts.get(0).getReceipt().getId())
                .totalAmount(totalAmount) // 총합계 금액
                .items(dtoItems) // 변환된 품목 리스트
                .build();
    }

    /**
     * 가격 정보를 파싱하여 정수로 반환. null 또는 빈 값일 경우 기본값 0 반환.
     */
    private int parsePrice(BaseDetail baseDetail) {
        if (baseDetail.getFormatted() != null && baseDetail.getFormatted().getValue() != null) {
            return Integer.parseInt(baseDetail.getFormatted().getValue().replaceAll("[^0-9]", ""));
        } else if (baseDetail.getText() != null) {
            return Integer.parseInt(baseDetail.getText().replaceAll("[^0-9]", ""));
        }
        return 0; // 기본값
    }


    /**
     * Receipt, ReceiptProduct INSERT & Return ReceiptProduct List
     */
    @Transactional
    public List<ReceiptProduct> saveReceiptAndProduct(Receipt receipt, List<ReceiptProduct> productDetails) {
        // 1. Receipt 저장
        receiptRepository.save(receipt); // Receipt 저장

        // 2. ReceiptProduct 저장
        List<ReceiptProduct> savedProducts = receiptProductRepository.saveAll(productDetails);

        return savedProducts;
    }


    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 비어 있습니다.");
        }
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().contains(".")) {
            throw new IllegalArgumentException("파일 이름에서 확장자를 추출할 수 없습니다.");
        }
        String extension = extractFileExtension(file.getOriginalFilename());
        List<String> supportedExtensions = List.of("png", "jpg", "jpeg");
        if (!supportedExtensions.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + extension);
        }
        long maxSizeInBytes = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSizeInBytes) {
            throw new IllegalArgumentException("업로드된 파일이 5MB를 초과합니다.");
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String extractFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
