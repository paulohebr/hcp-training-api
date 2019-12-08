package br.gov.prodesp.hcpdemo.hcpModel.query.request.expression;

import java.util.Objects;

public class HCPRangeCriteria extends HCPCriteria {
    private final Object valueStart;
    private final Object valueEnd;
    private final HCPCriteriaOperator operator;

    private HCPRangeCriteria(String property, Object valueStart, Object valueEnd, HCPCriteriaOperator operator) {
        super(property);
        this.valueStart = Objects.requireNonNull(valueStart);
        this.valueEnd = Objects.requireNonNull(valueEnd);
        this.operator = Objects.requireNonNull(operator);
    }

    @Override
    public String toString() {
        String operator = HCPExpressionHelper.formatOperator(this.operator);
        String valueStart = HCPExpressionHelper.formatValue(this.valueStart);
        String valueEnd = HCPExpressionHelper.formatValue(this.valueEnd);
        return operator + "(" + property + ":[" + valueStart + " TO " + valueEnd + "])";
    }

    public static class RangeBuilder {
        private Object valueStart;
        private Object valueEnd;
        private final HCPRangeCriteria.Builder builder;

        public RangeBuilder(Builder builder) {
            this.builder = builder;
        }

        public HCPRangeCriteria.RangeBuilder start(Object value) {
            this.valueStart = value;
            return this;
        }

        public HCPQueryExpressionBuilder end(Object value) {
            this.valueEnd = value;
            return this.builder.getParentBuilder();
        }
    }

    public static class Builder extends HCPCriteriaBuilder {
        private final DefaultHCPCriteriaBuilder builder;
        private final RangeBuilder rangeBuilder;
        private HCPCriteriaOperator operator;

        Builder(DefaultHCPCriteriaBuilder builder) {
            this.builder = builder;
            this.rangeBuilder = new RangeBuilder(this);
        }

        public HCPRangeCriteria.RangeBuilder must() {
            this.operator = HCPCriteriaOperator.MUST;
            return rangeBuilder;
        }

        public HCPRangeCriteria.RangeBuilder not() {
            this.operator = HCPCriteriaOperator.NOT;
            return rangeBuilder;
        }

        public HCPRangeCriteria.RangeBuilder optional() {
            this.operator = HCPCriteriaOperator.OPTIONAL;
            return rangeBuilder;
        }

        private HCPQueryExpressionBuilder getParentBuilder() {
            return this.builder.getParentBuilder();
        }

        @Override
        HCPCriteria build() {
            return new HCPRangeCriteria(this.builder.getProperty(), rangeBuilder.valueStart, rangeBuilder.valueEnd, operator);
        }
    }
}
