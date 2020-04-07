package com.comcast.pop.endpoint.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ErrorResponseCode
{
    BAD_REQUEST(400),
    UNAUTHORIZED(403),
    OBJECT_NOT_FOUND(404);

    private final int number;

    ErrorResponseCode(int number)
    {
        this.number = number;
    }

    public int getNumber()
    {
        return number;
    }

    private static final Map<Integer,ErrorResponseCode> lookup = new HashMap<>();

    static {
        for(ErrorResponseCode e : EnumSet.allOf(ErrorResponseCode.class))
            lookup.put(e.getNumber(), e);
    }

    public static ErrorResponseCode getFromCode(int responseCode)
    {
        return lookup.get(responseCode);
    }

}
