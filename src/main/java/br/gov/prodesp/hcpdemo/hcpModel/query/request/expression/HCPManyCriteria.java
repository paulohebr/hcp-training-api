package br.gov.prodesp.hcpdemo.hcpModel.query.request.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HCPManyCriteria extends HCPCriteria {
    private final List<HCPValueCriteria> valueList;

    private HCPManyCriteria(String property, List<HCPValueCriteria> valueList) {
        super(property);
        this.valueList = valueList;
    }

    @Override
    public String toString() {
        return property + ":(" + valueList.stream().map(Objects::toString).collect(Collectors.joining(" ")) + ")";
    }

    private static class HCPValueCriteria {
        private final Object value;
        private final HCPCriteriaOperator operator;

        private HCPValueCriteria(Object value, HCPCriteriaOperator operator) {
            this.value = Objects.requireNonNull(value);
            this.operator = Objects.requireNonNull(operator);
        }

        @Override
        public String toString() {
            String operator = HCPExpressionHelper.formatOperator(this.operator);
            String value = HCPExpressionHelper.formatValue(this.value);
            return operator + value;
        }
    }

    public static class ValueBuilder {
        private final Object value;
        private final HCPCriteriaOperator operator;

        public ValueBuilder(Object value, HCPCriteriaOperator operator) {
            this.value = value;
            this.operator = operator;
        }

        private HCPValueCriteria build() {
            return new HCPValueCriteria(value, operator);
        }
    }

    public static class Builder extends HCPCriteriaBuilder {
        private final DefaultHCPCriteriaBuilder builder;
        private final List<HCPManyCriteria.ValueBuilder> values;

        Builder(DefaultHCPCriteriaBuilder builder) {
            this.builder = builder;
            values = new ArrayList<>();
        }

        public HCPManyCriteria.Builder must(Object value) {
            ValueBuilder valueBuilder = new ValueBuilder(value, HCPCriteriaOperator.MUST);
            this.values.add(valueBuilder);
            return this;
        }

        public HCPManyCriteria.Builder not(Object value) {
            ValueBuilder valueBuilder = new ValueBuilder(value, HCPCriteriaOperator.NOT);
            this.values.add(valueBuilder);
            return this;
        }

        public HCPManyCriteria.Builder optional(Object value) {
            ValueBuilder valueBuilder = new ValueBuilder(value, HCPCriteriaOperator.OPTIONAL);
            this.values.add(valueBuilder);
            return this;
        }

        public HCPQueryExpressionBuilder done() {
            return this.builder.getParentBuilder();
        }

        @Override
        HCPCriteria build() {
            List<HCPValueCriteria> collect = values.stream().map(ValueBuilder::build).collect(Collectors.toList());
            return new HCPManyCriteria(this.builder.getProperty(), collect);
        }
    }
}
