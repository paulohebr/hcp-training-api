package br.gov.prodesp.hcpdemo.hcpModel.query.request;

import com.fasterxml.jackson.databind.util.StdConverter;

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
