package br.gov.prodesp.hcpdemo.hcpModel;

import org.springframework.web.reactive.function.client.ClientResponse;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public enum HCPHeader {
    CHANGE_TIME_STRING("ChangeTimeString", String.class),
    CONTENT_ENCODING("Content-Encoding", String.class),
    CONTENT_LENGTH("Content-Length", Integer.class),
    CONTENT_RANGE("Content-Range", String.class),
    CONTENT_TYPE("Content-Type", String.class),
    ETAG("ETag", String.class),
    HCP_ACL("X-HCP-ACL", Boolean.class),
    HCP_CHANGE_TIME_MILLISECONDS("X-HCP-ChangeTimeMilliseconds", String.class),
    HCP_CHANGE_TIME_STRING_ISO("X-HCP-ChangeTimeString", ZonedDateTime.class),
    HCP_CONTENT_LENGTH("X-HCP-ContentLength", Integer.class),
    HCP_CUSTOM_METADATA("X-HCP-Custom-Metadata", Boolean.class),
    HCP_CUSTOM_METADATA_ANNOTATIONS("X-HCP-CustomMetadataAnnotations", String.class),
    HCP_CUSTOM_METADATA_CONTENT_TYPE("X-HCP-CustomMetadataContentType", String.class),
    HCP_CUSTOM_METADATA_FIRST("X-HCP-CustomMetadataFirst", Boolean.class),
    HCP_DATA_CONTENT_TYPE("X-HCP-DataContentType", String.class),
    HCP_DOMAIN("X-HCP-Domain", String.class),
    HCP_DPL("X-HCP-DPL", String.class),
    HCP_GID("X-HCP-GID", String.class),
    HCP_HASH("X-HCP-Hash", String.class),
    HCP_INDEX("X-HCP-Index", Boolean.class),
    HCP_INGEST_PROTOCOL("X-HCP-IngestProtocol", String.class),
    HCP_INGEST_TIME("X-HCP-IngestTime", Integer.class),
    HCP_LAST_VERSION_ID("X-HCP-LastVersionId", String.class),
    HCP_OWNER("X-HCP-Owner", String.class),
    HCP_REPLICATED("X-HCP-Replicated", Boolean.class),
    HCP_REPLICATION_COLLISION("X-HCP-ReplicationCollision", Boolean.class),
    HCP_RETENTION("X-HCP-Retention", Integer.class),
    HCP_RETENTION_CLASS("X-HCP-RetentionClass", String.class),
    HCP_RETENTION_HOLD("X-HCP-RetentionHold", Boolean.class),
    HCP_RETENTION_STRING("X-HCP-RetentionString", String.class),
    HCP_SHRED("X-HCP-Shred", Boolean.class),
    HCP_SIZE("X-HCP-Size", Integer.class),
    HCP_SOFTWARE_VERSION("X-HCP-SoftwareVersion", String.class),
    HCP_SYMLINK_TARGET("X-HCP-SymlinkTarget", String.class),
    HCP_TYPE("X-HCP-Type", String.class),
    HCP_UID("X-HCP-UID", String.class),
    HCP_VERSION_ID("X-HCP-VersionId", String.class);

    private String headerValue;
    private Class<?> clazz;
    private String value;

    HCPHeader(String headerValue, Class<?> clazz) {
        this.headerValue = headerValue;
        this.clazz = clazz;
    }

    public String getHeaderValue(){
        return this.headerValue;
    }

    public Class<?> getClazz(){
        return this.clazz;
    }

    public String asString() {
        return this.value;
    }

    public Integer asInteger() throws HCPHeaderTypeException {
        try {
            return Integer.parseInt(this.value);
        } catch (NumberFormatException e) {
            throw new HCPHeaderTypeException(this, e);
        }
    }

    public Long asLong() throws HCPHeaderTypeException {
        try {
            return Long.parseLong(this.value);
        } catch (NumberFormatException e) {
            throw new HCPHeaderTypeException(this, e);
        }
    }

    public Boolean asBoolean() {
        return Boolean.parseBoolean(this.value);
    }

    public Instant asInstant() throws HCPHeaderTypeException {
        try {
            long seconds;
            if (this.value.contains(".")){
                seconds = Long.parseLong(this.value.split("\\.")[0]);
            } else {
                seconds = Long.parseLong(this.value);
            }
            return Instant.ofEpochSecond(seconds);
        } catch (NumberFormatException | DateTimeException e){
            throw new HCPHeaderTypeException(this, e);
        }
    }

    public ZonedDateTime asZonedDateTime() throws HCPHeaderTypeException {
        try {
            return ZonedDateTime.parse(this.value);
        } catch (DateTimeParseException e) {
            throw new HCPHeaderTypeException(this, e);
        }
    }

    public static HCPHeader fromHeaders(HCPHeader hcpHeader, ClientResponse.Headers headers) throws HCPHeaderException {
        hcpHeader.value = headers.header(hcpHeader.headerValue).stream().findFirst().orElseThrow(() -> new HCPHeaderException(hcpHeader));
        return hcpHeader;
    }

    public static class HCPHeaderTypeException extends RuntimeException {
        public HCPHeaderTypeException(HCPHeader hcpHeader, Throwable e) {
            super("invalid HCPHeader format: " + hcpHeader.clazz, e);
        }
    }

    public static class HCPHeaderException extends RuntimeException {
        public HCPHeaderException(HCPHeader header) {
            super("no HCPHeader " + header.headerValue + " present");
        }
    }
}
