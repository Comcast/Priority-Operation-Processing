package com.cts.fission.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 */
public class CidGenerator
{
    private final static Logger logger = LoggerFactory.getLogger(CidGenerator.class);
    private static String suffixSeparator = "-";

    /**
     * Creates a CID with the class and method name as the suffix
     * @param method The method to pull the class and method name from.
     * @return New CID
     */
    public static String generateCid(Method method)
    {
        // NOTE/TODO: It would be nice to get the lowest class but we get the base... (because that's where the methods with @Test are!)
        String cid = UUID.randomUUID().toString() + suffixSeparator + method.getDeclaringClass().getSimpleName() + "." + method.getName();
        logger.info("Generated CID: {}", cid);
        return cid;
    }

    public static String generateCid()
    {
        String cid = UUID.randomUUID().toString();
        logger.info("Generated CID: {}", cid);
        return cid;
    }

    public static String getSuffixSeparator()
    {
        return suffixSeparator;
    }

    public static void setSuffixSeparator(String separator)
    {
        suffixSeparator = separator;
    }
}
