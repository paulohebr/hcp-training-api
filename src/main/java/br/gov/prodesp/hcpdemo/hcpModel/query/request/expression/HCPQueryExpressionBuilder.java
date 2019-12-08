package br.gov.prodesp.hcpdemo.hcpModel.query.request.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HCPQueryExpressionBuilder {
    private final List<HCPCriteriaBuilder> criteriaBuilders;

    private HCPQueryExpressionBuilder(List<HCPCriteriaBuilder> criteriaBuilders) {
        this.criteriaBuilders = criteriaBuilders;
    }

    public DefaultHCPCriteriaBuilder add(String property){
        DefaultHCPCriteriaBuilder defaultHCPCriteriaBuilder = new DefaultHCPCriteriaBuilder(property, this);
        this.criteriaBuilders.add(defaultHCPCriteriaBuilder);
        return defaultHCPCriteriaBuilder;
    }

    @Override
    public String toString() {
        return criteriaBuilders.stream().map(HCPCriteriaBuilder::build).map(Objects::toString).collect(Collectors.joining(" "));
    }

    public String build() {
        return toString();
    }

    public static HCPQueryExpressionBuilder builder(){
        return new HCPQueryExpressionBuilder(new ArrayList<>());
    }
}
