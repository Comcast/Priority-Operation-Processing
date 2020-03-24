package com.theplatform.dfh.cp.handler.base.log;

import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.field.api.args.MetaData;
import com.theplatform.dfh.cp.handler.base.field.retriever.api.FieldRetriever;

import java.util.HashMap;
import java.util.Map;

public class HandlerMetadataRetriever  implements MetaData<String>
{
    private final FieldRetriever fieldRetriever;
    private final Map<String, String> metadata;

    public HandlerMetadataRetriever(FieldRetriever fieldRetriever)
    {
        this.fieldRetriever = fieldRetriever;
        metadata = new HashMap<>();
    }

    @Override
    public Map<String, String> getMetadata()
    {
        if(fieldRetriever == null || !metadata.isEmpty())
        {
            return metadata;
        }
        for(HandlerField fieldKey: HandlerField.values())
        {
            if(fieldRetriever.isFieldSet(fieldKey.name()))
            {
                String key = fieldKey.name();
                String value = fieldRetriever.getField(key);
                metadata.put(key,value);
            }
        }
        return metadata;
    }
}
