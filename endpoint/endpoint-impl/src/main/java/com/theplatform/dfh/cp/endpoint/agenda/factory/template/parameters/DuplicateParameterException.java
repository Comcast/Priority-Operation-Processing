package com.theplatform.dfh.cp.endpoint.agenda.factory.template.parameters;

import com.comcast.pop.endpoint.api.ValidationException;

public class DuplicateParameterException extends ValidationException
{
    public DuplicateParameterException(String message) {
        super(message);
    }
}
