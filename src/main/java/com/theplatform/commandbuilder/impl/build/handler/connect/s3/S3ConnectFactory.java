package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectData;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

/**
 * Extensible s3 parser list (e.g. S3VhsConnect, test parsers, etc)
 * Hm.  Revisiting the idea that we will need more than one s3 connect parser.
 *
 * Note assumption that, at most, one parser will be filtered.
 */
public class S3ConnectFactory
{
    private final List<S3Connect> s3Connects;

    public S3ConnectFactory(S3Connect... s3Connects)
    {
        this.s3Connects = asList(s3Connects);
    }

    public  Optional<S3Connect> getS3Connect(ConnectData connectData)
    {
        Optional<S3Connect> s3Connect = s3Connects.stream().filter(s3ConnectInstance -> s3ConnectInstance.isParsable(connectData)).findFirst();
        return s3Connect;
    }

}
