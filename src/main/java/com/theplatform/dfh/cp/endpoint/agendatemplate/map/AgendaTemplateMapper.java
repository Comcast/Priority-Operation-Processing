package com.theplatform.dfh.cp.endpoint.agendatemplate.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.endpoint.agendatemplate.map.parameters.AgendaTemplateParametersExtractor;
import com.theplatform.dfh.cp.endpoint.agendatemplate.map.parameters.StaticParametersExtractor;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonReferenceReplacer;

import java.util.HashMap;
import java.util.Map;

public class AgendaTemplateMapper
{
    private JsonHelper jsonHelper;
    private JsonReferenceReplacer referenceReplacer;
    private StaticParametersExtractor staticParametersExtractor;
    private AgendaTemplateParametersExtractor agendaTemplateParametersExtractor;

    public AgendaTemplateMapper()
    {
        this.referenceReplacer = new JsonReferenceReplacer();
        this.jsonHelper = new JsonHelper();
        this.staticParametersExtractor = new StaticParametersExtractor();
        this.agendaTemplateParametersExtractor = new AgendaTemplateParametersExtractor();
    }

    public Agenda map(AgendaTemplate agendaTemplate, JsonNode inputParams)
    {
        JsonNode agendaTemplateRoot = jsonHelper.getObjectMapper().valueToTree(agendaTemplate);
        Map<String, JsonNode> parameterMap = new HashMap<>();
        staticParametersExtractor.updateParameterMap(parameterMap, agendaTemplateRoot.get("staticParameters"));
        agendaTemplateParametersExtractor
            .setRequiredParameters(agendaTemplateRoot.get("templateParameters"))
            .updateParameterMap(parameterMap, inputParams);

        JsonNode agendaTemplateNode = agendaTemplateRoot.get("agenda");

        referenceReplacer.replaceReferences(agendaTemplateNode, parameterMap);

        try
        {
            return jsonHelper.getObjectMapper().treeToValue(agendaTemplateNode, Agenda.class);
        }
        catch (Exception e){}
        return null;
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

    public AgendaTemplateMapper setStaticParametersExtractor(StaticParametersExtractor staticParametersExtractor)
    {
        this.staticParametersExtractor = staticParametersExtractor;
        return this;
    }

    public AgendaTemplateMapper setAgendaTemplateParametersExtractor(
        AgendaTemplateParametersExtractor agendaTemplateParametersExtractor)
    {
        this.agendaTemplateParametersExtractor = agendaTemplateParametersExtractor;
        return this;
    }
}
