package com.theplatform.dfh.cp.endpoint.agenda.factory.template.parameters;

import com.theplatform.dfh.endpoint.api.ValidationException;

public class MissingTemplateParameterException extends ValidationException
{
    public MissingTemplateParameterException(String message) {
        super(message);
    }
}
