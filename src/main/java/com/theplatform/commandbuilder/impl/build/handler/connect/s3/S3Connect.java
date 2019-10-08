package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectData;

public interface S3Connect
{
     boolean isParsable(ConnectData connectData);
     S3Data makeS3Data(ConnectData connectData);
}
