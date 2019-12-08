package br.gov.prodesp.hcpdemo.hcpModel.query.request.expression;

import java.util.Objects;

public class DefaultHCPCriteriaBuilder extends HCPCriteriaBuilder {
    private final HCPQueryExpressionBuilder expressionBuilder;
    private final String property;
    private HCPCriteriaBuilder criteriaBuilder;

    DefaultHCPCriteriaBuilder(String property, HCPQueryExpressionBuilder expressionBuilder) {
        this.property = property;
        this.expressionBuilder = expressionBuilder;
    }

    public HCPSingleCriteria.Builder single(){
        HCPSingleCriteria.Builder criteriaBuilder = new HCPSingleCriteria.Builder(this);
        this.criteriaBuilder = criteriaBuilder;
        return criteriaBuilder;
    }

    public HCPRangeCriteria.Builder range(){
        HCPRangeCriteria.Builder criteriaBuilder = new HCPRangeCriteria.Builder(this);
        this.criteriaBuilder = criteriaBuilder;
        return criteriaBuilder;
    }

    public HCPManyCriteria.Builder many(){
        HCPManyCriteria.Builder criteriaBuilder = new HCPManyCriteria.Builder(this);
        this.criteriaBuilder = criteriaBuilder;
        return criteriaBuilder;
    }

    @Override
    HCPCriteria build(){
        return Objects.requireNonNull(criteriaBuilder).build();
    }

    String getProperty() {
        return property;
    }

    HCPQueryExpressionBuilder getParentBuilder() {
        return this.expressionBuilder;
    }
}
