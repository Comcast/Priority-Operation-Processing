package com.comcast.pop.commandbuilder.impl.build.handler.connect.s3;

import com.comcast.pop.commandbuilder.impl.command.api.ExternalCommand;
import com.comcast.pop.commandbuilder.impl.build.handler.connect.NoopConnectCommandGenerator;
import com.comcast.pop.commandbuilder.impl.build.handler.connect.ConnectDataImpl;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


public class S3CommandGeneratorTest extends S3TestBase
{
    List<ExternalCommand> commands;

    @BeforeTest
    public void init()
    {
        makeConnectData();
    }

    @Test
    public void testMakeAndGenerate()
    {
        S3CommandGenerator s3CommandGenerator = new S3CommandGenerator(s3Data, s3VhsParser);
        commands = s3CommandGenerator.generateCommands();

        assertThat(commands).hasSize(4);
        assertSoftly(softly ->
        {
            softly.assertThat(credential_file_cmd().toCommandString()).contains(TEST_S3_ID);
            softly.assertThat(credential_file_cmd().toCommandString()).contains(TEST_S3_SECRET);

            softly.assertThat(credential_file_cmd().toScrubbedCommandString()).doesNotContain(TEST_S3_ID);
            softly.assertThat(credential_file_cmd().toScrubbedCommandString()).doesNotContain(TEST_S3_SECRET);

            softly.assertThat(permissions_cmd().toCommandString()).isEqualTo("chmod 600 /root/.passwd-s3fs");

            softly.assertThat(s3_connect_cmd().toCommandString()).startsWith("( s3fs ");
            softly.assertThat(s3_connect_cmd().toCommandString()).contains(bucket);
            softly.assertThat(s3_connect_cmd().toCommandString()).contains(mount);

            softly.assertThat(max_wait_cmd().toCommandString()).contains(mount + pathToFile);
        });
    }


    @Test
    public void testMakePath()
    {
        S3CommandGenerator s3CommandGenerator = new S3CommandGenerator(s3Data, s3VhsParser);
        String path = s3CommandGenerator.makeConnectionUrl(connectData);
        assertThat("\"" + path + "\"").isEqualTo(s3Data.getUrl());
    }

    @Test
    public void testPrivilege()
    {
        ConnectGenerator s3CommandGenerator = new S3CommandGenerator(s3Data, s3VhsParser);
        ConnectGenerator noopGenerator = new NoopConnectCommandGenerator(new ConnectDataImpl(null, null));
        assertSoftly(softly ->
        {
            softly.assertThat(s3CommandGenerator.needsPrivilege()).isTrue();
            softly.assertThat(noopGenerator.needsPrivilege()).isFalse();
        });
    }


    private static final String QUOTE = "\"";

    @Test
    public void testUnquote()
    {
        String unquoted = "/var/tmp/manifest.m3u8";
        String quoted = QUOTE + unquoted + QUOTE;

        String result = S3CommandGenerator.removeQuotes(quoted);
        assertThat(result).isEqualTo(unquoted);
    }

    ExternalCommand credential_file_cmd()
    {
        return commands.get(0);
    }

    ExternalCommand permissions_cmd()
    {
        return commands.get(1);
    }

    ExternalCommand s3_connect_cmd()
    {
        return commands.get(2);
    }

    ExternalCommand max_wait_cmd()
    {
        return commands.get(3);
    }
}