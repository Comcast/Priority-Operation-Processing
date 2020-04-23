package com.comcast.pop.handler.executor.impl.processor.operation.generator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.ParamsMap;

import java.util.List;

public class TestGeneratedOutputObject
{
    public static final String OPERATIONS_PATH = "/generatedOperations";
    public static final String PARAMS_PATH = "/generatedParams";

    private List<Operation> operations;
    private ParamsMap params;

    public TestGeneratedOutputObject(){}

    public TestGeneratedOutputObject(List<Operation> operations, ParamsMap params)
    {
        this.operations = operations;
        this.params = params;
    }

    @JsonProperty("generatedOperations")
    public List<Operation> getOperations()
    {
        return operations;
    }

    public void setOperations(List<Operation> operations)
    {
        this.operations = operations;
    }

    @JsonProperty("generatedParams")
    public ParamsMap getParams()
    {
        return params;
    }

    public void setParams(ParamsMap params)
    {
        this.params = params;
    }
}