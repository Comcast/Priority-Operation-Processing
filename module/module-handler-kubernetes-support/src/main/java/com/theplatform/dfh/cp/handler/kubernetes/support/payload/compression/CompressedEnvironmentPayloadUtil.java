package com.theplatform.dfh.cp.handler.kubernetes.support.payload.compression;

import com.theplatform.dfh.compression.zlib.ZlibUtil;


public class CompressedEnvironmentPayloadUtil
{
    private static final String PAYLOAD_FIELD_FORMAT = "FISSION_COMPRESSED_PAYLOAD_%1$s";

    private ZlibUtil zlibUtil = new ZlibUtil();

    public void setZlibUtil(ZlibUtil zlibUtil)
    {
        this.zlibUtil = zlibUtil;
    }

    public ZlibUtil getZlibUtil()
    {
        return zlibUtil;
    }

    public String getEnvironmentVariableName(int index)
    {
        return String.format(PAYLOAD_FIELD_FORMAT, index);
    }
}