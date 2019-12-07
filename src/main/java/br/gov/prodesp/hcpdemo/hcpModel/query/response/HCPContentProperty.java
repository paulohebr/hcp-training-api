package br.gov.prodesp.hcpdemo.hcpModel.query.response;

import lombok.Data;

@Data
public class HCPContentProperty {
    private String name;
    private String expression;
    private String type;
    private Boolean multivalued;
    private String format;
}
