package br.gov.prodesp.hcpdemo.hcpModel;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public static HCPObject.HCPObjectBuilder fromHeaders(ClientResponse.Headers headers, String urlName){
        long size = Long.parseLong(headers.header("X-HCP-Size").stream()
                .findFirst().orElseThrow(() -> new RuntimeException("No X-HCP-Size header present")));
        String hash = headers.header("X-HCP-Hash").stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No X-HCP-Hash header present")).substring(21);
        Instant created = Instant.ofEpochSecond(Long.parseLong(headers.header("X-HCP-IngestTime").stream()
                .findFirst().orElseThrow(() -> new RuntimeException("No X-HCP-IngestTime present"))));
        String changeTimeMilliStr = headers.header("X-HCP-ChangeTimeMilliseconds")
                .stream().findFirst().orElseThrow(() -> new RuntimeException("No X-HCP-ChangeTimeMilliseconds header present"));
        int dotIndex = changeTimeMilliStr.lastIndexOf(".");
        changeTimeMilliStr = changeTimeMilliStr.substring(0, dotIndex);
        Instant changed = Instant.ofEpochMilli(Long.parseLong(changeTimeMilliStr));
        return HCPObject.builder()
                .urlName(urlName)
                .size(size)
                .hash(hash)
                .created(created)
                .changed(changed);
    }

    public static HCPObject fromHeadersWithAnnotation(ClientResponse.Headers headers, String urlName, String annotation, ObjectMapper objectMapper) {
        HCPObjectBuilder hcpObjectBuilder = fromHeaders(headers, urlName);

        return null;
    }
}
