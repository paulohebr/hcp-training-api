package br.gov.prodesp.hcpdemo.hcpModel.query.request;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.List;
import java.util.stream.Collectors;

public class HCPFacet {
    private final HCPFacetType type;
    private String key;
    private Object value;

    private HCPFacet(HCPFacetType type) {
        this.type = type;
    }

    public HCPFacet(String key, Object value) {
        this.type = HCPFacetType.CONTENT_PROPERTY;
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        switch (type){
            case HOLD:
            case NAMESPACE:
            case RETENTION_CLASS:
            case RETENTION:
                return type.key;
            case CONTENT_PROPERTY:
                return type.key + "(" + value.toString() + ")";
            default:
                throw new RuntimeException("invalid enum: " + type);
        }
    }

    public static HCPFacet hold(){
        return new HCPFacet(HCPFacetType.HOLD);
    }

    public static HCPFacet namespace(){
        return new HCPFacet(HCPFacetType.NAMESPACE);
    }

    public static HCPFacet retentionClass(){
        return new HCPFacet(HCPFacetType.RETENTION_CLASS);
    }

    public static HCPFacet retention(){
        return new HCPFacet(HCPFacetType.RETENTION);
    }

    public static HCPFacet contentProperty(String key, Object value) {
        return new HCPFacet(key, value);
    }

    enum HCPFacetType {
        HOLD("hold"),
        NAMESPACE("namespace"),
        RETENTION("retention"),
        RETENTION_CLASS("retentionClass"),
        CONTENT_PROPERTY("");

        private final String key;

        public String getValue() {
            return value;
        }

        private String value;

        HCPFacetType(String key) {
            this.key = key;
        }
    }

    enum HCPFacetRetentionType {
        INITIAL_UNSPECIFIED("initialUnspecified"),
        NEVER_DELETABLE("neverDeletable"),
        EXPIRED("expired"),
        NOT_EXPIRED("not expired");

        private final String key;

        HCPFacetRetentionType(String key) {
            this.key = key;
        }

        public static HCPFacetRetentionType parse(String key){
            for (HCPFacetRetentionType value : HCPFacetRetentionType.values()) {
                if (value.key.equalsIgnoreCase(key)){
                    return value;
                }
            }
            throw new RuntimeException("could not parse " + key + " to a HCPFacetRetentionType");
        }
    }

    public static class SerializerConverter extends StdConverter<List<HCPFacet>, String> {
        @Override
        public String convert(List<HCPFacet> value) {
            if (value == null){
                return null;
            }
            return value.stream().map(HCPFacet::toString).collect(Collectors.joining(","));
        }
    }
}
