package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import org.apache.commons.lang3.StringUtils;

import static com.theplatform.commandbuilder.impl.build.handler.connect.s3.S3Tokens.*;

public class S3TypeUtil
{


    public static boolean isParsable(String url)
    {
        if(StringUtils.isEmpty(url) || missingTokens(url) || invalidSplits(url))
        {
            return false;
        }
        return url.split(AMAZON_AWS_TOKEN)[0].split(SCHEME_TOKEN)[1].contains(S3_TOKEN);
    }

    private static boolean invalidSplits(String url)
    {
        return url.split(AMAZON_AWS_TOKEN).length < 2 ||
                url.split(AMAZON_AWS_TOKEN)[0].split(SCHEME_TOKEN).length < 2 ||
                url.split(S3_TOKEN).length == 0 || url.split(S3_TOKEN)[0].split(SCHEME_TOKEN).length < 2;
    }

    private static boolean missingTokens(String url)
    {
        return !url.contains(S3_TOKEN) || !url.contains(AMAZON_AWS_TOKEN) || !url.contains(SCHEME_TOKEN);
    }
}
