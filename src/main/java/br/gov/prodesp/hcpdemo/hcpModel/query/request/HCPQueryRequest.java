package br.gov.prodesp.hcpdemo.hcpModel.query.request;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "queryRequest")
public class HCPQueryRequest {
    private final HCPQueryObject object;
}
