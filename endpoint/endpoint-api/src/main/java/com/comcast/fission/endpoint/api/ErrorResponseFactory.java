package com.comcast.fission.endpoint.api;

/**
 */
// todo rename this.  It's used by services as well as data endpoints
public class ErrorResponseFactory
{

    public static ErrorResponse badRequest(String message, String cid)
    {
        return badRequest(new BadRequestException(message), cid);
    }

    public static ErrorResponse badRequest(BadRequestException e, String cid)
    {
        return buildErrorResponse(e, ErrorResponseCode.BAD_REQUEST, cid);
    }

    public static ErrorResponse unauthorized(String message, String cid)
    {
        return unauthorized(new UnauthorizedException(message), cid);
    }

    public static ErrorResponse unauthorized(UnauthorizedException e, String cid)
    {
        return buildErrorResponse(e, ErrorResponseCode.UNAUTHORIZED, cid);
    }

    public static ErrorResponse objectNotFound(String message, String cid)
    {
        return objectNotFound(new ObjectNotFoundException(message), cid);
    }

    public static ErrorResponse objectNotFound(ObjectNotFoundException e, String cid)
    {
        return buildErrorResponse(e, ErrorResponseCode.OBJECT_NOT_FOUND, cid);
    }

    public static ErrorResponse runtimeServiceException(String message, String cid)
    {
        return buildErrorResponse(new RuntimeServiceException(message, ErrorResponseCode.BAD_REQUEST.getNumber()), ErrorResponseCode.BAD_REQUEST, cid);
    }

    public static ErrorResponse runtimeServiceException(RuntimeServiceException exception, String cid)
    {
        return buildErrorResponse(exception, exception.getResponseCode(), cid);
    }

    public static ErrorResponse buildErrorResponse(Throwable e, int responsecode, String cid)
    {
        return new ErrorResponse(e, responsecode, cid);
    }

    public static ErrorResponse buildErrorResponse(Throwable e, ErrorResponseCode responseCode, String cid)
    {
        return buildErrorResponse(e, responseCode.getNumber(), cid);
    }
}
