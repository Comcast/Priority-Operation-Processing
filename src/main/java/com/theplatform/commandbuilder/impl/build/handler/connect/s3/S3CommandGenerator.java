package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectData;
import com.theplatform.commandbuilder.impl.command.api.ExternalCommand;

import java.util.LinkedList;
import java.util.List;

public class S3CommandGenerator implements ConnectGenerator
{
    public static final String S3_DEFAULT_MOUNT = "/var/s3";
    private static final String QUOTE = "\"";
    private final S3Data s3Data;
    private final S3Connect s3VhsConnect;

    public S3CommandGenerator(S3Data s3Data, S3Connect s3VhsConnect)
    {
        this.s3Data = s3Data;
        this.s3VhsConnect = s3VhsConnect;
    }

    public List<ExternalCommand> generateCommands()
    {
        List<ExternalCommand> commands = new LinkedList<ExternalCommand>();
        commands.add(S3Commands.S3_PASS_FILE.makeCommand(s3Data.getKey_id(), s3Data.getSecret_access_key()));
        commands.add(S3Commands.S3_PASS_FILE_PERMISSIONS.makeCommand());
        commands.add(S3Commands.MOUNT_S3.makeCommand(s3Data.getBucket(), s3Data.getMount(), S3Commands.S3_DEFAULT_CONNECT_LOG));
        commands.add(S3Commands.WAIT_MAX_60S_FOR_FILE.makeCommand(s3Data.getUrl()));
        return  commands;
    };

    @Override
    public String getUrl()
    {
        return removeQuotes(s3Data.getUrl());
    }

    @Override
    public boolean needsPrivilege()
    {
        return true;
    }

    @Override
    public String makeConnectionUrl(ConnectData connectData)
    {
        return removeQuotes(s3VhsConnect.makeS3Data(connectData).getUrl());
    }

    public static String removeQuotes(String url)
    {
        String urlOut = url;
        if(urlOut != null && urlOut.length() > 0 && urlOut.startsWith(QUOTE))
        {
            int length = urlOut.length()-1;
            return urlOut.subSequence(1,length).toString();
        }
        return urlOut;
    }
}
