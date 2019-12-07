package br.gov.prodesp.hcpdemo.hcpModel.query.response;

import lombok.Data;

@Data
public class HCPResult {
    private String version;
    private String urlName;
    private String utf8Name;
    private String updateTimeString;
    private Integer updateTime;
    private String uid;
    private String type;
    private Integer size;
    private Boolean shred;
    private String retentionString;
    private String retentionClass;
    private Integer retention;
    private Boolean replicationCollision;
    private Boolean replicated;
    private String permissions;
    private String owner;
    private String operation;
    private String objectPath;
    private String namespace;
    private String ingestTimeString;
    private String ingestTime;
    private Boolean index;
    private Boolean hold;
    private String hashScheme;
    private String hash;
    private String gid;
    private Integer dpl;
    private String customMetadataAnnotation;
    private Boolean customMetadata;
    private String changeTimeString;
    private String changeTimeMilliseconds;
    private String accessTimeString;
    private Integer accessTime;
    private Boolean acl;
}
