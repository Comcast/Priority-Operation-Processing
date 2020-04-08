package com.comcast.pop.commandbuilder.impl.build.handler.connect.s3;

import com.comcast.pop.commandbuilder.impl.build.handler.HandlerCommandFactory;
import com.comcast.pop.commandbuilder.impl.command.api.ExternalCommand;
import com.comcast.pop.commandbuilder.impl.command.api.Phase.PhaseInterval;
import com.comcast.pop.commandbuilder.impl.validate.exception.CommandValidationException;
import com.comcast.pop.commandbuilder.impl.validate.rules.RuleFactoryUtil;
import com.comcast.pop.commandbuilder.impl.build.handler.exception.HandlerCommandException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum S3Commands implements HandlerCommandFactory
{
    S3_PASS_FILE("echo '%s:%s'  > /root/.passwd-s3fs",2)
            {
                protected String makeObfuscatedCommandString(String... args)
                {
                    return makeCommandString(OBFUSCATED_S3_KEY_ID, OBFUSCATED_S3_KEY_VALUE);
                }
            },
    S3_PASS_FILE_PERMISSIONS("chmod 600 /root/.passwd-s3fs",0),
    MOUNT_S3("( s3fs %s %s -f -d -o passwd_file=/root/.passwd-s3fs,allow_other,uid=1000,gid=1000,max_stat_cache_size=1000,stat_cache_expire=900,retries=5,connect_timeout=10,use_path_request_style &> %s & )",3),
    // Assumes that file paths are quoted.
    WAIT_MAX_60S_FOR_FILE("eval 'wait_seconds=60 ; until test $((wait_seconds--)) -eq 0 -o -f %s; do sleep 1; done'",1);

    private final Logger logger = LoggerFactory.getLogger(S3Commands.class);
    private static final String ERROR_TEMPLATE = "For command %s, expected %2d arg(s); saw %2d arg(s) given: %s.";
    private final String commandTemplate;
    private final int argCount;
    public static final String S3_DEFAULT_CONNECT_LOG = "/var/s3_connect.log";
    private static final String OBFUSCATED_S3_KEY_ID = "s3_key_id";
    private static final String OBFUSCATED_S3_KEY_VALUE = "s3_private_key";

    S3Commands(String commandTemplate, int argCount)
    {
        this.commandTemplate=commandTemplate;
        this.argCount=argCount;
    }

    protected String makeCommandString(String... args)
    {
        if(!validArgs(args))
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
        if(!validArgs(args))
        {
            throw new HandlerCommandException(String.format(ERROR_TEMPLATE, this.name(), argCount, args.length, Arrays.toString(args)));
        }
        return new ConnectCommandBasic(makeCommandString(args), makeObfuscatedCommandString(args));
    }

    private boolean validArgs(String[] args)
    {
        if(args.length != argCount)
        {
            return false;
        }
        for(int i = 0; i < argCount; i++)
        {
            if(args[i] == null || args[i].length() == 0)
            {
                return false;
            }
        }

        return true;
    }
}
final class ConnectCommandBasic implements ExternalCommand
{
    private final String commandString;
    private final String scrubbedCommand;


    public ConnectCommandBasic(String commandString, String scrubbedCommand)
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
