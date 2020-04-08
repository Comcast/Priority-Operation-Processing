package com.comcast.pop.commandbuilder.impl.build.handler.connect.s3;

import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class S3CommandGeneratorBuilderTest extends S3TestBase
{

    @BeforeTest
    public void init()
    {
        makeConnectData();
    }


    @Test
    public void testMakeCommandGenerator()
    {
        S3CommandGeneratorBuilder s3CommandGeneratorBuilder = new S3CommandGeneratorBuilder(keyConversion);

        Optional<ConnectGenerator> optionalCommandGenerator = s3CommandGeneratorBuilder.makeCommandGenerator(connectData);
        Assertions.assertThat(optionalCommandGenerator).isPresent();
    }

    @Test
    public void testMakeCommandGeneratorBuilderWithNonS3Input()
    {
        S3CommandGeneratorBuilder s3CommandGeneratorBuilder = new S3CommandGeneratorBuilder(keyConversion);
        Optional<ConnectGenerator> optionalCommandGenerator = s3CommandGeneratorBuilder.makeCommandGenerator(nonS3connectData);
        Assertions.assertThat(optionalCommandGenerator).isNotPresent();
    }
}