package com.theplatform.dfh.cp.endpoint.agendatemplate;

import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.visibility.*;
import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 * AgendaTemplate specific RequestProcessor
 * Agenda Templates can have global read access. This allows for a single template in a resource
 */
public class AgendaTemplateRequestProcessor extends EndpointDataObjectRequestProcessor<AgendaTemplate>
{
    private static final AnyMatchVisibilityFilter globalObjectReadVisibilityFilter =
        new AnyMatchVisibilityFilter()
            .withFilter(new GlobalObjectVisibilityFilter())
            .withFilter(new CustomerVisibilityFilter())
            .withFilter(new AllowedCustomerVisibiltyFilter());

    public AgendaTemplateRequestProcessor(ObjectPersister<AgendaTemplate> agendaTemplateObjectPersister)
    {
        super(agendaTemplateObjectPersister);
        //allow global and allowed customers for READS
        setVisibilityFilter(VisibilityMethod.GET, globalObjectReadVisibilityFilter);
    }

}
