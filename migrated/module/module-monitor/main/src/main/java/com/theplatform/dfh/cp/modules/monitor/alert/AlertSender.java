package com.theplatform.dfh.cp.modules.monitor.alert;

public interface AlertSender
{
    public void send(AlertMessage message) throws AlertException;
}
