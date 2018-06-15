package com.theplatform.dfh.cp.api.source;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Text extends Source
{
    /**
     * languageCode: en
     */
    private String languageCode;

    /**
     * intent: closedCaptions
     */
    private String intent;

    @JsonProperty
    public String getLanguageCode()
    {
        return languageCode;
    }

    @JsonProperty
    public void setLanguageCode(String languageCode)
    {
        this.languageCode = languageCode;
    }

    @JsonProperty
    public String getIntent()
    {
        return intent;
    }

    @JsonProperty
    public void setIntent(String intent)
    {
        this.intent = intent;
    }
}
