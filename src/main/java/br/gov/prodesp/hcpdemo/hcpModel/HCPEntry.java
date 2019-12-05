package br.gov.prodesp.hcpdemo.hcpModel;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "entry")
public class HCPEntry {
    @JacksonXmlProperty(isAttribute = true)
    private String urlName;
    @JacksonXmlProperty(isAttribute = true)
    private String type;
    @JacksonXmlProperty(isAttribute = true)
    private Long size;
    @JacksonXmlProperty(isAttribute = true)
    private String hash;
}
