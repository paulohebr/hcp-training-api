package br.gov.prodesp.hcpdemo.hcpModel.query.request;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.List;
import java.util.stream.Collectors;

public class HCPSort {
    private final String objectProperty;
    private final Order order;

    private HCPSort(String objectProperty, Order order) {
        this.objectProperty = objectProperty;
        this.order = order;
    }

    public static HCPSort Asc(String objectProperty) {
        return new HCPSort(objectProperty, Order.ASC);
    }

    public static HCPSort Desc(String objectProperty) {
        return new HCPSort(objectProperty, Order.DESC);
    }

    @Override
    public String toString() {
        return objectProperty + "+" + order.value;
    }

    public enum Order {
        ASC("asc"),
        DESC("desc");

        public String getValue() {
            return value;
        }

        private final String value;

        Order(String value) {
            this.value = value;
        }
    }

    public static class SerializerConverter extends StdConverter<List<HCPSort>, String> {

        @Override
        public String convert(List<HCPSort> value) {
            if (value == null){
                return null;
            }
            return value.stream().map(HCPSort::toString).collect(Collectors.joining(","));
        }
    }
}
