package com.theplatform.dfh.endpoint.api;

/**
 */
public class DataObjectErrorResponses
{

    public static ErrorResponse badRequest(String message, String cid)
    {
        return badRequest(new BadRequestException(message), cid);
    }

    public static ErrorResponse badRequest(BadRequestException e, String cid)
    {
        return buildErrorResponse(e, 400, cid);
    }

    public static ErrorResponse unauthorized(String message, String cid)
    {
        return unauthorized(new UnauthorizedException(message), cid);
    }

    public static ErrorResponse unauthorized(UnauthorizedException e, String cid)
    {
        return buildErrorResponse(e, 403, cid);
    }

    public static ErrorResponse objectNotFound(String message, String cid)
    {
        return objectNotFound(new ObjectNotFoundException(message), cid);
    }

    public static ErrorResponse objectNotFound(ObjectNotFoundException e, String cid)
    {
        return buildErrorResponse(e, 404, cid);
    }

    public static ErrorResponse buildErrorResponse(Throwable e, int responseCode, String cid)
    {
        return new ErrorResponse(e, responseCode, cid);
    }
}
