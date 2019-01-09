package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.ServiceResponse;

public interface RequestProcessor<Res extends ServiceResponse, Req extends ServiceRequest>
{
    Res handleGET(Req request);

    Res handlePOST(Req request);

    Res handlePUT(Req request);

    Res handleDelete(Req request);

}
