package com.theplatform.dfh.cp.api.source;

import com.theplatform.dfh.cp.api.FileResource;
import com.theplatform.dfh.cp.api.FileResourceType;
import com.theplatform.dfh.cp.api.ParamsMap;

import java.util.Map;

public class TextResource implements FileResource
{
    private FileResource fileReference;
    /**
     * languageCode: en
     */
    private final static String languageCode = "languageCode";

    /**
     * intent: closedCaptions
     */
    private final static String intent = "intent";

    public TextResource(FileResource fileReference)
    {
        this.fileReference = fileReference;
    }

    @Override
    public int getIndex()
    {
        return fileReference.getIndex();
    }

    @Override
    public void setIndex(int index)
    {
        fileReference.setIndex(index);
    }

    @Override
    public String getLabel()
    {
        return fileReference.getLabel();
    }

    @Override
    public void setLabel(String label)
    {
        fileReference.setLabel(label);
    }

    @Override
    public String getUsername()
    {
        return fileReference.getUsername();
    }

    @Override
    public void setUsername(String username)
    {
        fileReference.setUsername(username);
    }

    @Override
    public String getPassword()
    {
        return fileReference.getPassword();
    }

    @Override
    public void setPassword(String password)
    {
        fileReference.setPassword(password);
    }

    @Override
    public String getType()
    {
        return FileResourceType.text.name();
    }

    @Override
    public void setType(String type)
    {
        fileReference.setType(type);
    }

    @Override
    public String getUrl()
    {
        return fileReference.getUrl();
    }

    @Override
    public void setUrl(String url)
    {
        fileReference.setUrl(url);
    }

    @Override
    public ParamsMap getParams()
    {
        return fileReference.getParams();
    }

    public String getLanguageCode()
    {
        return getString(languageCode);
    }

    public String getIntent()
    {
        return getString(intent);
    }

    public String getString(final String name)
    {
        if(fileReference == null || fileReference.getParams() == null) return null;
        return (String)fileReference.getParams().get(name);
    }
}
