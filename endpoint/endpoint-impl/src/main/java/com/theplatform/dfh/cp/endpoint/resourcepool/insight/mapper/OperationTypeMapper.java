package com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.operation.Operation;

import java.util.List;

public class OperationTypeMapper extends AbstractInsightMapper
{
    public OperationTypeMapper()
    {
        super(InsightMapperRegistry.operationType.name());
    }

    @Override
    public boolean matches(Agenda agenda)
    {
        if(agenda == null) return false;
        List<Operation> operations = agenda.getOperations();
        if(operations == null || operations.size() == 0) return false;
        return operations.stream().anyMatch(o -> matches(o.getType()));
    }
    public OperationTypeMapper withMatchValue(String matchValue)
    {
        return (OperationTypeMapper) super.withMatchValue(matchValue);
    }
}
