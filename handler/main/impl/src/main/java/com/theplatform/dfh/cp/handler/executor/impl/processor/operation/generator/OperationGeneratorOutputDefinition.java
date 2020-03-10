package com.theplatform.dfh.cp.handler.executor.impl.processor.operation.generator;

/**
 * Definition extracted from the params of an operation (not the payload params, the operation params)
 *
 * TODO: In the future we may consider eliminating this as the current resident operation generator
 * may be the only one and the format may be fixed.
 */
public class OperationGeneratorOutputDefinition
{
    private String operationsJsonPointer;
    private String paramsJsonPointer;
    private Boolean logOnly;

    public OperationGeneratorOutputDefinition()
    {
    }

    public OperationGeneratorOutputDefinition(String operationsJsonPointer, String paramsJsonPointer)
    {
        this.operationsJsonPointer = operationsJsonPointer;
        this.paramsJsonPointer = paramsJsonPointer;
    }

    public OperationGeneratorOutputDefinition(String operationsJsonPointer)
    {
        this.operationsJsonPointer = operationsJsonPointer;
    }

    public String getOperationsJsonPointer()
    {
        return operationsJsonPointer;
    }

    public void setOperationsJsonPointer(String operationsJsonPointer)
    {
        this.operationsJsonPointer = operationsJsonPointer;
    }

    public String getParamsJsonPointer()
    {
        return paramsJsonPointer;
    }

    public void setParamsJsonPointer(String paramsJsonPointer)
    {
        this.paramsJsonPointer = paramsJsonPointer;
    }

    public Boolean getLogOnly()
    {
        return logOnly;
    }

    public void setLogOnly(Boolean logOnly)
    {
        this.logOnly = logOnly;
    }
}
