package com.comcast.pop.commandbuilder.impl.build.handler.connect.s3;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Java6Assertions.assertThat;


public class S3CommandsTest
{

    @DataProvider(name = "getValidInput")
    public Object[][] getValidInput()
    {
        return new Object[][]
                {
                        {S3Commands.S3_PASS_FILE,  "user", "secret"},
                        {S3Commands.S3_PASS_FILE_PERMISSIONS,  null},
                        {S3Commands.MOUNT_S3, "bucket", "file", "log"},
                        {S3Commands.WAIT_MAX_60S_FOR_FILE, "file"}
                };
    }

    @Test(dataProvider = "getValidInput")
    public void testValidCommands(S3Commands s3Command, String ... args)
    {
        String[] commandArgs = args == null ? new String[0] : args;
        Throwable thrown = catchThrowable(() ->s3Command.makeCommand(commandArgs));
        assertThat(thrown).isNull();
    }

    @Test(dataProvider = "getValidInput")
    public void testValidCommandStrings(S3Commands s3Command, String ... args)
    {
        String[] commandArgs = args == null ? new String[0] : args;
        Throwable thrown = catchThrowable(() ->s3Command.makeCommandString(commandArgs));
        assertThat(thrown).isNull();
    }

    @DataProvider(name = "getInValidInput")
    public Object[][] getInValidInput()
    {
        return new Object[][]
                {
                        {S3Commands.S3_PASS_FILE,  "user", ""},           // blank arg
                        {S3Commands.S3_PASS_FILE_PERMISSIONS,  "extra"},  // extra arg
                        {S3Commands.MOUNT_S3, "bucket", "file", null},    // null arg
                        {S3Commands.WAIT_MAX_60S_FOR_FILE, null}          // missing arg
                };
    }

    @Test(dataProvider = "getInValidInput")
    public void testInValidCommands(S3Commands s3Command, String ... args)
    {
        String[] commandArgs = args == null ? new String[0] : args;
        Throwable thrown = catchThrowable(() ->s3Command.makeCommand(commandArgs));
        assertThat(thrown).isNotNull();
    }

    @Test(dataProvider = "getInValidInput")
    public void testInValidCommandStrings(S3Commands s3Command, String ... args)
    {
        String[] commandArgs = args == null ? new String[0] : args;
        Throwable thrown = catchThrowable(() ->s3Command.makeCommandString(commandArgs));
        assertThat(thrown).isNotNull();
    }

}