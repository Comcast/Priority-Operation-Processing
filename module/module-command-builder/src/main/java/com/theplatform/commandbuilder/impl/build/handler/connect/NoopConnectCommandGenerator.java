package com.theplatform.commandbuilder.impl.build.handler.connect;

import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectData;
import com.theplatform.commandbuilder.impl.build.handler.connect.NoopConnection;
import com.theplatform.commandbuilder.impl.build.handler.connect.s3.ConnectGenerator;
import com.theplatform.commandbuilder.impl.command.api.ExternalCommand;

import java.util.Collections;
import java.util.List;

public class NoopConnectCommandGenerator implements ConnectGenerator
{
    private final String url;

    public NoopConnectCommandGenerator(ConnectData connectData)
    {
        url = connectData.getUrl();
    }

    @Override
    public List<ExternalCommand> generateCommands()
    {
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getUrl()
    {
        return url == null ? NoopConnection.NOOP_URL : url;
    }

    @Override
    public boolean needsPrivilege()
    {
        return false;
    }

    @Override
    public String makeConnectionUrl(ConnectData connectData)
    {
        return connectData.getUrl();
    }
}
