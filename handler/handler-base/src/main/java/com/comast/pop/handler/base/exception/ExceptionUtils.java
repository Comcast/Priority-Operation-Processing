package com.comast.pop.handler.base.exception;

import java.util.Arrays;

public class ExceptionUtils
{
    private ExceptionUtils(){}

    public static String getStackTraceString(Exception e)
    {
        return Arrays.toString(e.getStackTrace());
    }
}
