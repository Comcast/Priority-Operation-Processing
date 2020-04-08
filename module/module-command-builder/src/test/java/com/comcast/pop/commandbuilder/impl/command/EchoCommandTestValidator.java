package com.comcast.pop.commandbuilder.impl.command;

import org.assertj.core.api.SoftAssertions;

@SuppressWarnings("ResultOfMethodCallIgnored")
class EchoCommandTestValidator {
    private final String[] commands;
    private SoftAssertions softly;

    EchoCommandTestValidator(String[] commands) {
        this.commands = commands;
    }

    void setSoftly(SoftAssertions softly) {
        this.softly = softly;
    }

    void assertEchoDate(int... indices)
    {
        for(int index: indices)
        {
            softly.assertThat(commands[index]).startsWith("echo $(date + '%F %T.%3N'): ");
        }
    }

    void assertCid(String id, int... indices)
    {
        for(int index: indices)
        {
            softly.assertThat(commands[index]).contains(String.format("cid: %s",id));
        }
    }

    void assertScrubbedCommand(int... indices)
    {
        for(int index: indices)
        {
            softly.assertThat(commands[index]).contains(" command: ");
            softly.assertThat(commands[index]).contains("scrubberArg");
        }
    }

    void assertCommandNotScrubbed(int... indices)
    {
        for(int index: indices)
        {
            softly.assertThat(commands[index]).doesNotContain("scrubberArg");
        }
    }

    void assertProgress(int... indices)
    {
        for(int index: indices)
        {
            softly.assertThat(commands[index]).contains(" progress: ");
        }
    }

    void assertProgressPercent(Double expectedProgress, int index) {
        softly.assertThat(commands[index]).contains(expectedProgress.toString());
    }
}
