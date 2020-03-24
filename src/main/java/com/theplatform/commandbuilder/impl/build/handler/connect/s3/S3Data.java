package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import com.theplatform.commandbuilder.impl.build.handler.connect.Connect;
import com.theplatform.commandbuilder.impl.build.handler.utilities.PathUtil;

public class S3Data implements Connect
{
    private final String key_id;
    private final String secret_access_key;
    private final String bucket;
    private final String mount;
    private final String url;

    public S3Data(String key_id, String secret_access_key, String bucket, String mount, String url)
    {
        this.key_id = key_id;
        this.secret_access_key = secret_access_key;
        this.bucket = bucket;
        this.mount = mount;
        this.url = PathUtil.quoteFilepath(url);
    }

    public String getKey_id()
    {
        return key_id;
    }

    public String getSecret_access_key()
    {
        return secret_access_key;
    }

    public String getBucket()
    {
        return bucket;
    }

    public String getMount()
    {
        return mount;
    }

    public String getUrl()
    {
        return url;
    }

    @Override
    public boolean needsPrivilege()
    {
        return true;
    }

}
