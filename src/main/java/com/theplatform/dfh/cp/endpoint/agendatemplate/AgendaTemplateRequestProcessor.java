package com.theplatform.dfh.cp.endpoint.agendatemplate;

import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 * AgendaTemplate specific RequestProcessor
 */
public class AgendaTemplateRequestProcessor extends EndpointDataObjectRequestProcessor<AgendaTemplate>
{
    public AgendaTemplateRequestProcessor(ObjectPersister<AgendaTemplate> agendaTemplateObjectPersister)
    {
        super(agendaTemplateObjectPersister);
    }

}
