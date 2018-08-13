package com.theplatform.dfh.cp.handler.base;

public interface InMemoryHandler<T>
{
    T execute(String payload);
}
