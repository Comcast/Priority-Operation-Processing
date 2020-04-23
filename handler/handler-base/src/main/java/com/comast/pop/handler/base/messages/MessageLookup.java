package com.comast.pop.handler.base.messages;

/**
 * Interface for enums to use as a basis for looking up messages (see ResourceBundleStringRetriever)
 */
public interface MessageLookup
{
    String getMessage(Object... args);
    String getKey();
}
