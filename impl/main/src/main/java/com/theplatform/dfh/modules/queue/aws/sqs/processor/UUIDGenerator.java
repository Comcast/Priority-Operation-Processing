package com.theplatform.dfh.modules.queue.aws.sqs.processor;

import java.util.UUID;

public class UUIDGenerator
{
    public String generate()
    {
        return UUID.randomUUID().toString();
    }
}
