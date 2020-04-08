package com.comcast.pop.commandbuilder.impl.build.handler.utilities;

public class PathUtil
{
    private static final String QUOTE = "\"";

    public static String quoteFilepath(String filepath)
    {
        if(filepath == null || filepath.length() == 0)
        {
            return filepath;
        }
        if(!filepath.startsWith("\""))
        {
            filepath = QUOTE + filepath + QUOTE;
        }
        return filepath;
    }
}
