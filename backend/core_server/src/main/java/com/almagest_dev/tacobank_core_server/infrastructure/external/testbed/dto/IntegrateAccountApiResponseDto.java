package com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto;

import lombok.Data;
import java.util.List;

@Data
public class IntegrateAccountApiResponseDto {

    private String apiTranId;
    private String userFinanceId;
    private String apiTranDtm;
    private String rspCode;
    private String traceNo;
    private String rspMessage;
    private String ainfoTranId;
    private String ainfoTranDate;
    private String rspType;
    private String ainfoRspCode;
    private String ainfoRspMessage;
    private String inquiryBankType;
    private String orgAinfoTranId;
    private List<AccountInfoDto> resList;
}
