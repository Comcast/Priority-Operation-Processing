package com.comcast.pop.endpoint.agendatemplate.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.endpoint.persistence.DateConverter;
import com.comcast.pop.endpoint.persistence.ParamsMapConverter;

import java.util.Date;
import java.util.Set;

/**
 */
public class PersistentAgendaTemplate extends AgendaTemplate
{

    @Override
    @DynamoDBHashKey
    public String getId()
    {
        return super.getId();
    }

    @Override
    @DynamoDBTypeConverted(converter = DateConverter.class)
    public Date getUpdatedTime()
    {
        return super.getUpdatedTime();
    }

    @Override
    @DynamoDBTypeConverted(converter = DateConverter.class)
    public Date getAddedTime()
    {
        return super.getAddedTime();
    }

    @Override
    public String getCustomerId()
    {
        return super.getCustomerId();
    }

    @Override
    public String getCid()
    {
        return super.getCid();
    }

    @Override
    @DynamoDBTypeConverted(converter = ParamsMapConverter.class)
    @DynamoDBAttribute(attributeName = "staticParameters")
    public ParamsMap getStaticParameters()
    {
        return super.getStaticParameters();
    }

    @Override
    @DynamoDBTypeConverted(converter = ParamsMapConverter.class)
    @DynamoDBAttribute(attributeName = "params")
    public ParamsMap getParams()
    {
        return super.getParams();
    }

    @Override
    public String getTitle()
    {
        return super.getTitle();
    }

    @Override
    @DynamoDBTypeConverted(converter = AgendaConverter.class)
    @DynamoDBAttribute(attributeName = "agenda")
    public Agenda getAgenda()
    {
        return super.getAgenda();
    }

    @Override
    public Set<String> getAllowedCustomerIds()
    {
        return super.getAllowedCustomerIds();
    }

    @Override
    public Boolean getIsDefaultTemplate()
    {
        return super.getIsDefaultTemplate();
    }

    @Override
    public Boolean getIsGlobal()
    {
        return super.getIsGlobal();
    }
}
