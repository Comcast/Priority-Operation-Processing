package com.comcast.pop.commandbuilder.impl.build.handler;

import com.comcast.pop.commandbuilder.impl.build.handler.connect.ConnectionCommandBuilderFactory;
import com.comcast.pop.commandbuilder.impl.build.handler.connect.s3.S3TestBase;
import com.comcast.pop.commandbuilder.impl.command.api.CommandGeneratorBuilder;
import com.comcast.pop.commandbuilder.impl.build.handler.connect.ConnectData;
import com.comcast.pop.commandbuilder.impl.build.handler.connect.NoopConnectCommandGenerator;
import com.comcast.pop.commandbuilder.impl.build.handler.connect.s3.ConnectGenerator;
import com.comcast.pop.commandbuilder.impl.build.handler.connect.s3.S3CommandGenerator;
import com.comcast.pop.commandbuilder.impl.build.handler.connect.s3.S3CommandGeneratorBuilder;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionCommandBuilderFactoryTest extends S3TestBase
{

    S3TestBase s3TestBase = new S3TestBase();

    @BeforeTest
    public void init()
    {
        s3TestBase.makeConnectData();
    }

    @Test
    public void testMakeConnectionCommandBuilder()
    {
        CommandGeneratorBuilder<ConnectGenerator, ConnectData> s3builder = new S3CommandGeneratorBuilder(keyConversion);
        ConnectionCommandBuilderFactory factory = new ConnectionCommandBuilderFactory(s3TestBase.connectData, s3builder);
        Optional<ConnectGenerator> optionalCommandGenerator = factory.makeConnectionCommandBuilder();
        assertThat(optionalCommandGenerator).isPresent();
        assertThat(optionalCommandGenerator).get().isInstanceOf(S3CommandGenerator.class);
    }

    @Test
    public void testMakeConnectionCommandBuilderWithNonS3Input()
    {
        CommandGeneratorBuilder<ConnectGenerator, ConnectData> s3builder = new S3CommandGeneratorBuilder(keyConversion);
        ConnectionCommandBuilderFactory factory = new ConnectionCommandBuilderFactory(s3TestBase.nonS3connectData, s3builder);
        Optional<ConnectGenerator> optionalCommandGenerator = factory.makeConnectionCommandBuilder();
        assertThat(optionalCommandGenerator).isPresent();
        assertThat(optionalCommandGenerator).get().isInstanceOf(NoopConnectCommandGenerator.class);
    }
}