package com.comcast.pop.handler.kubernetes.support.podconfig.client.registry.api;

public class PodConfigRegistryClientException extends Exception {
    public PodConfigRegistryClientException() {
        super();
    }

    public PodConfigRegistryClientException(String message) {
        super(message);
    }

    public PodConfigRegistryClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public PodConfigRegistryClientException(Throwable cause) {
        super(cause);
    }

    protected PodConfigRegistryClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
