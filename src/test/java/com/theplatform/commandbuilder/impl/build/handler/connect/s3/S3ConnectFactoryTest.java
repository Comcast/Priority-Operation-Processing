package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class S3ConnectFactoryTest extends S3TestBase
{
    private Optional<S3Connect> s3Connect;

    @BeforeTest
    public void init()
    {
        makeConnectData();
    }

    @Test
    public void initTest()
    {
        s3Connect = new S3ConnectFactory(new S3VhsConnect(keyConversion)).getS3Connect(connectData);
        assertThat(s3Connect).isPresent();
    }

    @Test
    public void initNonS3Test()
    {
        s3Connect = new S3ConnectFactory(new S3VhsConnect(keyConversion)).getS3Connect(nonS3connectData);
        assertThat(s3Connect).isNotPresent();
    }

}