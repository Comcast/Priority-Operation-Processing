package com.comcast.pop.endpoint.test.base;

import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.ServiceResponse;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.test.CidGenerator;
import com.comcast.pop.test.JsonUtil;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.api.TransformRequest;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.facility.ResourcePool;
import com.comcast.pop.api.facility.SchedulingAlgorithm;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.test.cleanup.endpoint.IdentifiedObjectCleanupManager;
import com.comcast.pop.endpoint.client.AgendaServiceClient;
import com.comcast.pop.endpoint.client.HttpObjectClient;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.endpoint.client.ObjectClient;
import com.comcast.pop.endpoint.client.ProgressServiceClient;
import com.comcast.pop.endpoint.client.ResourcePoolServiceClient;
import com.comcast.pop.http.api.HttpURLConnectionFactory;
import com.comcast.pop.object.api.IdentifiedObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@ContextConfiguration(locations = "classpath:spring-endpoint.xml")
public class EndpointTestBase extends AbstractTestNGSpringContextTests
{
    private static final Logger logger = LoggerFactory.getLogger(EndpointTestBase.class);

    public static final String COOLEST_DOG_SMALL_UUID = "ed709ac1-6889-498d-b079-28bcc2c9a923";
    public static final String EFS_TFM_LOCATION = "EFS";
    private List<Long> fileIdsToClean = new ArrayList<>();
    protected JsonHelper jsonHelper = new JsonHelper();

    @Resource
    protected IdentifiedObjectCleanupManager identifiedObjectCleanupManager;

    @Resource
    protected String cid;

    @Resource
    protected HttpURLConnectionFactory httpUrlConnectionFactory;

    @Resource
    protected HttpObjectClient<AgendaProgress> agendaProgressClient;

    @Resource
    protected AgendaServiceClient agendaServiceClient;

    @Resource
    protected HttpObjectClient<OperationProgress> operationProgressClient;

    @Resource
    protected HttpObjectClient<AgendaTemplate> agendaTemplateClient;

    @Resource
    protected HttpObjectClient<Agenda> agendaClient;

    @Resource
    protected HttpObjectClient<ResourcePool> resourcePoolClient;

    @Resource
    protected HttpObjectClient<Customer> customerClient;

    @Resource
    protected HttpObjectClient<Insight> insightClient;

    @Resource
    protected ResourcePoolServiceClient resourcePoolServiceClient;

    @Resource
    protected ProgressServiceClient progressServiceClient;

    @Resource
    protected String testCustomerId;

    @Resource
    protected String testInsightQueueName;

    @Resource
    protected String testInsightId;

    @Resource
    protected String testInsightOperation;

    @Resource
    protected String testResourcePoolId;

    @Resource
    protected String agendaTemplateUrl;

    @Resource
    protected String agendaUrl;

    @Resource
    protected String agendaServiceUrl;

    @Resource
    protected String agendaProgressUrl;

    @Resource
    protected String operationProgressUrl;

    @Resource
    protected String resourcePoolServiceUrl;

    @Resource
    protected String progressProviderUrl;

    @Resource
    protected String resourcePoolUrl;

    @Resource
    protected String customerUrl;

    @Resource
    protected String insightUrl;

    @BeforeMethod
    public void regenerateCid(Method method)
    {
        cid = CidGenerator.generateCid(method);
    }

    @AfterMethod
    public void clean()
    {
        identifiedObjectCleanupManager.clean();
    }

    /**
     * Registers the AgendaProgress and (loosely) associated OperationProgress for cleanup
     * @param agendaProgressId The id of the AgendaProgress to register
     * @param operations The operations to clean up in association with the AgendaProgress
     */
    protected void registerProgressObjectsForCleanup(String agendaProgressId, List<Operation> operations)
    {
        registerProgressObjectIdsForCleanup(
            agendaProgressId,
            operations.stream().map(Operation::getName).collect(Collectors.toList())
        );
    }

    /**
     * Registers the AgendaProgress and (loosely) associated OperationProgress for cleanup
     * @param agendaProgressId The id of the AgendaProgress to register
     * @param operationNames The operationNames to clean up in association with the AgendaProgress
     */
    protected void registerProgressObjectIdsForCleanup(String agendaProgressId, List<String> operationNames)
    {
        identifiedObjectCleanupManager.getIdentifiedObjectCreateTracker().registerForCleanup(AgendaProgress.class, agendaProgressId);
        logger.info("Created AgendaProgress id: {}", agendaProgressId);
        for (String operationName : operationNames)
        {
            String operationProgressId = OperationProgress.generateId(agendaProgressId, operationName);
            identifiedObjectCleanupManager.getIdentifiedObjectCreateTracker().registerForCleanup(OperationProgress.class, operationProgressId);
            logger.info("Created OperationProgress id: {}", operationProgressId);
        }
    }

    public <T extends IdentifiedObject> T persistDataObject(ObjectClient<T> objectClient, Class<T> objectClass, T object)
    {
        DataObjectResponse<T> response = objectClient.persistObject(object);
        verifyNoError(response);
        Assert.assertNotNull(response.getFirst());
        T createdObject = response.getFirst();
        identifiedObjectCleanupManager.getIdentifiedObjectCreateTracker().registerForCleanup(objectClass, createdObject.getId());
        return createdObject;
    }

    public void verifyError(ServiceResponse<ErrorResponse> response, int responseCode, String expectedTitle, String expectedDescriptionFragment)
    {
        verifyError(response, responseCode, expectedTitle);
        if(expectedDescriptionFragment != null)
            Assert.assertTrue(StringUtils.containsIgnoreCase(response.getErrorResponse().getDescription(), expectedDescriptionFragment),
                "Expected description to contain fragment " + expectedDescriptionFragment);
    }

    public void verifyError(ServiceResponse<ErrorResponse> response, int responseCode, String expectedTitle)
    {
        Assert.assertTrue(response.isError(), "Expected response error. Response was: " + new JsonHelper().getPrettyJSONString(response));
        Assert.assertEquals(response.getErrorResponse().getResponseCode(), (Integer) responseCode,
            String.format("Expected response code: %1$s Actual: %2$s. Response was: %3$s ",
                response.getErrorResponse().getResponseCode(),
                responseCode,
                new JsonHelper().getPrettyJSONString(response)));

        if (expectedTitle != null && expectedTitle.length() >0 )
            Assert.assertEquals(response.getErrorResponse().getTitle(), expectedTitle);
    }

    public void verifyNoError(ServiceResponse response)
    {
        if (response.isError())
        {
            Assert.fail("Request returned unexpected error: " + new JsonHelper().getPrettyJSONString(response));
        }
    }
    public TransformRequest getTransformRequestFromFile(String fileName) throws IOException
    {
        return JsonUtil.toObjectFromFile(fileName, TransformRequest.class);
    }

    protected HttpURLConnectionFactory getDefaultHttpURLConnectionFactory()
    {
        return httpUrlConnectionFactory;
    }

    public Insight createTestInsight()
    {
        Insight insight = new Insight();
        insight.setCustomerId(testCustomerId);
        insight.setQueueName(testInsightQueueName);
        insight.setTitle("testGetAgendaWithInsight " + cid);
        insight.setQueueSize(1);
        Map<String, Set<String>> operationMapper = new HashMap<>();
        operationMapper.put("operationType", Collections.singleton(testInsightOperation));
        insight.setMappers(operationMapper);
        insight.setSchedulingAlgorithm(SchedulingAlgorithm.FirstInFirstOut);
        insight.setResourcePoolId(testResourcePoolId);
        insight = insightClient.persistObject(insight).getFirst();
        logger.info("New Insight id: {}", insight.getId());
        return insight;
    }
}
