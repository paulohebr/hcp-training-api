package br.gov.prodesp.hcpdemo.hcpModel;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.reactive.function.client.ClientResponse;

import java.time.Instant;

@Data
@Builder
public class HCPObject {
    private String urlName;
    private byte[] data;
    private long size;
    private String hash;
    private Instant created;
    private Instant changed;
    private Object annotation;
    private String versionId;

    public static HCPObject.HCPObjectBuilder fromHeaders(ClientResponse.Headers headers, String... path){
        HCPObjectBuilder builder = HCPObject.builder();
        builder.urlName(String.join("/", path));
        builder.size(HCPHeader.fromHeaders(HCPHeader.HCP_SIZE, headers).asLong());
        builder.hash(HCPHeader.fromHeaders(HCPHeader.HCP_HASH, headers).asString());
        try {
            builder.created(HCPHeader.fromHeaders(HCPHeader.HCP_INGEST_TIME, headers).asInstant());
        } catch (HCPHeader.HCPHeaderException ignored){}
        builder.changed(HCPHeader.fromHeaders(HCPHeader.HCP_CHANGE_TIME_MILLISECONDS, headers).asInstant());
        try {
            builder.versionId(HCPHeader.fromHeaders(HCPHeader.HCP_VERSION_ID, headers).asString());
        } catch (HCPHeader.HCPHeaderException ignored){}
        return builder;
    }
}
