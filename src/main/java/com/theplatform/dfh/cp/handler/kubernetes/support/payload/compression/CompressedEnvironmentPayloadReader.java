package com.theplatform.dfh.cp.handler.kubernetes.support.payload.compression;

import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.handler.base.payload.PayloadReader;

import java.util.Base64;

/**
 * Specialized reader the processes compressed payloads across potentially multiple environment variables (OS limitation fun)
 */
public class CompressedEnvironmentPayloadReader extends BaseCompressedEnvironmentPayload implements PayloadReader
{
    private LaunchDataWrapper launchDataWrapper;

    public CompressedEnvironmentPayloadReader(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    @Override
    public String readPayload()
    {
        EnvironmentFieldRetriever fieldRetriever = launchDataWrapper.getEnvironmentRetriever();
        int index = 0;
        StringBuilder payloadBuilder = new StringBuilder();
        while(true)
        {
            String payloadSubpart = fieldRetriever.getField(getEnvironmentVariableName(index));
            if(payloadSubpart == null)
                break;
            payloadBuilder.append(payloadSubpart);
            index++;
        }
        String base64Encoded = payloadBuilder.toString();
        byte[] compressedBytes = Base64.getDecoder().decode(base64Encoded);
        return zlibUtil.inflateMe(compressedBytes);
    }
}
