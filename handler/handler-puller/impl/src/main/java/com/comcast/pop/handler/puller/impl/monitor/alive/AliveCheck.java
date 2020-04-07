package com.comcast.pop.handler.puller.impl.monitor.alive;

public interface AliveCheck
{
    boolean isAlive();
    String getNotAliveString();
}
