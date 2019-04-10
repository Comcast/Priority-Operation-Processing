package com.theplatform.dfh.cp.handler.executor.impl.messages;

import com.theplatform.dfh.cp.handler.base.messages.MessageLookup;
import com.theplatform.dfh.cp.handler.base.messages.PropertyMessages;

public enum ExecutorMessages implements MessageLookup
{
    AGENDA_LOADING,
    AGENDA_LOADED,
    AGENDA_LOAD_FAIL,
    AGENDA_LOAD_INVALID,
    AGENDA_NO_OPERATIONS,
    OPERATIONS_RUNNING,
    OPERATIONS_ERROR,
    OPERATION_EXECUTION_ERROR;

    private static PropertyMessages propertyMessages = new PropertyMessages("com/theplatform/handler/executor/executorMessages");

    @Override
    public String getMessage(Object... args)
    {
        return propertyMessages.getMessage(this.name().toLowerCase(), args);
    }
}
