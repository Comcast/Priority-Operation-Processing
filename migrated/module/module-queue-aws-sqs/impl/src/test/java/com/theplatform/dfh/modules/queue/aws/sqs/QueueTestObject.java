package com.theplatform.dfh.modules.queue.aws.sqs;

import java.util.Objects;

public class QueueTestObject
{
    private String id;
    private String field;

    public QueueTestObject(){}

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        QueueTestObject that = (QueueTestObject) o;
        return Objects.equals(getId(), that.getId()) &&
            Objects.equals(getField(), that.getField());
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(getId(), getField());
    }
}
