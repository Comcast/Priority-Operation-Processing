package com.comcast.fission.handler.puller.impl.monitor.alive;

public interface AliveCheck
{
    boolean isAlive();
    String getNotAliveString();
}
