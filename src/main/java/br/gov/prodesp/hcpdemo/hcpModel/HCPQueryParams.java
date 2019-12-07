package br.gov.prodesp.hcpdemo.hcpModel;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;

public enum HCPQueryParams {
    TYPE_WHOLE_OBJECT("type", "whole-object"),
    TYPE_CUSTOM_METADATA("type", "custom-metadata"),
    TYPE_DIRECTORY("type", "directory"),
    ANNOTATION("annotation");


    private String key;
    private String value;

    HCPQueryParams(String key, String value) {
        this.key = key;
        this.value = value;
    }

    HCPQueryParams(String key) {
        this.key = key;
    }

    public LinkedMultiValueMap<String, String> asMultiValueMap() {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        return assignTo(queryParams);
    }

    public LinkedMultiValueMap<String, String> assignTo(LinkedMultiValueMap<String, String> multiValueMap) throws NullPointerException {
        if (StringUtils.isEmpty(this.value)) {
            throw new NullPointerException("HCPQueryParam " + this.key + " value is null or empty");
        }
        multiValueMap.add(this.key, this.value);
        return multiValueMap;
    }

    public static HCPQueryParams annotation(String value) {
        HCPQueryParams annotation = ANNOTATION;
        annotation.value = value;
        return annotation;
    }
}
