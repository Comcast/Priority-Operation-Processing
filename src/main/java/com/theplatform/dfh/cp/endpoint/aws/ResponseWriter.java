package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Class for writing AWS responses to the OutputStream for use with implementations of RequestStreamHandler
 * (not sure if this will expand much)
 */
public class ResponseWriter
{
    /**
     * Writes a standard AWS response object to the OutputStream
     * @param outputStream The OutputStream to write to
     * @param objectMapper The ObjectMapper to use to convert objects to json
     * @param httpStatusCode The status code to respond with
     * @param responseBodyObject (optional) The object to write as json to the response body
     * @throws IOException
     */
    public void writeResponse(OutputStream outputStream, ObjectMapper objectMapper, int httpStatusCode, Object responseBodyObject) throws IOException
    {
        String responseBody = responseBodyObject == null ? null : objectMapper.writeValueAsString(responseBodyObject);
        String response = objectMapper.writeValueAsString(new AWSLambdaStreamResponseObject(httpStatusCode, responseBody));
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(response);
        writer.close();
    }
}
