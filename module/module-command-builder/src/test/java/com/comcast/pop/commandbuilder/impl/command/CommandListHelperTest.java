package com.comcast.pop.commandbuilder.impl.command;

import com.comcast.pop.commandbuilder.impl.command.api.ExternalCommand;
import com.comcast.pop.commandbuilder.impl.command.api.Phase.PhaseInterval;
import com.comcast.pop.commandbuilder.impl.command.api.ProgressFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;


public class CommandListHelperTest
{
    @Test
    public void testCommandlistAddCommand()
    {
        CommandListHelper<ExternalCommand> commandListHelper = new TestCommandListHelper();
        commandListHelper.add(new TestCommand("test", Collections.emptyList()), TestPhase.Pretest);
        commandListHelper.add(new TestCommand("test", Collections.emptyList()), TestPhase.Test);
        commandListHelper.add(new TestCommand("test", Collections.emptyList()), TestPhase.Test);
        commandListHelper.add(new TestCommand("test", Collections.emptyList()), TestPhase.Test);
        commandListHelper.add(new TestCommand("test", Collections.emptyList()), TestPhase.Test);
        commandListHelper.add(new TestCommand("test", Collections.emptyList()), TestPhase.Clean);
        commandListHelper.add(new TestCommand("test", Collections.emptyList()), TestPhase.Clean);

        ExternalCommand[] commands = commandListHelper.get().toArray(new ExternalCommand[0]);

        assertThat(commands).hasSize(7);
        assertSoftly(softly ->
        {
            softly.assertThat(commands[0].getProgress()).isEqualTo("- || Phase : Pretest || Percent done : 30.0 || ");
            softly.assertThat(commands[1].getProgress()).isEqualTo("- || Phase : Test || Percent done : 40.0 || ");
            softly.assertThat(commands[2].getProgress()).isEqualTo("- || Phase : Test || Percent done : 50.0 || ");
            softly.assertThat(commands[3].getProgress()).isEqualTo("- || Phase : Test || Percent done : 60.0 || ");
            softly.assertThat(commands[4].getProgress()).isEqualTo("- || Phase : Test || Percent done : 70.0 || ");
            softly.assertThat(commands[5].getProgress()).isEqualTo("- || Phase : Clean || Percent done : 85.0 || ");
            softly.assertThat(commands[6].getProgress()).isEqualTo("- || Phase : Clean || Percent done : 100.0 || ");
        });
    }

    @Test
    public void testCommandlistAddFactory()
    {
        CommandListHelper<ExternalCommand> commandListHelper = new TestCommandListHelper();
        ProgressFactory<ExternalCommand> progressFactory = makeProgressFactory();
        commandListHelper.add(progressFactory);
        ExternalCommand[] commands = commandListHelper.get().toArray(new ExternalCommand[0]);

        assertThat(commands).hasSize(7);
        assertSoftly(softly ->
        {
            softly.assertThat(commands[0].getProgress()).isEqualTo("- || Phase : Pretest || Percent done : 30.0 || ");
            softly.assertThat(commands[1].getProgress()).isEqualTo("- || Phase : Test || Percent done : 40.0 || ");
            softly.assertThat(commands[2].getProgress()).isEqualTo("- || Phase : Test || Percent done : 50.0 || ");
            softly.assertThat(commands[3].getProgress()).isEqualTo("- || Phase : Test || Percent done : 60.0 || ");
            softly.assertThat(commands[4].getProgress()).isEqualTo("- || Phase : Test || Percent done : 70.0 || ");
            softly.assertThat(commands[5].getProgress()).isEqualTo("- || Phase : Clean || Percent done : 85.0 || ");
            softly.assertThat(commands[6].getProgress()).isEqualTo("- || Phase : Clean || Percent done : 100.0 || ");
        });
    }

    @Test
    public void testClear()
    {
        CommandListHelper<ExternalCommand>  commandListHelper = new TestCommandListHelper();
        ProgressFactory<ExternalCommand> progressFactory = makeProgressFactory();
        commandListHelper.add(progressFactory);
        List<ExternalCommand> commands = commandListHelper.get();
        commandListHelper.clear();
        List<ExternalCommand> noCommands = commandListHelper.get();

        assertSoftly(softly ->
        {
            softly.assertThat(commands).hasSize(7);
            softly.assertThat(noCommands).hasSize(0);
        });
    }

    private ProgressFactory<ExternalCommand> makeProgressFactory()
    {
        return new ProgressFactory<ExternalCommand>()
        {
            private List<PhaseInterval> progressData = Arrays.asList(TestPhase.Pretest,
                    TestPhase.Test, TestPhase.Test, TestPhase.Test, TestPhase.Test, TestPhase.Clean, TestPhase.Clean);
            private List<ExternalCommand> commands = Arrays.asList(
                    new TestCommand("test", Collections.emptyList()),
                    new TestCommand("test", Collections.emptyList()),
                    new TestCommand("test", Collections.emptyList()),
                    new TestCommand("test", Collections.emptyList()),
                    new TestCommand("test", Collections.emptyList()),
                    new TestCommand("test", Collections.emptyList()),
                    new TestCommand("test", Collections.emptyList()));

            @Override
            public List<PhaseInterval> getProgressData()
            {
                return progressData;
            }

            @Override
            public void makeCommands()
            {
            }

            @Override
            public List<ExternalCommand> getCommands()
            {
                return commands;
            }
        };
    }
}