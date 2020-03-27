package com.comcast.fission.agenda.reclaim.aws.config;

import com.comcast.fission.agenda.reclaim.config.ReclaimerConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class AWSReclaimerConfig extends ReclaimerConfig
{
    // AgendaProgress scan specific
    private String tableName;
    private String idFieldName;
    private String timeFieldName;
    private int reclaimAgeMinutes = 0;
    private long scanDelayMillis = 0;
    private int targetBatchSize = 50;
    private int objectScanLimit = 50;

    public String getTableName()
    {
        return tableName;
    }

    public AWSReclaimerConfig setTableName(String tableName)
    {
        this.tableName = tableName;
        return this;
    }

    public String getIdFieldName()
    {
        return idFieldName;
    }

    public AWSReclaimerConfig setIdFieldName(String idFieldName)
    {
        this.idFieldName = idFieldName;
        return this;
    }

    public String getTimeFieldName()
    {
        return timeFieldName;
    }

    public AWSReclaimerConfig setTimeFieldName(String timeFieldName)
    {
        this.timeFieldName = timeFieldName;
        return this;
    }

    public int getReclaimAgeMinutes()
    {
        return reclaimAgeMinutes;
    }

    public AWSReclaimerConfig setReclaimAgeMinutes(int reclaimAgeMinutes)
    {
        this.reclaimAgeMinutes = reclaimAgeMinutes;
        return this;
    }

    public long getScanDelayMillis()
    {
        return scanDelayMillis;
    }

    public AWSReclaimerConfig setScanDelayMillis(long scanDelayMillis)
    {
        this.scanDelayMillis = scanDelayMillis;
        return this;
    }

    public int getTargetBatchSize()
    {
        return targetBatchSize;
    }

    public AWSReclaimerConfig setTargetBatchSize(int targetBatchSize)
    {
        this.targetBatchSize = targetBatchSize;
        return this;
    }

    public int getObjectScanLimit()
    {
        return objectScanLimit;
    }

    public AWSReclaimerConfig setObjectScanLimit(int objectScanLimit)
    {
        this.objectScanLimit = objectScanLimit;
        return this;
    }

    @Override
    public String validate()
    {
        String superValidationResult = super.validate();

        List<String> validationIssues = new LinkedList<>();

        if(StringUtils.isBlank(tableName))
            validationIssues.add("tableName must be specified");

        if(StringUtils.isBlank(idFieldName))
            validationIssues.add("idFieldName must be specified");

        if(StringUtils.isBlank(timeFieldName))
            validationIssues.add("timeFieldName must be specified");

        if(reclaimAgeMinutes < 0)
            validationIssues.add("reclaimAgeMinutes must be non-negative");

        if(scanDelayMillis < 0)
            validationIssues.add("scanDelayMillis must be non-negative");

        if(targetBatchSize < 1)
            validationIssues.add("targetBatchSize must be 1 or more");

        if(objectScanLimit < 1)
            validationIssues.add("objectScanLimit must be 1 or more");

        String result = validationIssues.size() == 0
               ? null
               : String.join(",", validationIssues);

        if(superValidationResult == null && result == null)
            return null;
        if(superValidationResult == null)
            return result;
        return String.join(",", superValidationResult, result);
    }
}
