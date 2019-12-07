package br.gov.prodesp.hcpdemo.hcpModel;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HCPZonedDateTimeJackson {

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static class Serializer extends StdSerializer<ZonedDateTime> {

        public Serializer() {
            super(ZonedDateTime.class);
        }

        @Override
        public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.format(dateTimeFormatter));
        }
    }

    public static class Deserializer extends StdDeserializer<ZonedDateTime> {

        public Deserializer() {
            super(ZonedDateTime.class);
        }

        @Override
        public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String str = p.getCodec().readValue(p, String.class);
            return ZonedDateTime.parse(str, dateTimeFormatter);
        }
    }
}
