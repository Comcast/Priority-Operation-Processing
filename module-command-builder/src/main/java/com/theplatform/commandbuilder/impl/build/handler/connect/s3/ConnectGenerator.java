package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import com.theplatform.commandbuilder.impl.command.api.CommandGenerator;
import com.theplatform.commandbuilder.impl.build.handler.connect.Connect;
import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectData;

public interface ConnectGenerator extends CommandGenerator, Connect
{
    String makeConnectionUrl(ConnectData connectData);
}
