package com.theplatform.dfh.cp.handler.base.reporter;

public interface ProgressReporter<T>
{
    /**
     * Reports the specified object as progress
     * @param object The object to report progress with
     */
    void reportProgress(T object);

    /**
     * Reports the specified object and the resulting payload
     * @param object The object to report progress with
     * @param resultPayload The resulting payload
     */
    void reportProgress(T object, Object resultPayload);
}
