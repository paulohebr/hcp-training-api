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
    private String utf8Name;

    @JacksonXmlProperty(isAttribute = true)
    private String type;

    @JacksonXmlProperty(isAttribute = true)
    private Long size;

    @JacksonXmlProperty(isAttribute = true)
    private String hash;

    @JacksonXmlProperty(isAttribute = true)
    private String hashScheme;

    @JacksonXmlProperty(isAttribute = true)
    private String etag;

    @JacksonXmlProperty(isAttribute = true)
    private Integer retention;

    @JacksonXmlProperty(isAttribute = true)
    private String retentionString;

    @JacksonXmlProperty(isAttribute = true)
    private String retentionClass;

    @JacksonXmlProperty(isAttribute = true)
    private Integer ingestTime;

    @JacksonXmlProperty(isAttribute = true)
    private Long versionCreateTimeMilliseconds;

    @JacksonXmlProperty(isAttribute = true)
    private String ingestTimeString;

    @JacksonXmlProperty(isAttribute = true)
    private Boolean hold;

    @JacksonXmlProperty(isAttribute = true)
    private Boolean shred;

    @JacksonXmlProperty(isAttribute = true)
    private Integer dpl;

    @JacksonXmlProperty(isAttribute = true)
    private Boolean index;

    @JacksonXmlProperty(isAttribute = true)
    private Boolean customMetadata;

    @JacksonXmlProperty(isAttribute = true)
    private String customMetadataAnnotations;

    @JacksonXmlProperty(isAttribute = true)
    private Long version;

    @JacksonXmlProperty(isAttribute = true)
    private Boolean replicated;

    @JacksonXmlProperty(isAttribute = true)
    private Integer accessTime;

    @JacksonXmlProperty(isAttribute = true)
    private String accessTimeString;

    @JacksonXmlProperty(isAttribute = true)
    private Integer modTime;

    @JacksonXmlProperty(isAttribute = true)
    private String modTimeString;

    @JacksonXmlProperty(isAttribute = true)
    private String changeTimeMilliseconds;

    @JacksonXmlProperty(isAttribute = true)
    private String changeTimeString;

    @JacksonXmlProperty(isAttribute = true)
    private String owner;

    @JacksonXmlProperty(isAttribute = true)
    private String domain;

    @JacksonXmlProperty(isAttribute = true)
    private Boolean hasAcl;

    @JacksonXmlProperty(isAttribute = true)
    private String state;
}
