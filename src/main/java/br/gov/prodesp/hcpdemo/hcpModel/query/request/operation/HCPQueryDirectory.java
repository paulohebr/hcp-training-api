package br.gov.prodesp.hcpdemo.hcpModel.query.request.operation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HCPQueryDirectory {
    private String directory;
    private List<HCPNamespace> namespaces;
}
