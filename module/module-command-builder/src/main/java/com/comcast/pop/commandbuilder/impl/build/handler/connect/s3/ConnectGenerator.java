package com.comcast.pop.commandbuilder.impl.build.handler.connect.s3;

import com.comcast.pop.commandbuilder.impl.build.handler.connect.Connect;
import com.comcast.pop.commandbuilder.impl.command.api.CommandGenerator;
import com.comcast.pop.commandbuilder.impl.build.handler.connect.ConnectData;

public interface ConnectGenerator extends CommandGenerator, Connect
{
    String makeConnectionUrl(ConnectData connectData);
}
