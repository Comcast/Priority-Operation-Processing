package com.comast.pop.handler.base.validation.string;

import java.util.List;

public class MultiStringValidator implements StringValidator
{
    private List<StringValidator> validators;

    public MultiStringValidator(List<StringValidator> validators)
    {
        this.validators = validators;
    }

    public void addValidator(StringValidator validator)
    {
        validators.add(validator);
    }

    @Override
    public void validate(String input)
    {
        if(validators == null) return;
        for(StringValidator validator : validators)
            validator.validate(input);
    }
}
