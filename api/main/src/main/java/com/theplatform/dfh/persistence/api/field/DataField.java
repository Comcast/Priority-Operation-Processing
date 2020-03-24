package com.theplatform.dfh.persistence.api.field;

public interface DataField
{
    String name();
    boolean isMatch(String fieldName);
}
