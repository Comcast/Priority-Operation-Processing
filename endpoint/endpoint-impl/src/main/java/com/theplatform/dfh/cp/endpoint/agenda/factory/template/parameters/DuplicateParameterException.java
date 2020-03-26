package com.theplatform.dfh.cp.endpoint.agenda.factory.template.parameters;

import com.theplatform.dfh.endpoint.api.ValidationException;

public class DuplicateParameterException extends ValidationException
{
    public DuplicateParameterException(String message) {
        super(message);
    }
}
