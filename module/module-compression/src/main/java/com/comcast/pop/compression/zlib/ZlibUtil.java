package com.comcast.pop.compression.zlib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class ZlibUtil
{
    public ZlibUtil(){}

    /**
     * Standard Java Inflator
     *
     * @param array binary, deflated
     * @return out binary, inflated
     */
    public String inflateMe(byte[] array)
    {
        int startingSize = 128;
        ByteArrayOutputStream flexibleBuffer = new ByteArrayOutputStream(startingSize);
        try (InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(flexibleBuffer))
        {
            inflaterOutputStream.write(array);
            inflaterOutputStream.finish();
            inflaterOutputStream.flush();
            byte[] bytes = flexibleBuffer.toByteArray();
            return new String(bytes, 0, bytes.length, "UTF-8");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Standard Java Deflator, binary in (inflated), binary out (deflated)
     *
     * @param in binary, inflated
     * @return out binary, deflated
     */
    public byte[] deflateMe(byte[] in)
    {
        int startingSize = 128;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(startingSize);
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream);
        try
        {
            deflaterOutputStream.write(in);
            deflaterOutputStream.finish();
            deflaterOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try
            {
                deflaterOutputStream.close();
            }
            catch (IOException e)
            {
                // oh please!
            }
        }
    }
}
