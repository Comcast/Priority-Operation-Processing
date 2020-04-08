package com.comcast.pop.persistence.aws.dynamodb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Table Index definition for AWS DynamoDB
 *
 * Note: Composite keys for an index are limited to the primary and sort key only
 */
public class TableIndexes
{
    private HashMap<String, List<TableIndex>> fieldToIndexSetMap = new HashMap<>();
    private String primaryKey = "id";

    public TableIndexes withPrimaryKey(String fieldName)
    {
        this.primaryKey = fieldName;
        return this;
    }

    public TableIndexes withIndex(String indexName, String fieldName)
    {
        appendTableIndex(fieldName, new TableIndex(indexName, fieldName));
        return this;
    }

    public TableIndexes withIndex(String indexName, String fieldName, String sortKey)
    {
        appendTableIndex(fieldName, new TableIndex(indexName, fieldName, sortKey));
        return this;
    }

    public TableIndexes withIndex(TableIndex tableIndex)
    {
        appendTableIndex(tableIndex.getPartitionKey(), tableIndex);
        return this;
    }

    public boolean isPrimary(String fieldName)
    {
       return fieldName != null && primaryKey != null && fieldName.equals(primaryKey);
    }

    protected void appendTableIndex(String fieldName, TableIndex tableIndex)
    {
        fieldToIndexSetMap.computeIfAbsent(fieldName, v -> new LinkedList<>()).add(tableIndex);
    }

    /**
     * Gets the first index associated with the specified field
     * @param fieldName The field to look up
     * @return First associated table index or null if none found
     */
    public TableIndex getTableIndex(String fieldName)
    {
        if(fieldName == null) return null;
        // just select any TableIndex from the field's set
        List<TableIndex> tableIndexSet = fieldToIndexSetMap.get(fieldName);
        return (tableIndexSet == null || tableIndexSet.size() == 0)
            ? null
            : tableIndexSet.get(0);
    }

    /**
     * Gets the best fitting index based on the fields supplied. The first matching partition/sort composite index will be returned.
     * If the caller is trying to target a specific index the fields should be in search priority order
     * @param fields The fields to search with
     * @return The first best fit or null if nothing can be found
     */
    public TableIndex getBestTableIndexMatch(List<String> fields)
    {
        if(fields == null || fields.size() == 0) return null;
        if(fields.size() == 1) return getTableIndex(fields.get(0));

        // NOTE: this set technically includes more data than required in the lookup below (no harm)
        Set<String> otherFields = new HashSet<>(fields);

        TableIndex bestIndexMatch = null;
        for(String field : fields)
        {
            List<TableIndex> tableIndexSet = fieldToIndexSetMap.get(field);
            if(tableIndexSet == null || tableIndexSet.size() == 0) continue;

            // look for matching sort keys
            Optional<TableIndex> tableIndexSearch = tableIndexSet.stream()
                .filter(tableIndex -> otherFields.contains(tableIndex.getSortKey()))
                .findAny();
            // if both the primary and sort key are found this is a good index match
            if(tableIndexSearch.isPresent()) return tableIndexSearch.get();
            if(bestIndexMatch == null) bestIndexMatch = tableIndexSet.get(0);
        }
        return bestIndexMatch;
    }
}
