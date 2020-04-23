package com.comcast.pop.modules.monitor.alert;

public interface AlertSender
{
    public void send(AlertMessage message) throws AlertException;
}
