package com.comcast.pop.endpoint.api;

public class ObjectNotFoundException extends RuntimeServiceException
{
    private static final int RESPONSE_CODE = 404;

    public ObjectNotFoundException() {
        super(404);
    }

    public ObjectNotFoundException(String message) {
        super(message, 404);
    }

    public ObjectNotFoundException(String message, Throwable cause) {
        super(message, cause, 404);
    }

    public ObjectNotFoundException(Throwable cause) {
        super(cause, 404);
    }
}
