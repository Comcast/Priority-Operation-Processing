package com.comcast.pop.compression.zlib;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ZlibUtilTest
{
    private ZlibUtil zlibUtil;

    @BeforeMethod
    public void setup()
    {
        zlibUtil = new ZlibUtil();
    }

    @Test
    public void testInflateDeflate() throws Exception
    {
        String original = "{\"foo\":\"bar\"}";
        byte[] in = original.getBytes("UTF-8");
        byte[] b = zlibUtil.deflateMe(in);
        String json = zlibUtil.inflateMe(b);
        Assert.assertEquals(json, original);
    }
}
