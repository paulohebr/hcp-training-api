package br.gov.prodesp.hcpdemo.hcpModel;

import lombok.Data;

public enum HCPEndpoint {
    REST("rest"),
    QUERY("query");

    public String getEndpoint() {
        return endpoint;
    }

    private String endpoint;

    HCPEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
