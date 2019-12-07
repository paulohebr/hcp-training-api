package br.gov.prodesp.hcpdemo.hcpModel.query.response;

import lombok.Data;

@Data
public class HCPStatus {
    private String code;
    private String message;
    private Long totalResults;
    private Long results;
}
