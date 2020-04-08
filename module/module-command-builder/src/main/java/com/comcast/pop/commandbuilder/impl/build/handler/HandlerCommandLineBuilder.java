package com.comcast.pop.commandbuilder.impl.build.handler;

import com.comcast.pop.commandbuilder.impl.build.BashCommandBuilder;
import com.comcast.pop.commandbuilder.impl.command.CommandEchoHelper;
import com.comcast.pop.commandbuilder.impl.command.api.CommandStringGenerator;

import java.util.Collections;

import static com.comcast.pop.commandbuilder.impl.build.handler.HandlerCommands.SET_VAR;

public abstract class HandlerCommandLineBuilder implements CommandStringGenerator
{
    public static final String JOB_STATUS_VAR = "job_status_var";
    public static final String JOB_FAIL = "job_status:_failed";
    public static final String JOB_SUCCEED = "job_status:_succeeded";
    private static final String JOB_STATUS_DEFAULT = JOB_FAIL;
    private static final String ASH_SHELL = "ash";
    private static final String ARG_READ_COMMANDS_FROM_STRING = "-c";

    BashCommandBuilder commandBuilder = new BashCommandBuilder();
    CommandEchoHelper echoCommmandHelper = makeEchoHelper(getCid());

    private CommandEchoHelper makeEchoHelper(String cid)
    {
        String dateTemplate = "echo \"$(date +'%%%%F'; nmeter -d0 %%%%3t | head -n1): cid: %s ";
        CommandEchoHelper.setHeaderTemplate(dateTemplate);
        CommandEchoHelper commandEchoHelper = new CommandEchoHelper(cid);
        return commandEchoHelper;
    }

    public abstract String getCid();

    public BashCommandBuilder getCommandBuilder()
    {
        return commandBuilder;
    }

    public CommandEchoHelper getEchoCommmandHelper()
    {
        return echoCommmandHelper;
    }

    public void initializeCommandBuilder()
    {
        commandBuilder
                .add(ASH_SHELL).newBlock()
                .add(ARG_READ_COMMANDS_FROM_STRING).newBlock()
                .setExecUntilFail();
    }

    public void setStatusVar()
    {
        commandBuilder
                .addAll(echoCommmandHelper.makeEchos(Collections.singletonList(SET_VAR.makeCommand(JOB_STATUS_VAR, JOB_STATUS_DEFAULT))));
    }

    public void finalizeCommandBuilder()
    {
        commandBuilder
                .addAll(echoCommmandHelper.makeEchos(Collections.singletonList(SET_VAR.makeCommand(JOB_STATUS_VAR, JOB_SUCCEED))))
                .setExecAll()
                .add("echo $"+ JOB_STATUS_VAR);

    }
}
