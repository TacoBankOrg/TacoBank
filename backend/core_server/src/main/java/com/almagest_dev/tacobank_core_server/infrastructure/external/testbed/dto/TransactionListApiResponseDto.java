package com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto;
import lombok.Data;
import java.util.List;

@Data
public class TransactionListApiResponseDto {

    private String tranNum;
    private String apiTranDtm;
    private String rspCode;
    private String rspMessage;
    private String bankCodeTran;
    private String bankName;
    private String fintechUseNum;
    private String balanceAmt;
    private List<TransactionDetailApiDto> resList;
}
