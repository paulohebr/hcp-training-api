package br.gov.prodesp.hcpdemo.hcpModel.query.request.expression;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class HCPExpressionHelper {
    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static String formatValue(Object value) {
        if (value instanceof Number){
            Number number = (Number) value;
            if (number.intValue() < 0){
                return wrapWithDoubleQuotes(number.toString());
            }
        } else if (value instanceof TemporalAccessor){
            return formatTemporalAccessor((TemporalAccessor) value);
        }
        return value.toString();
    }

    public static String formatTemporalAccessor(TemporalAccessor time){
        return dateTimeFormatter.format(time);
    }

    public static String wrapWithDoubleQuotes(String value){
        return "\"" + value + "\"";
    }

    public static String formatOperator(HCPCriteriaOperator operator) {
        switch (operator){
            case MUST:
                return "+";
            case NOT:
                return "-";
            default:
                return "";
        }
    }
}
