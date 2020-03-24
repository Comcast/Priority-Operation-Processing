package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectData;
import com.theplatform.commandbuilder.impl.build.handler.connect.KeyConversion;
import com.theplatform.commandbuilder.impl.build.handler.connect.NoopConnectKey;

import java.util.HashMap;
import java.util.Map;

import static com.theplatform.commandbuilder.impl.build.handler.connect.s3.S3CommandGenerator.S3_DEFAULT_MOUNT;
import static com.theplatform.commandbuilder.impl.build.handler.connect.s3.S3ConnectionKeys.ID;
import static com.theplatform.commandbuilder.impl.build.handler.connect.s3.S3ConnectionKeys.MOUNT;
import static com.theplatform.commandbuilder.impl.build.handler.connect.s3.S3ConnectionKeys.SECRET;
import static com.theplatform.commandbuilder.impl.build.handler.connect.s3.S3Tokens.*;

/**
 * Expecting this s3 url pattern (virtual-hosted-style) ...
 * ... 'scheme'://'s3 bucket'.s3'optional region designation'.amazonaws.com/'path to file starting with "/"'.
 * See @link: https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingBucket.html#create-bucket-intro
 *
 * There is also a 'path-style' s3 url pattern we will not support, as it is being deprecated by AWS
 * ... https://aws.amazon.com/blogs/aws/amazon-s3-path-deprecation-plan-the-rest-of-the-story/
 */

public class S3VhsConnect implements S3Connect
{
    private static final String QUOTE = "\"";

    private static final String ERROR_MESSAGE_TEMPLATE = "File url - %s - does not appear to be S3 compliant.";
    public static final String DEFAULT_MOUNT = "/var/s3";
    private  String mount = DEFAULT_MOUNT;
    private final KeyConversion keyConversion;

    public S3VhsConnect(KeyConversion keyConversion)
    {
        this.keyConversion = keyConversion;
    }

    private void init(ConnectData connectData)
    {
        convertKeys(connectData);
        Map<String,String> connectionMap = connectData.getParameters();
        mount = connectionMap.keySet().contains(MOUNT.name()) ? connectionMap.get(MOUNT.name()) : S3_DEFAULT_MOUNT;
    }

    /**
     * Will convert specified incoming key-value pairs to ones needed for s3.
     * Note that this adds the converted key-vaule pairs
     * @param connectData - pojo that may have params to be converted to s3-specific key-value pairs.
     *
     */
    protected void convertKeys(ConnectData connectData)
    {
        Map<String, String> convertedParams = new HashMap<>();
        for(String key: connectData.getParameters().keySet())
        {
            String convertedKey = keyConversion.convertKey(key).name();
            if(!convertedKey.equals(NoopConnectKey.NOOP_CONNECT_KEY))
            {
                convertedParams.put(convertedKey, connectData.getParameters().get(key));
            }
        }
        connectData.getParameters().putAll(convertedParams);
    }

    public boolean isParsable(ConnectData connectData)
    {
        return connectData != null && connectData.getUrl() != null && S3TypeUtil.isParsable(connectData.getUrl());
    }

    private String getBucket(String url)
    {
        if(!isParsable(url))
        {
            return String.format(ERROR_MESSAGE_TEMPLATE, url);
        }
        return url.split(S3_TOKEN)[0].split(SCHEME_TOKEN)[1];
    }

    private boolean isParsable(String url)
    {
        return url != null && S3TypeUtil.isParsable(url);
    }

    private String getFilePath(String url)
    {
        if(!isParsable(url))
        {
            return String.format(ERROR_MESSAGE_TEMPLATE, url);
        }
        return QUOTE + mount + url.split(AMAZON_AWS_TOKEN)[1] + QUOTE;
    }

    public S3Data makeS3Data(ConnectData connectData)
    {
        init(connectData);
        S3Data s3Data = new S3Data(
                connectData.getParameters().get(ID.name()),
                connectData.getParameters().get(SECRET.name()),
                getBucket(connectData.getUrl()),
                mount,
                getFilePath(connectData.getUrl()));
        return s3Data;
    }
}
