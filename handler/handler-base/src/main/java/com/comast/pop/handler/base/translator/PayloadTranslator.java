package com.comast.pop.handler.base.translator;

public interface PayloadTranslator<T>
{
    /**
     * Translates the payload to a object of the specified type
     * @param payload The payload to translate
     * @param clazz The class to convert the payload to
     * @return The translation result
     */
    PayloadTranslationResult<T> translatePayload(String payload, Class<T> clazz);
}
