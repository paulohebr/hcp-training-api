package br.gov.prodesp.hcpdemo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "hcp")
public class MainConfig {
    private String host;
    private String auth;
    private String schema = "https";
}