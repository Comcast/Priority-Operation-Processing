package com.comcast.pop.commandbuilder.impl.command;

import com.comcast.pop.commandbuilder.impl.command.api.ExternalCommand;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CommandEchoHelper
{
    /*
    1. Note that not all 'date' utilities support %N notation (for example, does not work on some Macs)
    2. Also note that we the '%' symbols twice for Java string templates to indicate they are literal. However, since we are
    using a template to make another template, we end up needing four '%' symbols to propagate through.
     */
    private static String headerTemplate = "echo $(date + '%%%%F %%%%T.%%%%3N'): cid: %s ";
    public static String commandPrefixValue = "command: ";
    public static String progressPrefixValue = "progress: ";
    private String echoHeader;
    private String commandEchoPrefix;
    private String progressEchoPrefix;
    private Boolean doEchoProgress = false;


    public CommandEchoHelper(String id) {
        echoHeader = makeHeader(id);
        commandEchoPrefix = commandPrefixValue;
        progressEchoPrefix = progressPrefixValue;
    }


    public CommandEchoHelper(String id, Boolean doEchoProgress) {
        this(id);
        this.doEchoProgress = doEchoProgress;
    }

    /**
     * Note that setting the HeaderTemplate affects only instances created afterward.
     */
    public static void setHeaderTemplate(String headerTemplate)
    {
        CommandEchoHelper.headerTemplate = headerTemplate;
    }

    /**
     * Note that setting the commandEchoPrefixValueIn affects only instances created afterward.
     */
    public static void setCommandEchoPrefixValue(String commandEchoPrefixValueIn)
    {
        commandPrefixValue = commandEchoPrefixValueIn;
    }

    /**
     * Note that setting the progressEchoPrefixValueIn affects only instances created afterward.
     */
    public static void setProgressEchoPrefixValue(String progressEchoPrefixValueIn)
    {
        progressPrefixValue = progressEchoPrefixValueIn;
    }



    private String makeHeader(String id) {
        return String.format(headerTemplate, id) +": %s: $\'%s\'";
    }

    public String[] makeEchos(Collection<ExternalCommand> commands)
    {
        List<String> commandStrings = new LinkedList<>();
        commands.forEach(command -> commandStrings.addAll(makeEchoCommands(command)));
        return commandStrings.toArray(new String[0]);
    }

    private Collection<? extends String> makeEchoCommands(ExternalCommand command) {
        List<String> commandStrings = new LinkedList<>();
        commandStrings.add(makeEcho(commandEchoPrefix, escapeQuotes(command.toScrubbedCommandString())));
        commandStrings.add(command.toCommandString());
        if(doEchoProgress)
        {
            commandStrings.add(makeEcho(progressEchoPrefix, command.getProgress()));
        }
        return commandStrings;
    }

    protected String escapeQuotes(String commandString)
    {
        commandString = commandString.replaceAll("\"", "\\\\\"");
        commandString = commandString.replaceAll("\'", "\\\\\'");
        return commandString;
    }

    private String makeEcho(String prefix, String command) {
        return String.format(echoHeader,prefix,command);
    }
}
