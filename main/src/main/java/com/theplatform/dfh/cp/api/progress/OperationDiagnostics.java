package com.theplatform.dfh.cp.api.progress;

import com.theplatform.dfh.cp.api.IdentifiedObject;

import java.util.Date;

/**
 */
public class OperationDiagnostics implements IdentifiedObject
{

    private String id;
    private String errorCode;
    private String errorMessage;
    private Date errorTime;
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

    public String getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(String errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public Date getErrorTime()
    {
        return errorTime;
    }

    public void setErrorTime(Date errorTime)
    {
        this.errorTime = errorTime;
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
