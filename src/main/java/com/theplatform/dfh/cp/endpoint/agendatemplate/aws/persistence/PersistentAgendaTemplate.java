package com.theplatform.dfh.cp.endpoint.agendatemplate.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.ListOperationsConverter;
import com.theplatform.dfh.cp.endpoint.persistence.DateConverter;
import com.theplatform.dfh.cp.endpoint.persistence.ParamsMapConverter;

import java.util.Date;
import java.util.List;
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
    @DynamoDBAttribute(attributeName = "templateParameters")
    public ParamsMap getTemplateParameters()
    {
        return super.getTemplateParameters();
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
    public boolean getIsGlobal()
    {
        return super.getIsGlobal();
    }
}
