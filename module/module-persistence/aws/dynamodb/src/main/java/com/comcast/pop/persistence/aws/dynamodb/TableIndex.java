package com.comcast.pop.persistence.aws.dynamodb;

/**
 * Basic definition for a DynamoDB table index (sort key is optional)
 */
public class TableIndex
{
    private String name;
    private String partitionKey;
    private String sortKey;

    public TableIndex(String name, String partitionKey)
    {
        this(name, partitionKey, null);
    }

    public TableIndex(String name, String partitionKey, String sortKey)
    {
        this.name = name;
        this.partitionKey = partitionKey;
        this.sortKey = sortKey;
    }

    public String getName()
    {
        return name;
    }

    public String getPartitionKey()
    {
        return partitionKey;
    }

    public String getSortKey()
    {
        return sortKey;
    }

    /**
     * Is the specified field the partition or sort key
     * @param fieldName The field to check
     * @return true if the field is a match, false otherwise
     */
    public boolean isPrimaryKey(String fieldName)
    {
        if(fieldName == null) return false;
        return fieldName.equals(partitionKey) || fieldName.equals(sortKey);
    }
}
