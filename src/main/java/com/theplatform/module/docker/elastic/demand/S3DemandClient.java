package com.theplatform.module.docker.elastic.demand;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 */
public class S3DemandClient
{
    private static Logger logger = LoggerFactory.getLogger(S3DemandClient.class);

    private String bucketName;
    private String key;
    private String keyId;
    private String secretKey;

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public void setKeyId(String keyId)
    {
        this.keyId = keyId;
    }

    public void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }

    public String getDemandJson()
    {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
            keyId, secretKey);

        AmazonS3Client amazonS3Client = new AmazonS3Client(awsCredentials);

        S3Object s3Object = amazonS3Client.getObject(bucketName, key);

        try
            (BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent())))
        {
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";

            while ((line = reader.readLine()) != null)
            {
                ;
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
        catch (IOException e)
        {
            logger.error("Failed to get Demand json", e);
            return null;
        }
    }
}
