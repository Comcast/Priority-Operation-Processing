package com.comcast.pop.modules.queue.aws.sqs.api;

/**
 * The type of request to perform on the queue
 */
public enum QueueRequestType
{
    add,
    poll,
    //peek,
    size,
}
