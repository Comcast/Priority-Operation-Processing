package com.theplatform.dfh.cp.reaper.objects.aws.config;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class DataObjectReaperConfig
{
    // lambda settings
    private int maximumExecutionSeconds = 60000;

    // general settings
    private String tableName;
    private String idFieldName;
    private long reapAgeMinutes;

    // delete settings
    private long deleteCallDelayMillis = 0;

    // scan settings
    private long scanDelayMillis = 0;
    private int batchSize = 50;
    private int objectScanLimit = 50;

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

    public long getReapAgeMinutes()
    {
        return reapAgeMinutes;
    }

    public DataObjectReaperConfig setReapAgeMinutes(long reapAgeMinutes)
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

    public int getBatchSize()
    {
        return batchSize;
    }

    public DataObjectReaperConfig setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
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

        if(batchSize < 1)
            validationIssues.add("batchSize must be 1 or more");

        if(objectScanLimit < 1)
            validationIssues.add("objectScanLimit must be 1 or more");

        if(maximumExecutionSeconds < 0)
            validationIssues.add("maximumExecutionSeconds must be 0 non-negative");

        return validationIssues.size() == 0
            ? null
            : String.join(",", validationIssues);

    }
}
