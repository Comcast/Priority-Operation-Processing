package com.theplatform.dfh.cp.api.operation;

public enum OperationReference
{
    OUTPUT(".out");

    private final String suffix;

    OperationReference(String suffix)
    {
        this.suffix = suffix;
    }

    public String getSuffix()
    {
        return suffix;
    }
}
