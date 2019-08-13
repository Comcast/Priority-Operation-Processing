package com.cts.fission.scheduling.queue.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;

public class AWSLambdaFactory
{
    private final AWSCredentialsProvider awsCredentialsProvider;
    private Regions region = Regions.DEFAULT_REGION;

    public AWSLambdaFactory(AWSCredentialsProvider awsCredentialsProvider)
    {
        this.awsCredentialsProvider = awsCredentialsProvider;
    }

    public AWSLambdaFactory()
    {
        awsCredentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
    }

    public AWSLambda create()
    {
        if(region == null)
            region = Regions.fromName(Regions.getCurrentRegion().getName());
        return AWSLambdaClient.builder()
            .withCredentials(awsCredentialsProvider)
            .withRegion(region)
            .build();
    }

    public AWSLambdaFactory setRegion(Regions region)
    {
        this.region = region;
        return this;
    }
}
