package com.theplatform.dfh.cp.modules.jsonhelper.replacement;

import java.util.HashSet;
import java.util.Set;

public class ReferenceReplacementResult
{
    private String result;
    private Set<String> missingReferences;
    private Set<String> invalidReferences;

    public ReferenceReplacementResult()
    {
        missingReferences = new HashSet<>();
        invalidReferences = new HashSet<>();
    }

    public String getResult()
    {
        return result;
    }

    public void setResult(String result)
    {
        this.result = result;
    }

    public void addMissingReference(String missingReference)
    {
        missingReferences.add(missingReference);
    }

    public void addInvalidReference(String invalidReference)
    {
        invalidReferences.add(invalidReference);
    }

    public Set<String> getMissingReferences()
    {
        return missingReferences;
    }

    public void setMissingReferences(Set<String> missingReferences)
    {
        this.missingReferences = missingReferences;
    }

    public Set<String> getInvalidReferences()
    {
        return invalidReferences;
    }

    public void setInvalidReferences(Set<String> invalidReferences)
    {
        this.invalidReferences = invalidReferences;
    }
}
