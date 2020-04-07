package com.comast.pop.handler.base.payload;

public interface PayloadWriterFactory<T>
{
    PayloadWriter createWriter(T param);
}
