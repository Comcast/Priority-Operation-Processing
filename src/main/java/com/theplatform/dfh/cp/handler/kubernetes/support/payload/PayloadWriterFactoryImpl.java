package com.theplatform.dfh.cp.handler.kubernetes.support.payload;

import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.payload.PayloadWriter;
import com.theplatform.dfh.cp.handler.base.payload.PayloadWriterFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.payload.compression.CompressedEnvironmentPayloadWriter;
import org.apache.commons.lang3.StringUtils;

public class PayloadWriterFactoryImpl implements PayloadWriterFactory
{
    private LaunchDataWrapper launchDataWrapper;

    public PayloadWriterFactoryImpl(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    @Override
    public PayloadWriter createWriter()
    {
        // if payload compression is on, use it!
        if(StringUtils.equalsIgnoreCase(Boolean.TRUE.toString(), launchDataWrapper.getPropertyRetriever().getField(PayloadField.PAYLOAD_COMPRESSION_ENABLED)))
        {
            return new CompressedEnvironmentPayloadWriter(launchDataWrapper);
        }
        return new EnvironmentPayloadWriter();
    }
}
