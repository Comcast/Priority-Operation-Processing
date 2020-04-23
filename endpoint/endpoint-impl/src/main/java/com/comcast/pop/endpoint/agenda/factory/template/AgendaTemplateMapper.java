package com.comcast.pop.endpoint.agenda.factory.template;

import com.comcast.pop.endpoint.agenda.factory.template.parameters.BasicParametersExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.modules.jsonhelper.replacement.JsonReferenceReplacer;
import com.comcast.pop.endpoint.api.RuntimeServiceException;

import java.util.HashMap;
import java.util.Map;

public class AgendaTemplateMapper
{
    private JsonHelper jsonHelper;
    private JsonReferenceReplacer referenceReplacer;
    private BasicParametersExtractor basicParametersExtractor;

    public AgendaTemplateMapper()
    {
        this.referenceReplacer = new JsonReferenceReplacer();
        this.jsonHelper = new JsonHelper();
        this.basicParametersExtractor = new BasicParametersExtractor();
    }

    public Agenda map(AgendaTemplate agendaTemplate, JsonNode inputParams)
    {
        JsonNode agendaTemplateRoot = jsonHelper.getObjectMapper().valueToTree(agendaTemplate);
        Map<String, JsonNode> parameterMap = new HashMap<>();
        basicParametersExtractor.updateParameterMap(parameterMap, agendaTemplateRoot.get("staticParameters"));
        // NOTE: previously we supported "required" parameters that used the AgendaTemplateParametersExtractor (no longer required)
        basicParametersExtractor.updateParameterMap(parameterMap, inputParams);

        JsonNode agendaTemplateNode = agendaTemplateRoot.get("agenda");

        referenceReplacer.replaceReferences(agendaTemplateNode, parameterMap);

        try
        {
            return jsonHelper.getObjectMapper().treeToValue(agendaTemplateNode, Agenda.class);
        }
        catch (Exception e)
        {
            throw new RuntimeServiceException(String.format("Failed to generate Agenda from template: [%1$s]", agendaTemplate.getTitle()), e, 500);
        }
    }

    public AgendaTemplateMapper setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
        return this;
    }

    public AgendaTemplateMapper setReferenceReplacer(JsonReferenceReplacer referenceReplacer)
    {
        this.referenceReplacer = referenceReplacer;
        return this;
    }

    public AgendaTemplateMapper setBasicParametersExtractor(BasicParametersExtractor basicParametersExtractor)
    {
        this.basicParametersExtractor = basicParametersExtractor;
        return this;
    }
}
