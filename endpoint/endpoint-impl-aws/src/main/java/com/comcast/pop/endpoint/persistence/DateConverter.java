package com.comcast.pop.endpoint.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.util.Date;

public class DateConverter implements DynamoDBTypeConverter<Long, Date>
{
    @Override
    public Long convert(Date date)
    {
        if(date == null) return -1L;
        return date.getTime();
    }

    @Override
    public Date unconvert(Long longValue)
    {
        if(longValue == null) return new Date();
        return new Date(longValue);
    }
}
