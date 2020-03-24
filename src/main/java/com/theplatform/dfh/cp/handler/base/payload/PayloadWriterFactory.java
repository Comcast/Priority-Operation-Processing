package com.theplatform.dfh.cp.handler.base.payload;

public interface PayloadWriterFactory<T>
{
    PayloadWriter createWriter(T param);
}
