package com.comcast.pop.persistence.api.field;

public interface DataField
{
    String name();
    boolean isMatch(String fieldName);
}
