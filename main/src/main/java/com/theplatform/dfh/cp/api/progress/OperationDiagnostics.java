package com.theplatform.dfh.cp.api.progress;

import com.theplatform.dfh.cp.api.IdentifiedObject;

import java.util.Date;

/**
 */
public class OperationDiagnostics implements IdentifiedObject
{

    private String id;
    private String lastErrorCode;
    private String lastErrorMessage;
    private Date lastErrorTime;
    private Integer attempt;
    private Date attemptTime;


    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void setId(String id)
    {
        this.id = id;
    }

    public String getLastErrorCode()
    {
        return lastErrorCode;
    }

    public void setLastErrorCode(String lastErrorCode)
    {
        this.lastErrorCode = lastErrorCode;
    }

    public String getLastErrorMessage()
    {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage)
    {
        this.lastErrorMessage = lastErrorMessage;
    }

    public Date getLastErrorTime()
    {
        return lastErrorTime;
    }

    public void setLastErrorTime(Date lastErrorTime)
    {
        this.lastErrorTime = lastErrorTime;
    }

    public Integer getAttempt()
    {
        return attempt;
    }

    public void setAttempt(Integer attempt)
    {
        this.attempt = attempt;
    }

    public Date getAttemptTime()
    {
        return attemptTime;
    }

    public void setAttemptTime(Date attemptTime)
    {
        this.attemptTime = attemptTime;
    }
}
