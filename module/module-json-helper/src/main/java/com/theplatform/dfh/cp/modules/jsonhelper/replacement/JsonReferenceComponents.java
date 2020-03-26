package com.theplatform.dfh.cp.modules.jsonhelper.replacement;

public class JsonReferenceComponents
{
    private String parameter;
    private String jsonPtrExpr;

    public JsonReferenceComponents(String parameter, String jsonPtrExpr)
    {
        this.parameter = parameter;
        this.jsonPtrExpr = jsonPtrExpr;
    }

    public String getParameter()
    {
        return parameter;
    }

    public void setParameter(String parameter)
    {
        this.parameter = parameter;
    }

    public String getJsonPtrExpr()
    {
        return jsonPtrExpr;
    }

    public void setJsonPtrExpr(String jsonPtrExpr)
    {
        this.jsonPtrExpr = jsonPtrExpr;
    }
}
