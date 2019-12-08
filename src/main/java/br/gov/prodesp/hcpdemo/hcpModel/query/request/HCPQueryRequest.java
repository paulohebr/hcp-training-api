package br.gov.prodesp.hcpdemo.hcpModel.query.request;

import br.gov.prodesp.hcpdemo.hcpModel.query.request.operation.HCPQueryOperation;
import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "queryRequest")
public class HCPQueryRequest {
    private final HCPQueryObject object;
    private final HCPQueryOperation operation;
}
