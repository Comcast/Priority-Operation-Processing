package com.theplatform.dfh.cp.endpoint.agendatemplate;

import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.endpoint.base.EndpointDataObjectRequestProcessor;
import com.comcast.pop.endpoint.base.visibility.*;
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

    @Override
    protected AgendaTemplate defaultFieldsOnCreate(AgendaTemplate object)
    {
        if(object.getIsGlobal() == null) object.setIsGlobal(false);
        if(object.getIsDefaultTemplate() == null) object.setIsDefaultTemplate(false);
        return object;
    }
}
