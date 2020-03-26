package com.theplatform.commandbuilder.impl.build.handler.utilities;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * This utility helps generate meaningful continuity ids; for possible inclusion in logging headers.
 * Note the usage of the environment variable 'CID'.   We could consider making this configurable.
 * If other process-metatdata is desired (for example, customer id), this utility might be a good place to handle it.
 */
public class CidUtil
{
    public static final String CID_VAR = "CID";
    public static final String DEFAULT_HANDLER = "-unspecified_service-";
    private static String HANDLER;
    private static String  SUFFIX;
    public static String TEST_CID = "no-cid-found";

    public static void setHandler(String handler)
    {
        if(HANDLER == null)
        {
            HANDLER = handler;
        }
    }

    public static synchronized String getCid()
    {
        if(SUFFIX == null)
        {
            if(HANDLER == null)
            {
                String default_handler = DEFAULT_HANDLER;
                HANDLER = default_handler;
            }
            SUFFIX = HANDLER + RandomStringUtils.randomAlphanumeric(8);
        }

        try
        {
            String cid = System.getenv(CID_VAR);
            cid = cid == null ? TEST_CID : cid;
            return cid + SUFFIX;
        }
        catch (Exception e)
        {
            return TEST_CID + SUFFIX;
        }
    }
}
