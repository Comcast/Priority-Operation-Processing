package com.comcast.pop.api.operation;

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
