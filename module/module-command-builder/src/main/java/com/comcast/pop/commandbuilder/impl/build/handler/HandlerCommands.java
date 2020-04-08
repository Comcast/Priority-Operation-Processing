package com.comcast.pop.commandbuilder.impl.build.handler;

import com.comcast.pop.commandbuilder.impl.validate.exception.CommandValidationException;
import com.comcast.pop.commandbuilder.impl.validate.rules.RuleFactoryUtil;
import com.comcast.pop.commandbuilder.impl.build.handler.exception.HandlerCommandException;
import com.comcast.pop.commandbuilder.impl.command.api.ExternalCommand;
import com.comcast.pop.commandbuilder.impl.command.api.Phase.PhaseInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum HandlerCommands implements HandlerCommandFactory
{
    DEBUG("echo \"'%s'\" >> /root/commands.log",1),
    SET_VAR("%s='%s'",2),
    // expect that quotes are added to path if needed.
    VALIDATE_EXISTENCE("[ -f %1$s ] || { echo '%1$s - FILE, NOT FOUND' ; exit 1 ;}",1), // secure existence of file.
    LINGER_AN_HOUR("sleep 36000",0);  // pause for an hour - e.g. to allow one to ssh into pod, etc.
    ;

    private final Logger logger = LoggerFactory.getLogger(HandlerCommands.class);

    private static final String ERROR_TEMPLATE = "For command %s, expected %2d arg(s); saw %2d arg(s) given: %s.";
    private final String commandTemplate;
    private final int argCount;

    HandlerCommands(String commandTemplate, int argCount)
    {
        this.commandTemplate=commandTemplate;
        this.argCount=argCount;
    }

    protected String makeCommandString(String... args)
    {
        if(args.length != argCount)
        {
            throw new HandlerCommandException(String.format(ERROR_TEMPLATE, this.name(), argCount, args.length, Arrays.toString(args)));
        }
        return String.format(commandTemplate, secure(args));
    }

    private String[] secure(String[] args)
    {
        if(!RuleFactoryUtil.FORBIDDEN_CHARACTERS.test(args))
        {
            String msg = String.format("Argument for %s has forbidden character",name());
            logger.error(msg);
            throw new CommandValidationException(msg);
        };
        return args;
    }

    protected String makeObfuscatedCommandString(String... args)
    {
        return makeCommandString(args);
    }

    @Override
    public ExternalCommand makeCommand(String... args)
    {
        if(args.length != argCount)
        {
            throw new HandlerCommandException(String.format(ERROR_TEMPLATE, this.name(), argCount, args.length, Arrays.toString(args)));
        }
        return new HandlerCommandBasic(makeCommandString(args), makeObfuscatedCommandString(args));
    }
}
final class HandlerCommandBasic implements ExternalCommand
 {
     private final String commandString;
     private final String scrubbedCommand;


     public HandlerCommandBasic(String commandString, String scrubbedCommand)
     {
         this.scrubbedCommand = scrubbedCommand;
         this.commandString = commandString;
     }

     @Override
     public String getProgramName()
     {
         return "base command type";
     }

     @Override
     public String toCommandString()
     {
         return commandString;
     }

     @Override
     public String toScrubbedCommandString()
     {
         return scrubbedCommand;
     }

     @Override
     public List<String> getProgramArgumentList()
     {
         return Collections.EMPTY_LIST;
     }

     @Override
     public String getProgress()
     {
         return "progress not calculated";
     }

     @Override
     public void setPhaseFraction(double v)
     {
     }

     @Override
     public void setPhaseInterval(PhaseInterval phaseInterval)
     {
     }
}
