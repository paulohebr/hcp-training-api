package br.gov.prodesp.hcpdemo.hcpModel.query.response;

import lombok.Data;

@Data
public class HCPQuery {
    private String expression;
    private Long start;
    private Long end;
}
