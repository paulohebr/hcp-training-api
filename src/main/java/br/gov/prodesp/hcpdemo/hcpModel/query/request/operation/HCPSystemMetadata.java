package br.gov.prodesp.hcpdemo.hcpModel.query.request.operation;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HCPSystemMetadata {
    private HCPChangeTime changeTime;
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<HCPQueryDirectory> directories;
    private Boolean indexable;
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<HCPNamespace> namespaces;
    private Boolean replicationCollision;
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<HCPTransaction> transactions;
}
