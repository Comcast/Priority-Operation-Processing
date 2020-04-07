package com.comcast.pop.endpoint.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.OutputStream;

public interface JsonRequestStreamHandler extends RequestStreamHandler
{
    void handleRequest(JsonNode inputStreamNode, OutputStream outputStream, Context context) throws IOException;
}
