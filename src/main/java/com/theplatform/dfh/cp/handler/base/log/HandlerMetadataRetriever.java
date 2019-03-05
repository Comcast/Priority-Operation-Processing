package com.theplatform.dfh.cp.handler.base.log;


import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.api.args.MetaData;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;

import java.util.HashMap;
import java.util.Map;

public class HandlerMetadataRetriever  implements MetaData<String>
{
    private final LaunchDataWrapper launchDataWrapper;
    private final FieldRetriever fieldRetriever;

    public HandlerMetadataRetriever(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
        fieldRetriever = launchDataWrapper.getEnvironmentRetriever();
    }

    @Override
    public Map<String, String> getMetadata()
    {
        Map<String, String> metadata = new HashMap<>();
        if(fieldRetriever == null)
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
