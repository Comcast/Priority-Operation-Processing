package com.theplatform.dfh.cp.api;

public enum FileResourceType
{
    VIDEO,
    AUDIO,
    TEXT,
    IMAGE,
    PACKAGE,
    UNKNOWN;

    public static FileResourceType parse(String name)
    {
        if(name == null)
            return FileResourceType.UNKNOWN;
        return FileResourceType.valueOf(name);
    }
}
