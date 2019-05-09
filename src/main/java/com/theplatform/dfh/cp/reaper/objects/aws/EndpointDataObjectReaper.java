package com.theplatform.dfh.cp.reaper.objects.aws;

import com.theplatform.com.dfh.modules.sync.util.Consumer;
import com.theplatform.com.dfh.modules.sync.util.Producer;
import com.theplatform.com.dfh.modules.sync.util.SynchronousProducerConsumerProcessor;
import com.theplatform.dfh.cp.api.EndpointDataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointDataObjectReaper extends SynchronousProducerConsumerProcessor<String>
{
    private static Logger logger = LoggerFactory.getLogger(EndpointDataObjectReaper.class);
    private Class<? extends EndpointDataObject> endpointClass;

    public EndpointDataObjectReaper(String table, Class<? extends EndpointDataObject> endpointClass)
    {
        super(null, null);
        this.endpointClass = endpointClass;
    }

    @Override
    public void processingTimeExpired()
    {
        logger.info("Time expired while performing reap of objects: {}");
    }
}
