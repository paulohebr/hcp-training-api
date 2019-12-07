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

        String urlName = String.join("/", path);

        long size = HCPHeader.fromHeaders(HCPHeader.HCP_SIZE, headers).asLong();

        String hash = HCPHeader.fromHeaders(HCPHeader.HCP_HASH, headers).asString();

        Instant created = HCPHeader.fromHeaders(HCPHeader.HCP_INGEST_TIME, headers).asInstant();

        Instant changed = HCPHeader.fromHeaders(HCPHeader.HCP_CHANGE_TIME_MILLISECONDS, headers).asInstant();

        String versionId = HCPHeader.fromHeaders(HCPHeader.HCP_VERSION_ID, headers).asString();

        return HCPObject.builder()
                .urlName(urlName)
                .size(size)
                .hash(hash)
                .created(created)
                .changed(changed)
                .versionId(versionId);
    }
}
