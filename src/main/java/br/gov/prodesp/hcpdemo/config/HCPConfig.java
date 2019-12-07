package br.gov.prodesp.hcpdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "hcp")
public class HCPConfig {
    private String namespace;
    private String tenant;
    private String hostname;
    private String ip;
    private String auth;
    private String schema = "https";

    public String getTenantHostname(){
        return tenant + "." + hostname;
    }

    public String getNamespaceHostname(){
        return namespace + "." + getTenantHostname();
    }
}
