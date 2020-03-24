package com.theplatform.dfh.endpoint.api.data.query.scheduling;

import com.theplatform.dfh.persistence.api.field.DataObjectField;
import com.theplatform.dfh.persistence.api.query.Query;

/**
 * This is a query against a composite field consisting of both the InsightId and CustomerId
 */
public class ByInsightIdCustomerId extends Query<String>
{
    private static final DataObjectField field = new DataObjectField("insightIdCustomerIdComposite");
    public ByInsightIdCustomerId(String insightId, String customerId)
    {
        if(insightId == null || insightId.isEmpty()
            || customerId == null || customerId.isEmpty())
        {
            throw new IllegalArgumentException("By Query requires a non-empty value.");
        }

        setField(field);
        setValue(insightId+customerId);
        setCollection(false);
    }

    public static String fieldName()
    {
        return field.name();
    }


}
