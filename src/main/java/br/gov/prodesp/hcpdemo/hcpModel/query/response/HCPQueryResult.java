package br.gov.prodesp.hcpdemo.hcpModel.query.response;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement(name = "queryResult")
public class HCPQueryResult {
    private HCPQuery query;
    private List<HCPResult> resultSet;
    private HCPStatus status;
    private List<HCPContentProperty> contentProperties;
    private List<HCPFacetResult> facets;
}
