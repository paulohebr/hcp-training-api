package br.gov.prodesp.hcpdemo.hcpModel.query.request;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.util.StdConverter;

import java.io.IOException;
import java.util.List;

public class HCPObjectPropertiesSerializerConverter extends StdConverter<List<String>, String> {

    @Override
    public String convert(List<String> value) {
        if (value == null || value.isEmpty()){
            return null;
        }
        return String.join(",", value);
    }
}
