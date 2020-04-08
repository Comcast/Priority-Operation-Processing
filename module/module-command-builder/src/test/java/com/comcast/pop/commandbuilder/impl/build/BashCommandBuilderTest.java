package com.comcast.pop.commandbuilder.impl.build;

import com.comcast.pop.commandbuilder.api.CommandExceptionFactory;
import com.comcast.pop.commandbuilder.impl.command.CommandEchoHelper;
import com.comcast.pop.commandbuilder.impl.command.TestCommand;
import com.comcast.pop.commandbuilder.impl.command.TestPhase;
import com.comcast.pop.commandbuilder.impl.command.api.ExternalCommand;
import com.comcast.pop.commandbuilder.impl.validate.exception.CommandValidationExceptionFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class BashCommandBuilderTest
{
    @Test
    public void testAdd()
    {
        BashCommandBuilder builder = new BashCommandBuilder();

        builder.add("sudo mkdir newdir");
        String[] commands = builder.build();

        assertThat(commands.length).isEqualTo(1);
        assertSoftly(softly ->
        {
            softly.assertThat(commands[0]).contains("mkdir ");
        });
    }


    @Test
    public void testThrowException()
    {
        BashCommandBuilder builder = new BashCommandBuilder();
        CommandExceptionFactory exceptionFactory = new CommandValidationExceptionFactory("test message");
        builder.setValidator(new TestFailValidator())
                .setCommandExceptionFactory(exceptionFactory);

        Throwable thrown = catchThrowable(() -> {builder.add("sudo mkdir newdir");});

        assertThat(thrown).isNotNull();
        assertSoftly(softly ->
        {
            softly.assertThat(thrown.getMessage()).contains("test message");
            softly.assertThat(thrown.getMessage()).contains("sudo mkdir newdir");
        });
    }

    @Test
    public void testAddAll()
    {
        BashCommandBuilder builder = new BashCommandBuilder();

        builder.addAll( new String[]{"sudo mkdir newdir", " ; ", "ls -l", " && ", "rm -rf *", " || ", "echo 'rats!'"});
        String[] commands = builder.build();

        assertThat(commands.length).isEqualTo(1);
        assertSoftly(softly ->
        {
            softly.assertThat(commands[0]).contains("mkdir ");
            softly.assertThat(commands[0]).contains(" ls ");
            softly.assertThat(commands[0]).contains(" rm ");
            softly.assertThat(commands[0]).contains("echo ");
        });
    }

    @Test
    public void testNewBlock()
    {
        BashCommandBuilder builder = new BashCommandBuilder();

        builder.add( "ash")
                .newBlock()
                .add(" -c ")
                .newBlock()
                .add("ls -l");

        String[] commands = builder.build();

        assertThat(commands.length).isEqualTo(3);
        assertSoftly(softly ->
        {
            softly.assertThat(commands[0]).contains("ash");
            softly.assertThat(commands[1]).contains(" -c ");
            softly.assertThat(commands[2]).contains("ls ");
        });
    }

    @Test
    public void testSetExecAll()
    {
        BashCommandBuilder builder = new BashCommandBuilder();

        builder.setExecAll()
                .addAll( new String[]{"sudo mkdir newdir", "ls -l", "rm -rf *", "echo 'rats!'"});
        String[] commands = builder.build();

        assertThat(commands.length).isEqualTo(1);
        assertSoftly(softly ->
                softly.assertThat(commands[0].split("; ").length).isEqualTo(4));
    }

    @Test
    public void testSetExecAny()
    {
        BashCommandBuilder builder = new BashCommandBuilder();

        builder.setExecAny()
                .addAll( new String[]{"sudo mkdir newdir", "ls -l", "rm -rf *", "echo 'rats!'"});
        String[] commands = builder.build();

        assertThat(commands.length).isEqualTo(1);
        assertSoftly(softly ->
        {
                softly.assertThat(commands[0].split(Pattern.quote("|| ")).length).isEqualTo(4);

        });
    }

    @Test
    public void testSetExecUntilFail()
    {
        BashCommandBuilder builder = new BashCommandBuilder();

        builder.setExecUntilFail()
                .addAll( new String[]{"sudo mkdir newdir", "ls -l", "rm -rf *", "echo 'rats!'"});
        String[] commands = builder.build();

        assertThat(commands.length).isEqualTo(1);
        assertSoftly(softly ->
        {
            softly.assertThat(commands[0].split("&& ").length).isEqualTo(4);
        });
    }

    @Test
    public void testChangeSeparator()
    {
        BashCommandBuilder builder = new BashCommandBuilder();

        builder.setExecUntilFail()
                .addAll(new String[]{"sudo mkdir newdir", "ls -l"})
                .setExecAll()
                .addAll(new String[]{"rm -rf *", "echo 'rats!'"});
        String[] commands = builder.build();

        assertThat(commands.length).isEqualTo(1);
        assertSoftly(softly ->
        {
            softly.assertThat(commands[0].split("&& ").length).isEqualTo(2);
            softly.assertThat(commands[0].split("; ").length).isEqualTo(3);
        });
    }

    @Test
    public void testBuilderWithEchoCommands()
    {
        BashCommandBuilder builder = makeBashCommandBuilderWithEchoedCommands();
        String[] commands = builder.build();

        int expectedCommandStringCount = 1;
        int expectedCommandCount = 6;
        assertThat(commands.length).isEqualTo(expectedCommandStringCount);
        assertSoftly(softly ->
        {
            softly.assertThat(commands[0].split("&& ").length).isEqualTo(expectedCommandCount);
        });
    }

    @Test
    public void testToString()
    {
        String CR = System.getProperty("line.separator");
        BashCommandBuilder builder = makeBashCommandBuilderWithEchoedCommands();
        String toString = builder.toString();
        String[] lines = toString.split(CR);
        assertThat(lines.length).isEqualTo(6);
    }


    private BashCommandBuilder makeBashCommandBuilderWithEchoedCommands()
    {
        // make two ExternalCommand instances
        List<String> testArgs1 = Arrays.asList("mkdir", "/mnt/working/out/newMedia0123");
        ExternalCommand testCommand1 = new TestCommand("tester", testArgs1);
        testCommand1.setPhaseInterval(TestPhase.Pretest);
        testCommand1.setPhaseFraction(0.5);

        List<String> testArgs2 = Arrays.asList("mp4split", "-o", "/mnt/working/out/destination/manifest.m3u8", "/mnt/working/in/media_video_1.mp4");
        ExternalCommand testCommand2 = new TestCommand("tester", testArgs2);
        testCommand2.setPhaseInterval(TestPhase.Test);
        testCommand2.setPhaseFraction(0.5);

        // make command echo helper object with job id (cid).  This will generate an echo command of a scrubbed version of the bash command - to standard out (for logging).
        // It will also generate the actual bash command of interest; and a third command that echos progress to standard out.
        String id = "testId123";
        CommandEchoHelper commandEchoHelper = new CommandEchoHelper(id,true);

        // make command builder, set to add && separators between bash commands
        BashCommandBuilder builder = new BashCommandBuilder();
        builder.setExecUntilFail()
                .addAll(commandEchoHelper.makeEchos(Arrays.asList(testCommand1, testCommand2)));
        return builder;
    }


}