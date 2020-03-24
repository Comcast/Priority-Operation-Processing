package com.theplatform.commandbuilder.impl.command;

import com.theplatform.commandbuilder.impl.build.handler.HandlerCommands;
import com.theplatform.commandbuilder.impl.build.handler.connect.s3.S3Commands;
import com.theplatform.commandbuilder.impl.command.api.ExternalCommand;
import com.theplatform.commandbuilder.impl.validate.exception.CommandValidationException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.Java6Assertions.catchThrowable;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class ExternalCommandSecurityTest
{
    @DataProvider
    public Object[][] getForbidden()
    {
        return new Object[][]
                {
                        {"* some stuff"},
                        {"; some stuff"},
                        {"& some stuff"},
                        {"| some stuff"},
                        {"[ some stuff"},
                        {"] some stuff"},
                        {"< some stuff"},
                        {"> some stuff"},
                        {"% some stuff"},
                        {"? some stuff"},
                        {"! some stuff"},
                        {"$ some stuff"},
                        {"@ some stuff"},
                        {"{ some stuff"},
                        {"} some stuff"},
                        {"# some stuff"},
                        {"` some stuff"},
                        {"~ some stuff"},
                        {", some stuff"},
                };
    }

    @Test(dataProvider = "getForbidden")
    public void testForbiddenChars(String forbidden)
    {
        ExternalCommand testCommand = new TestCommand("testCommand", Collections.singletonList(forbidden));
        Throwable thrown =  catchThrowable( () -> testCommand.toCommandString());
        Throwable thrown2 =  catchThrowable( () -> HandlerCommands.SET_VAR.makeCommand("envArg", forbidden));
        Throwable thrown3 =  catchThrowable( () -> S3Commands.S3_PASS_FILE.makeCommand("s3Key", forbidden));

        assertSoftly(softly ->
        {
            softly.assertThat(thrown).isNotNull();
            softly.assertThat(thrown2).isNotNull();
            softly.assertThat(thrown3).isNotNull();
        });

        assertSoftly(softly ->
        {
            softly.assertThat(thrown).isInstanceOf(CommandValidationException.class);
            softly.assertThat(thrown2).isInstanceOf(CommandValidationException.class);
            softly.assertThat(thrown3).isInstanceOf(CommandValidationException.class);
        });
    }
}