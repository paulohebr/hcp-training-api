package br.gov.prodesp.hcpdemo.hcpModel.query.request;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Builder;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Builder
@JsonSerialize(using = HCPQueryObject.Serializer.class)
public class HCPQueryObject {

    private String query;

    private Boolean contentProperties;

    private List<String> objectProperties;

    private Integer count;

    private Long offset;

    private List<HCPFacet> facets;

    private List<HCPSort> sort;

    private Boolean verbose;

    public HCPQueryRequest toRequest() {
        return new HCPQueryRequest(this, null);
    }

    public static class Serializer extends StdSerializer<HCPQueryObject> {

        protected Serializer() {
            super(HCPQueryObject.class);
        }

        @Override
        public void serialize(HCPQueryObject value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            if (!StringUtils.isEmpty(value.query)) {
                gen.writeStringField("query", value.query);
            }
            if (value.contentProperties != null) {
                gen.writeBooleanField("contentProperties", value.contentProperties);
            }
            if (value.objectProperties != null && !value.objectProperties.isEmpty()) {
                gen.writeStringField("objectProperties", String.join(",", value.objectProperties));
            }
            if (value.count != null) {
                gen.writeNumberField("count", value.count);
            }
            if (value.offset != null) {
                gen.writeNumberField("offset", value.offset);
            }
            if (value.facets != null && !value.facets.isEmpty()){
                gen.writeStringField("facets", value.facets.stream().map(Objects::toString).collect(Collectors.joining(",")));
            }
            if (value.sort != null && !value.sort.isEmpty()){
                gen.writeStringField("sort", value.sort.stream().map(Objects::toString).collect(Collectors.joining(",")));
            }
            if (value.verbose != null){
                gen.writeBooleanField("verbose", value.verbose);
            }
            gen.writeEndObject();
        }
    }
}
