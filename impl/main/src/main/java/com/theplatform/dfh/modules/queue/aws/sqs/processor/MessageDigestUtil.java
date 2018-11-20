package com.theplatform.dfh.modules.queue.aws.sqs.processor;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestUtil
{
    /**
     * Creates an MD5 string based on the input
     * @param input String to convert to MD5
     * @return The MD5 version of the input string
     * @throws NoSuchAlgorithmException
     */
    protected String getMD5String(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input.getBytes("UTF-8"));
        byte[] digest = md.digest();
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : digest)
        {
            stringBuilder.append(String.format("%02x", b & 0xff));
        }
        return stringBuilder.toString();
    }
}
