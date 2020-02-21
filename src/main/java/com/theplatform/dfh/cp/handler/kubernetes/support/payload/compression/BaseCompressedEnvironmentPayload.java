package com.theplatform.dfh.cp.handler.kubernetes.support.payload.compression;

import com.theplatform.dfh.compression.zlib.ZlibUtil;


public abstract class BaseCompressedEnvironmentPayload
{
    protected static final String PAYLOAD_FIELD_FORMAT = "FISSION_COMPRESSED_PAYLOAD_%1$s";

    protected ZlibUtil zlibUtil = new ZlibUtil();

    protected static String getEnvironmentVariableName(int index)
    {
        return String.format(PAYLOAD_FIELD_FORMAT, index);
    }

    public void setZlibUtil(ZlibUtil zlibUtil)
    {
        this.zlibUtil = zlibUtil;
    }
}
