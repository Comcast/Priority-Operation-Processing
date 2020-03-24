package com.theplatform.dfh.cp.handler.puller.impl.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/alive")
@Produces(MediaType.TEXT_PLAIN)
public class AliveEndpoint
{
    private static Logger logger = LoggerFactory.getLogger(AliveEndpoint.class);

    @GET
    public String getAlive()
    {
        logger.info("Puller is alive");
        return "Puller is alive";
    }
}
