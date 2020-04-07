package com.comcast.pop.auth.aws;

import com.amazonaws.auth.policy.Statement;
import com.amazonaws.services.lambda.runtime.Context;
import com.comcast.pop.auth.aws.response.AuthorizerOutputBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.endpoint.aws.AbstractLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.LambdaRequest;
import com.comcast.pop.endpoint.base.RequestProcessor;
import com.comcast.pop.endpoint.api.BadRequestException;
import com.comcast.pop.endpoint.api.ServiceResponse;
import com.theplatform.dfh.version.info.ServiceBuildPropertiesContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Fission Sample authorizer
 * Sends the AWS Policy to the API Gateway for verification (assumes all is well, NO ACTUAL AUTHENTICATION IS TAKING PLACE)
 * A bunch of the API code was following https://github.com/awslabs/aws-apigateway-lambda-authorizer-blueprints/blob/master/blueprints/python/api-gateway-authorizer-python.py
 *
 * throw new RuntimeException("Unauthorized"); = 401 error back to the caller of the API Gateway
 */
public class AWSLambdaStreamEntry extends AbstractLambdaStreamEntry<ServiceResponse,LambdaRequest>
{
    private static final Logger logger = LoggerFactory.getLogger(AWSLambdaStreamEntry.class);
    private static final String STAGE_VAR_SERVICE_NAME = "serviceName";
    private static final String STAGE_VAR_SERVICE_INSTANCE = "serviceInstance";

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        ServiceBuildPropertiesContainer.logServiceBuildString(logger);

        JsonNode jsonNode = getObjectMapper().readTree(inputStream);
        logObject("Received request: ", jsonNode);
        LambdaRequest lambdaRequest = new LambdaRequest(jsonNode);
        setupLoggingMDC(jsonNode);

        //resource in format /fission/agenda/service
        final String token = lambdaRequest.getAuthorizationHeader();
        final String methodArn = lambdaRequest.getRequestValue("/methodArn");

        if(logger.isDebugEnabled())
        {
            //logger.debug("token {}", token);
            logger.debug("methodArn {}", methodArn);
        }

        // setup the basics for the output builder
        AuthorizerOutputBuilder authorizerOutputBuilder = new AuthorizerOutputBuilder();
        authorizerOutputBuilder.withMethodArn(methodArn);

        //TODO: Make your own Authorize call here!
        // assume all is well
        authorizerOutputBuilder.withStatementEffect(Statement.Effect.Allow);

        // TODO: for reference when you implement your auth failure cases
        /*
        catch(AuthenticationException e)
        {
            // according to the AWS blueprint (and actual observation), this causes a 401
            throw new RuntimeException("Unauthorized");
        }
        catch(AuthorizationException e)
        {
            // This will result in a 403
            authorizerOutputBuilder.withStatementEffect(Statement.Effect.Deny);
        }
         */


        String responseBody = authorizerOutputBuilder.build();
        if(logger.isDebugEnabled()) logger.debug("Response Body {}", responseBody);
        writeToStream(outputStream, responseBody);
    }

    private void writeToStream(OutputStream outputStream, String response) throws IOException
    {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write(response);
        writer.close();
    }

    @Override
    public RequestProcessor getRequestProcessor(LambdaRequest request)
    {
        return null;
    }

    @Override
    public LambdaRequest getRequest(JsonNode jsonNode) throws BadRequestException
    {
        return null;
    }
}
