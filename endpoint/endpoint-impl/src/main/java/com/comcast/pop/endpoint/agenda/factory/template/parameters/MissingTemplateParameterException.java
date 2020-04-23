package com.comcast.pop.endpoint.agenda.factory.template.parameters;

import com.comcast.pop.endpoint.api.ValidationException;

public class MissingTemplateParameterException extends ValidationException
{
    public MissingTemplateParameterException(String message) {
        super(message);
    }
}
