package com.theplatform.dfh.cp.endpoint.resourcepool;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.client.DataObjectRequestProcessorClient;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.query.resourcepool.insight.ByResourcePoolId;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Request processor for the Facility Endpoint
 */
public class ResourcePoolRequestProcessor extends DataObjectRequestProcessor<ResourcePool>
{
    private ObjectClient<Insight> insightClient;

    public ResourcePoolRequestProcessor(ObjectPersister<ResourcePool> resourcePoolObjectPersister,
        ObjectPersister<Insight> insightObjectPersister)
    {
        super(resourcePoolObjectPersister);
        insightClient = new DataObjectRequestProcessorClient<>(new InsightRequestProcessor(insightObjectPersister));

    }

    @Override
    protected DataObjectResponse<ResourcePool> handleGET(DataObjectRequest<ResourcePool> request)
    {
        DataObjectResponse<ResourcePool> response = super.handleGET(request);
        addInsightIds(response);
        return response;
    }

    /**
     * Updates the response object with the Insight Ids associated with each ResourcePool
     * @param response
     */
    protected void addInsightIds(DataObjectResponse<ResourcePool> response)
    {
        if(response != null && !response.isError() && response.getCount() != null)
        {
            for(ResourcePool resourcePool : response.getAll())
            {
                DataObjectResponse<Insight> insightsResponse = insightClient.getObjects(Collections.singletonList(new ByResourcePoolId(resourcePool.getId())));
                if(!insightsResponse.isError())
                {
                    if(insightsResponse.getCount() != null)
                    {
                        resourcePool.setInsightIds(
                            insightsResponse.getAll().stream().map(Insight::getId).collect(Collectors.toList()));
                    }
                    // if the count is null but not an error, whatever
                }
                else
                {
                    // send back the error response from the insight lookup with a note about the insight lookup failure.
                    ErrorResponse errorResponse = insightsResponse.getErrorResponse();
                    errorResponse.setDescription(
                        String.format("Failed to lookup insights for ResourcePool: [%1$s] : %2$s",
                            resourcePool.getId(), insightsResponse.getErrorResponse().getDescription()));
                    // update the existing response with the new error
                    response.setErrorResponse(errorResponse);
                    // break out completely
                    break;
                }
            }
        }
    }

    protected ResourcePoolRequestProcessor setInsightClient(ObjectClient<Insight> insightClient)
    {
        this.insightClient = insightClient;
        return this;
    }
}
