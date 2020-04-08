package com.comcast.pop.commandbuilder.impl.command;

import com.comcast.pop.commandbuilder.impl.command.api.ExternalCommand;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class CommandEchoHelperTest {

    @Test
    public void testHelperHappy()
    {
        String id = "testId123";
        boolean makeProgressCommand = true;
        CommandEchoHelper commandEchoHelper = new CommandEchoHelper(id, makeProgressCommand);

        List<String> testArgs1 = Arrays.asList("mkdir", "/mnt/working/out/newMedia0123");
        ExternalCommand testCommand1 = new TestCommand("tester", testArgs1);
        testCommand1.setPhaseFraction(0);

        List<String> testArgs2 = Arrays.asList("mp4split", "-o", "/mnt/working/out/destination/manifest.m3u8", "/mnt/working/in/media_video_1.mp4");
        ExternalCommand testCommand2 = new TestCommand("tester", testArgs2);
        testCommand2.setPhaseFraction(1);

        String[] commands = commandEchoHelper.makeEchos(Arrays.asList(testCommand1, testCommand2));
        EchoCommandTestValidator validator = new EchoCommandTestValidator(commands);
        int expectedCommandCount = 6;
        assertThat(commands.length).isEqualTo(expectedCommandCount);
        assertSoftly(softly ->
        {
            validator.setSoftly(softly);
            validator.assertEchoDate(0,2,3,5);
            validator.assertCid(id,0,2,3,5);
            validator.assertScrubbedCommand(0,3);
            validator.assertProgress(2,5);
        });
    }

    @Test
    public void testNoProgressCommands()
    {
        String id = "testId123";
        boolean makeProgressCommand = false; // is also false by default
        CommandEchoHelper commandEchoHelper = new CommandEchoHelper(id, makeProgressCommand);

        List<String> testArgs1 = Arrays.asList("mkdir", "/mnt/working/out/newMedia0123");
        ExternalCommand testCommand1 = new TestCommand("tester", testArgs1);
        testCommand1.setPhaseFraction(0);

        List<String> testArgs2 = Arrays.asList("mp4split", "-o", "/mnt/working/out/destination/manifest.m3u8", "/mnt/working/in/media_video_1.mp4");
        ExternalCommand testCommand2 = new TestCommand("tester", testArgs2);
        testCommand2.setPhaseFraction(1);

        String[] commands = commandEchoHelper.makeEchos(Arrays.asList(testCommand1, testCommand2));
        EchoCommandTestValidator validator = new EchoCommandTestValidator(commands);
        int expectedCommandCount = 4;
        assertThat(commands.length).isEqualTo(expectedCommandCount);
        assertSoftly(softly ->
        {
            validator.setSoftly(softly);
            validator.assertEchoDate(0,2);
            validator.assertCid(id,0,2);
            validator.assertScrubbedCommand(0,2);
        });
    }

    @Test
    public void testProgress()
    {
        String id = "testId123";
        boolean makeProgressCommand = true;
        CommandEchoHelper commandEchoHelper = new CommandEchoHelper(id, makeProgressCommand);

        List<String> testArgs1 = Arrays.asList("mkdir", "/mnt/working/out/newMedia0123");
        ExternalCommand testCommand1 = new TestCommand("tester", testArgs1);
        testCommand1.setPhaseInterval(TestPhase.Pretest);
        testCommand1.setPhaseFraction(0.5);

        List<String> testArgs2 = Arrays.asList("mp4split", "-o", "/mnt/working/out/destination/manifest.m3u8", "/mnt/working/in/media_video_1.mp4");
        ExternalCommand testCommand2 = new TestCommand("tester", testArgs2);
        testCommand2.setPhaseInterval(TestPhase.Test);
        testCommand2.setPhaseFraction(0.5);

        String[] commands = commandEchoHelper.makeEchos(Arrays.asList(testCommand1, testCommand2));
        EchoCommandTestValidator validator = new EchoCommandTestValidator(commands);
        assertSoftly(softly ->
        {
            validator.setSoftly(softly);
            double expectedProgress1 = 15.0; // phase progress interval is 0 - 30%.  Phase fraction is 0.5.  So, progress is 0 + 0.5(30 - 0) = 15%.
            validator.assertProgressPercent(expectedProgress1,2);

            double expectedProgress2 = 50.0; // phase progress interval is 30 - 70%.  Phase fraction is 0.5.  So, progress is 30 + 0.5(70 - 30) = 50%.
            validator.assertProgressPercent(expectedProgress2,5);
        });
    }


    @Test
    public void testEscapeQuotes()
    {
        String testText = "\"a\'b\'\"c";
        String expectedText = "\\\"a\\\'b\\\'\\\"c";
        String id = "testId123";
        boolean makeProgressCommand = false;
        CommandEchoHelper commandEchoHelper = new CommandEchoHelper(id, makeProgressCommand);
        String result = commandEchoHelper.escapeQuotes(testText);
        assertThat(result).isEqualTo(expectedText);
    }


    @Test
    public void testNestedQuotes()
    {
        String id = "testId123";
        boolean makeProgressCommand = false;
        CommandEchoHelper commandEchoHelper = new CommandEchoHelper(id, makeProgressCommand);

        List<String> testArgs1 = Arrays.asList("mkdir", "\'/mnt/working/out(\"hah\")/newMedia0123\'");
        ExternalCommand testCommand1 = new TestCommand("tester", testArgs1);

        String[] commands = commandEchoHelper.makeEchos(Arrays.asList(testCommand1));
        int expectedCommandCount = 2;
        assertThat(commands.length).isEqualTo(expectedCommandCount);
        assertSoftly(softly ->
        {
            softly.assertThat(commands[0]).contains("\"");
            softly.assertThat(commands[0]).contains("\'");
        });
    }
}