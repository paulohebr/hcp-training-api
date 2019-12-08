package br.gov.prodesp.hcpdemo.hcpModel.query.request.operation;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class HCPTransaction {
    private String transaction;

    public List<HCPTransaction> asList() {
        List<HCPTransaction> result = new ArrayList<>();
        result.add(this);
        return result;
    }
}
