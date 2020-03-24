package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

/**
 * Local dynamo client (uses the profile credentials provider)
 *
 * This is intended for local testing only with aws credentials.
 * http://tpconfluence.corp.theplatform.com/display/TD/Local+AWS+Testing+and+Java
 */
public class LocalDynamoDBFactory extends AWSDynamoDBFactory
{
    private String profileName;
    private Regions region;

    public LocalDynamoDBFactory(String profileId, Regions region)
    {
        this.profileName = profileId;
        this.region = region;
    }

    @Override
    public AmazonDynamoDB getAmazonDynamoDB()
    {
        return AmazonDynamoDBClientBuilder.standard()
            .withCredentials(new ProfileCredentialsProvider(profileName))
            .withRegion(region)
            .build();
    }

    public String getProfileName()
    {
        return profileName;
    }

    public LocalDynamoDBFactory setProfileName(String profileName)
    {
        this.profileName = profileName;
        return this;
    }

    public Regions getRegion()
    {
        return region;
    }

    public LocalDynamoDBFactory setRegion(Regions region)
    {
        this.region = region;
        return this;
    }
}
