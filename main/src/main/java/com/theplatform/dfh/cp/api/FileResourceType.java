package com.theplatform.dfh.cp.api;

public enum FileResourceType
{
    video,
    audio,
    text,
    image,
    unknown;

    public static FileResourceType parse(String name)
    {
        if(name == null)
            return FileResourceType.unknown;
        return FileResourceType.valueOf(name);
    }
}
