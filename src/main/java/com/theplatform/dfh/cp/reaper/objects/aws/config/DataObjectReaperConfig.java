package com.theplatform.dfh.cp.reaper.objects.aws.config;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration object for the reaper functionality
 */
public class DataObjectReaperConfig
{
    // lambda settings
    private int maximumExecutionSeconds = 60;

    // general settings
    private String tableName;
    private String idFieldName;

    // scan settings
    private int reapAgeMinutes;
    private long scanDelayMillis = 0;
    private int targetBatchSize = 50;
    private int objectScanLimit = 50;

    // delete settings
    private long deleteCallDelayMillis = 0;
    private boolean logDeleteOnly = false;

    // reaper scan settings
    private String timeFieldName;


    public DataObjectReaperConfig()
    {
    }

    public String getTableName()
    {
        return tableName;
    }

    public int getMaximumExecutionSeconds()
    {
        return maximumExecutionSeconds;
    }

    public DataObjectReaperConfig setMaximumExecutionSeconds(int maximumExecutionSeconds)
    {
        this.maximumExecutionSeconds = maximumExecutionSeconds;
        return this;
    }

    public DataObjectReaperConfig setTableName(String tableName)
    {
        this.tableName = tableName;
        return this;
    }

    public String getIdFieldName()
    {
        return idFieldName;
    }

    public DataObjectReaperConfig setIdFieldName(String idFieldName)
    {
        this.idFieldName = idFieldName;
        return this;
    }

    public int getReapAgeMinutes()
    {
        return reapAgeMinutes;
    }

    public DataObjectReaperConfig setReapAgeMinutes(int reapAgeMinutes)
    {
        this.reapAgeMinutes = reapAgeMinutes;
        return this;
    }

    public long getDeleteCallDelayMillis()
    {
        return deleteCallDelayMillis;
    }

    public DataObjectReaperConfig setDeleteCallDelayMillis(long deleteCallDelayMillis)
    {
        this.deleteCallDelayMillis = deleteCallDelayMillis;
        return this;
    }

    public long getScanDelayMillis()
    {
        return scanDelayMillis;
    }

    public DataObjectReaperConfig setScanDelayMillis(long scanDelayMillis)
    {
        this.scanDelayMillis = scanDelayMillis;
        return this;
    }

    public int getTargetBatchSize()
    {
        return targetBatchSize;
    }

    public DataObjectReaperConfig setTargetBatchSize(int targetBatchSize)
    {
        this.targetBatchSize = targetBatchSize;
        return this;
    }

    public int getObjectScanLimit()
    {
        return objectScanLimit;
    }

    public DataObjectReaperConfig setObjectScanLimit(int objectScanLimit)
    {
        this.objectScanLimit = objectScanLimit;
        return this;
    }

    public String getTimeFieldName()
    {
        return timeFieldName;
    }

    public DataObjectReaperConfig setTimeFieldName(String timeFieldName)
    {
        this.timeFieldName = timeFieldName;
        return this;
    }

    public boolean isLogDeleteOnly()
    {
        return logDeleteOnly;
    }

    public DataObjectReaperConfig setLogDeleteOnly(boolean logDeleteOnly)
    {
        this.logDeleteOnly = logDeleteOnly;
        return this;
    }

    public String validate()
    {
        List<String> validationIssues = new LinkedList<>();

        if(StringUtils.isBlank(tableName))
            validationIssues.add("tableName must be specified");

        if(StringUtils.isBlank(idFieldName))
            validationIssues.add("idFieldName must be specified");

        if(StringUtils.isBlank(timeFieldName))
            validationIssues.add("timeFieldName must be specified");

        if(reapAgeMinutes < 0)
            validationIssues.add("reapAgeMinutes must be non-negative");

        if(deleteCallDelayMillis < 0)
            validationIssues.add("deleteCallDelayMillis must be non-negative");

        if(scanDelayMillis < 0)
            validationIssues.add("scanDelayMillis must be non-negative");

        if(targetBatchSize < 1)
            validationIssues.add("targetBatchSize must be 1 or more");

        if(objectScanLimit < 1)
            validationIssues.add("objectScanLimit must be 1 or more");

        if(maximumExecutionSeconds < 0)
            validationIssues.add("maximumExecutionSeconds must be non-negative");

        return validationIssues.size() == 0
            ? null
            : String.join(",", validationIssues);

    }
}
