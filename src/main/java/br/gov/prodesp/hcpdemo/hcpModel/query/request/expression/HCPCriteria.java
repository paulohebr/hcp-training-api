package br.gov.prodesp.hcpdemo.hcpModel.query.request.expression;

import org.springframework.util.StringUtils;

public abstract class HCPCriteria {
    protected final String property;

    protected HCPCriteria(String property) throws RuntimeException {
        if (StringUtils.isEmpty(property)){
            throw new RuntimeException("property must not be null or empty");
        }
        this.property = property;
    }

    @Override
    public abstract String toString();
}
