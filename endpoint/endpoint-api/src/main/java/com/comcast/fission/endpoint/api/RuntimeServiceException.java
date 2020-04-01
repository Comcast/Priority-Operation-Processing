package com.comcast.fission.endpoint.api;

public class RuntimeServiceException
        extends RuntimeException
{
    // the response code
    protected final int responseCode;

    // the correlation ID
    private String correlationId;

    /**
     * Constructor.
     *
     * @param responseCode the response code
     */
    protected RuntimeServiceException(int responseCode)
    {
        super(Integer.toString(responseCode));

        this.responseCode = responseCode;
    }

    /**
     * Constructor.
     *
     * @param message the error message
     * @param responseCode the response code
     */
    public RuntimeServiceException(String message, int responseCode)
    {
        super(message);

        this.responseCode = responseCode;
    }

    /**
     * Constructor.
     *
     * @param message the error message
     * @param cause inner exception
     * @param responseCode the response code
     */
    public RuntimeServiceException(String message, Throwable cause, int responseCode)
    {
        super(message, cause);

        this.responseCode = responseCode;
    }

    /**
     * Constructor.
     *
     * @param cause inner exception
     * @param responseCode the response code
     */
    public RuntimeServiceException(Throwable cause, int responseCode)
    {
        super(cause);

        this.responseCode = responseCode;
    }

    /**
     * {@inheritDoc}
     */
    public int getResponseCode()
    {
        return responseCode;
    }

    /**
     * {@inheritDoc}
     */
    public String getCorrelationId()
    {
        return correlationId;
    }

    /**
     * Sets the correlation ID.
     *
     * @param correlationId the correlation ID
     */
    public RuntimeServiceException withCorrelationId(String correlationId)
    {
        this.correlationId = correlationId;
        return this;
    }

}

