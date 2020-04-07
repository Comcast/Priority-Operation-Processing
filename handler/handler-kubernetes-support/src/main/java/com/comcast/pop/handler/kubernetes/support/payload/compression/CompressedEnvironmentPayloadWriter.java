package com.comcast.pop.handler.kubernetes.support.payload.compression;

import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comcast.pop.handler.kubernetes.support.payload.BaseExecutionConfigPayloadWriter;
import com.comcast.pop.handler.kubernetes.support.payload.PayloadType;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Specialized writer that compresses, base64 encodes, and splits the result across environment variables
 */
public class CompressedEnvironmentPayloadWriter extends BaseExecutionConfigPayloadWriter
{
    private static Logger logger = LoggerFactory.getLogger(CompressedEnvironmentPayloadWriter.class);

    private static final int DEFAULT_MAX_PAYLOAD_SEGMENT_LENGTH = 100_000;
    private CompressedEnvironmentPayloadUtil compressedEnvironmentPayloadUtil = new CompressedEnvironmentPayloadUtil();
    private int maxPayloadSegmentLength = DEFAULT_MAX_PAYLOAD_SEGMENT_LENGTH;

    public CompressedEnvironmentPayloadWriter(ExecutionConfig executionConfig)
    {
        super(executionConfig);
    }

    public CompressedEnvironmentPayloadWriter(ExecutionConfig executionConfig, LaunchDataWrapper launchDataWrapper)
    {
        this(executionConfig);
        this.maxPayloadSegmentLength =
            launchDataWrapper.getPropertyRetriever().getInt(CompressedPayloadField.PAYLOAD_COMPRESSION_MAX_SEGMENT_SIZE, DEFAULT_MAX_PAYLOAD_SEGMENT_LENGTH);
    }

    @Override
    public void writePayload(String payload, Map<String, String> outputMap)
    {
        byte[] compressedPayload = compressedEnvironmentPayloadUtil.getZlibUtil().deflateMe(payload.getBytes(StandardCharsets.UTF_8));
        String base64Encoded = Base64.getEncoder().encodeToString(compressedPayload);
        logger.trace(String.format("Original Size: %1$s Compressed Size: %2$s Base64+Compressed Size: %3$s",
            payload.length(), compressedPayload.length, base64Encoded.length()));
        writePartsToMap(base64Encoded, outputMap);
    }

    protected void writePartsToMap(String fullEntry, Map<String, String> outputMap)
    {
        int index = 0;
        int startIndex = 0;
        do
        {
            int readLength = Math.min(fullEntry.length() - startIndex, maxPayloadSegmentLength);
            int endIndex = startIndex + readLength;
            outputMap.put(compressedEnvironmentPayloadUtil.getEnvironmentVariableName(index), fullEntry.substring(startIndex, endIndex));
            startIndex += readLength;
            index++;
        }while(startIndex < fullEntry.length());
    }

    public void setMaxPayloadSegmentLength(int maxPayloadSegmentLength)
    {
        this.maxPayloadSegmentLength = maxPayloadSegmentLength;
    }

    @Override
    public String getPayloadType()
    {
        return PayloadType.COMPRESSED_ENV_VAR.getFieldName();
    }

    public void setCompressedEnvironmentPayloadUtil(CompressedEnvironmentPayloadUtil compressedEnvironmentPayloadUtil)
    {
        this.compressedEnvironmentPayloadUtil = compressedEnvironmentPayloadUtil;
    }

    public CompressedEnvironmentPayloadUtil getCompressedEnvironmentPayloadUtil()
    {
        return compressedEnvironmentPayloadUtil;
    }
}
