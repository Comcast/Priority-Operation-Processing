package com.comcast.fission.auth.aws.response;

import com.amazonaws.auth.policy.Action;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PolicyBuilder
{
    private String restApiId;
    private String region;
    private String stage;
    /**
     * The AWS account id the policy will be generated for. This is used to create
     * the method ARNs.
     *
     * @property awsAccountId
     * @type {String}
     */
    private String awsAccountId;
    /**
     * The regular expression used to validate resource paths for the policy
     *
     * @property pathRegex
     * @type {RegExp}
     * @default '^\/[/.a-zA-Z0-9-\*]+$'
     */
    private static final String pathRegex = "^[/.a-zA-Z0-9-\\*]+$";


    // these are the internal lists of allowed and denied methods.
    // No conditions supported.
    private List<Statement> allowMethods = new ArrayList<>();
    private List<Statement> denyMethods = new ArrayList<>();
    /**
     * A set of existing HTTP verbs supported by API Gateway. This property is here
     * only to avoid spelling mistakes in the policy.
     *
     * @property HttpVerb
     * @type {Object}
     */
    enum HttpVerb
    {
        GET("GET"),
        POST("POST"),
        PUT("PUT"),
        PATCH("PATCH"),
        HEAD("HEAD"),
        DELETE("DELETE"),
        OPTIONS("OPTIONS"),
        ALL("*");
        private String method;

        HttpVerb(String method)
        {
            this.method = method;
        }

        static boolean hasMethod(String method)
        {
            return Arrays.stream(HttpVerb.values()).anyMatch(m -> m.method.equals(method));
        }
    }

    public PolicyBuilder()
    {
    }

    public PolicyBuilder(AuthorizerRequest authorizeRequest)
    {
        withAwsAccountId(authorizeRequest.getAwsAccountId());
        withRegion(authorizeRequest.getRegion());
        withRestApiId(authorizeRequest.getRestApiId());
        withStage(authorizeRequest.getStage());
    }

    public PolicyBuilder withRegion(String region)
    {
        this.region = region;
        return this;
    }

    public PolicyBuilder withRestApiId(String restApiId)
    {
        this.restApiId = restApiId;
        return this;
    }

    public PolicyBuilder withAwsAccountId(String awsAccountId)
    {
        this.awsAccountId = awsAccountId;
        return this;
    }

    public PolicyBuilder withStage(String stage)
    {
        this.stage = stage;
        return this;
    }

    public PolicyBuilder withStatement(Action action, Statement.Effect effect, String verb, String resource)
    {
        if (!"*".equals(verb)
                && !HttpVerb.hasMethod(verb)) {
            throw new Error("Invalid HTTP verb " + verb + ". Allowed verbs in AuthPolicy.HttpVerb");
        }
        if (!resource.matches(pathRegex)) {
            throw new Error("Invalid resource path: " + resource + ". Path should match " + pathRegex);
        }
        if (resource != null
                && resource.length() > 1
                && (resource.substring(0, 1).equals("/"))) {
            resource = resource.substring(1, resource.length());
        }

        String resourceArn = "arn:aws:execute-api:" +
                this.region + ":" +
                this.awsAccountId + ":" +
                this.restApiId + "/" +
                this.stage + "/" +
                verb + "/" +
                resource;
        Statement statement = new Statement(effect);
        statement.setActions(Collections.singletonList(action));
        statement.setResources(Collections.singletonList(new Resource(resourceArn)));

        if (effect == Statement.Effect.Allow)
        {
            this.allowMethods.add(statement);
        }
        else if (effect == Statement.Effect.Deny)
        {
            this.denyMethods.add(statement);
        }
        return this;
    }

    public Policy build(String principal)
    {
        if ((allowMethods == null || allowMethods.size() == 0) &&
                (denyMethods == null || denyMethods.size() == 0))
        {
            throw new Error("No statements defined for the policy");
        }
        List<Statement> statements = new ArrayList<>();
        statements.addAll(allowMethods);
        statements.addAll(denyMethods);
        //Not adding the extra 'context' field to the policy that can be passed through to lambda or called resource.
        //see : https://github.com/awslabs/aws-apigateway-lambda-authorizer-blueprints/blob/master/blueprints/python/api-gateway-authorizer-python.py
        return new Policy("AWS:authentication:" + principal, statements);
    }
}
