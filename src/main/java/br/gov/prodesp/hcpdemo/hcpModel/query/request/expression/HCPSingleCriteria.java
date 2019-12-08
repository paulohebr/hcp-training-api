package br.gov.prodesp.hcpdemo.hcpModel.query.request.expression;

import java.util.Objects;

public class HCPSingleCriteria extends HCPCriteria {
    private final Object value;
    private final HCPCriteriaOperator operator;

    private HCPSingleCriteria(String property, Object value, HCPCriteriaOperator operator) {
        super(property);
        this.value = Objects.requireNonNull(value);
        this.operator = Objects.requireNonNull(operator);
    }

    @Override
    public String toString() {
        String operator = HCPExpressionHelper.formatOperator(this.operator);
        String value = HCPExpressionHelper.formatValue(this.value);
        return operator + "(" + property + ":" + value + ")";
    }

    public static class Builder extends HCPCriteriaBuilder {
        private final DefaultHCPCriteriaBuilder builder;
        private Object value;
        private HCPCriteriaOperator operator;

        Builder(DefaultHCPCriteriaBuilder builder) {
            this.builder = builder;
        }

        public HCPQueryExpressionBuilder must(Object value){
            this.operator = HCPCriteriaOperator.MUST;
            this.value = value;
            return this.builder.getParentBuilder();
        }

        public HCPQueryExpressionBuilder not(Object value){
            this.operator = HCPCriteriaOperator.NOT;
            this.value = value;
            return this.builder.getParentBuilder();
        }

        public HCPQueryExpressionBuilder optional(Object value){
            this.operator = HCPCriteriaOperator.OPTIONAL;
            this.value = value;
            return this.builder.getParentBuilder();
        }

        @Override
        HCPCriteria build(){
            return new HCPSingleCriteria(this.builder.getProperty(), value, operator);
        }
    }
}
