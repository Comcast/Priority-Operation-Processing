package com.comcast.pop.auth.aws.response;

import com.amazonaws.SdkClientException;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * This class generates the specially formatted authorizer response string
 *
 * This is due to the awkward format of the output for an authorizer (Jackson ObjectMapper magic would be extensive)
 */
public class JsonAuthorizerResponseWriter
{
    private JsonGenerator generator = null;
    private Writer writer = new StringWriter();

    public JsonAuthorizerResponseWriter()
    {
        try
        {
            this.generator = Jackson.jsonGeneratorOf(this.writer);
        }
        catch (IOException e)
        {
            throw new SdkClientException("Unable to instantiate JsonGenerator.", e);
        }
    }

    public String writeAuthorizerResponseToString(AuthorizerResponse authorizerResponse) {
        if(authorizerResponse == null)
        {
            throw new IllegalArgumentException("AuthorizerResponse cannot be null");
        }
        else
        {
            String result;
            try
            {
                result = this.jsonStringOf(authorizerResponse);
            }
            catch (Exception e)
            {
                String message = "Unable to serialize AuthorizerResponse to JSON string: " + e.getMessage();
                throw new IllegalArgumentException(message, e);
            }
            finally
            {
                try
                {
                    this.writer.close();
                }
                catch (Exception e) {}
            }

            return result;
        }
    }

    private String jsonStringOf(AuthorizerResponse authorizerResponse) throws IOException
    {
        generator.writeStartObject();
        generator.writeStringField("principalId", authorizerResponse.getPrincipalId());
        generator.writeFieldName("policyDocument");
        generator.writeRawValue(authorizerResponse.getPolicy().toJson());
        if(authorizerResponse.getPolicyContext() != null)
        {
            generator.writeFieldName("context");
            generator.writeRawValue(authorizerResponse.getPolicyContext().toJson());
        }
        generator.writeEndObject();
        generator.flush();
        return this.writer.toString();
    }
}
