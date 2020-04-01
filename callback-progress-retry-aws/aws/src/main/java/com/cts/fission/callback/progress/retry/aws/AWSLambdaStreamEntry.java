
package com.cts.fission.callback.progress.retry.aws;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.util.Base64;
import com.cts.fission.callback.progress.retry.AgendaProgressProcessorFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.client.AgendaServiceClientFactory;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.http.api.AuthHttpURLConnectionFactory;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.api.NoAuthHTTPUrlConnectionFactory;
import com.theplatform.dfh.version.info.ServiceBuildPropertiesContainer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.UUID;

/**
 * DynamoDB Trigger lambda for the AgendaProgress table.
 */
public class AWSLambdaStreamEntry implements RequestStreamHandler
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String VAR_FISSION_ENCRYPTED_PASS = "FISSION_ENCRYPTED_PASS";
    private static final String VAR_FISSION_USER = "FISSION_USER";
    private static final String VAR_IDENTITY_URL = "IDENTITY_URL";
    private static final String VAR_AGENDA_CLIENT_PROVIDER_URL = "AGENDA_CLIENT_URL";
    private static final String VAR_AGENDA_PROGRESS_URL = "AGENDA_PROGRESS_URL";
    private static final String CID = "CID";

    private static final String RECORDS_NODE = "/Records";

    private AgendaProgressProcessorFactory agendaProgressProcessorFactory = new AgendaProgressProcessorFactory();
    private AgendaServiceClientFactory agendaServiceClientFactory = new AgendaServiceClientFactory();

    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        ServiceBuildPropertiesContainer.logServiceBuildString(logger);
        // this is immediately made available for subclasses
        JsonNode rootRequestNode = objectMapper.readTree(inputStream);

        logObject("request: ", rootRequestNode);

        // NOTE: did you want to map this via json? Too bad! The AWS classes have multiple setters (so this won't work).
        JsonNode recordsNode = rootRequestNode.at(RECORDS_NODE);
        if(recordsNode.isMissingNode()) return;
        if(!recordsNode.isArray()) return;

        // track the processed link ids so the same one is not processed multiple times in a batch
        HashSet<String> processedIds = new HashSet<>();
        recordsNode.iterator().forEachRemaining(record -> processRecord(record, processedIds));
    }

    private void processRecord(JsonNode recordNode, HashSet<String> processedIds)
    {
        switch (getField(recordNode, "eventName", ""))
        {
            case "MODIFY":
                String id = getFieldAt(recordNode, "/dynamodb/NewImage/id/S");
                String cid = getFieldAt(recordNode, "/dynamodb/NewImage/cid/S");
                String customerId = getFieldAt(recordNode, "/dynamodb/NewImage/customerId/S");
                // Retrieve this as the primary filter before any additional (and slightly more expensive) processing
                String processingState = getFieldAt(recordNode, "/dynamodb/NewImage/processingState/S");
                if(id == null)
                {
                    logger.warn("Cannot update with the following invalid information: id: {}", id);
                    break;
                }
                if(StringUtils.equalsIgnoreCase(processingState, ProcessingState.COMPLETE.name()))
                {
                    // NOTE: This is not thread safe (not that threading this is even a consideration for this)
                    if(processedIds.contains(id)) return;

                    AgendaProgress agendaProgress = new AgendaProgress();
                    agendaProgress.setId(id);
                    agendaProgress.setCustomerId(customerId);
                    agendaProgress.setCid(cid);
                    handleAgendaProgressUpdate(agendaProgress);
                }
                break;
        }
    }

    private String getFieldAt(JsonNode node, String fieldPath)
    {
        JsonNode fieldNode = node.at(fieldPath);
        if(fieldNode.isMissingNode()) return null;
        return fieldNode.asText();
    }

    private String getField(JsonNode node, String fieldName, String defaultValue)
    {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isMissingNode() ? defaultValue : fieldNode.asText();
    }

    private void logObject(String nodeName, JsonNode node) throws JsonProcessingException
    {
        if(!logger.isDebugEnabled()) return;

        if(node != null)
        {
            logger.debug("[{}]\n{}", nodeName, objectMapper.writeValueAsString(node));
        }
        else
        {
            logger.debug("[{}] node not found", nodeName);
        }
    }

    private void handleAgendaProgressUpdate(AgendaProgress agendaProgress)
    {
        String cid = agendaProgress.getCid() == null ? UUID.randomUUID().toString() : agendaProgress.getCid();
        // all logs from this point forward will include the cid
        MDC.put(CID, cid);

        String identityUrl = System.getenv(VAR_IDENTITY_URL);
        String fissionUser = System.getenv(VAR_FISSION_USER);
        String fissionEncryptedPass = getEncryptedVarFromEnvironment(VAR_FISSION_ENCRYPTED_PASS);
        String agendaClientUrl = System.getenv(VAR_AGENDA_CLIENT_PROVIDER_URL);
        String agendaProgressUrl = System.getenv(VAR_AGENDA_PROGRESS_URL);

        // test logging (no we don't want to log the password)
        /*logger.info("idm: {} user: {} pass: {} agendaClientURL: {}, agendaProgressUrl: {}",
            identityUrl,
            fissionUser,
            fissionEncryptedPass == null ? null : fissionEncryptedPass.substring(0, 1),
            agendaClientUrl,
            agendaProgressUrl);*/


        HttpURLConnectionFactory httpUrlConnectionFactory = new AuthHttpURLConnectionFactory();

        agendaProgressProcessorFactory.createAgendaProgressProcessor(
            agendaServiceClientFactory.create(agendaClientUrl, httpUrlConnectionFactory), new HttpObjectClient<>(agendaProgressUrl, httpUrlConnectionFactory, AgendaProgress.class))
            .process(agendaProgress);
    }

    private String getEncryptedVarFromEnvironment(String varName)
    {
        // hacked from the pipeline modules
        String encryptedEnvVar = System.getenv(varName);
        if(encryptedEnvVar == null)
        {
            return null;
        }

        byte[] encryptedData = Base64.decode(encryptedEnvVar);

        AWSKMS client = AWSKMSClientBuilder.defaultClient();

        DecryptRequest request = new DecryptRequest().withCiphertextBlob(ByteBuffer.wrap(encryptedData));

        ByteBuffer plainTextKey = client.decrypt(request).getPlaintext();
        return new String(plainTextKey.array(), Charset.forName("UTF-8"));
    }

    public void setAgendaProgressProcessorFactory(AgendaProgressProcessorFactory agendaProgressProcessorFactory)
    {
        this.agendaProgressProcessorFactory = agendaProgressProcessorFactory;
    }
}