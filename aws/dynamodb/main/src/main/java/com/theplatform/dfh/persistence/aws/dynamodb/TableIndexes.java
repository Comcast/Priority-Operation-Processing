package com.theplatform.dfh.persistence.aws.dynamodb;

import java.util.*;

public class TableIndexes
{
    private List<TableIndex> indexes = new ArrayList<>();

    public TableIndexes withIndex(String indexName, String ... fieldNames)
    {
        this.indexes.add(new TableIndex(indexName, new HashSet(Arrays.asList(fieldNames))));
        return this;
    }

    public String getIndex(List<String> fields)
    {
        for(TableIndex index : indexes)
        {
            if (index.fieldNames.containsAll(fields))
                return index.indexName;
        }
        return null;
    }

    private class TableIndex
    {
        private String indexName;
        private Set<String> fieldNames;

        public TableIndex(String indexName, Set<String> fieldNames)
        {
            this.indexName = indexName;
            this.fieldNames = fieldNames;
        }

    }
}
