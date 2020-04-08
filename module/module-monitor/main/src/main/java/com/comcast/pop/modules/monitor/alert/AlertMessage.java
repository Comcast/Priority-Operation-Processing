package com.comcast.pop.modules.monitor.alert;

public interface AlertMessage
{
    public AlertLevel getLevel();
    public void setLevel(AlertLevel level);
}
