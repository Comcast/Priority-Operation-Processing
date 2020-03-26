package com.theplatform.dfh.cp.modules.monitor.alert;

public enum AlertLevel
{
    INFO,
    CRITICAL,
    WARNING,
    CLEAR,
    UNKNOWN;

    public static AlertLevel getEnum(String value) {
        for(AlertLevel v : values())
            if(v.name().equalsIgnoreCase(value)) return v;

        return UNKNOWN;
    }
}
