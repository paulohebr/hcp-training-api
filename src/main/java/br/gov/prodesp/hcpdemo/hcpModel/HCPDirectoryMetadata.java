package br.gov.prodesp.hcpdemo.hcpModel;

import lombok.Data;

import java.util.List;

@Data
public class HCPDirectoryMetadata {
    private String path;
    private List<HCPObject> objects;
}
