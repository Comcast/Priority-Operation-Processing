package com.theplatform.dfh.cp.modules.monitor.bananas.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.theplatform.dfh.cp.modules.monitor.alert.AlertLevel;
import com.theplatform.dfh.cp.modules.monitor.alert.AlertMessage;
import com.theplatform.dfh.cp.modules.monitor.bananas.config.*;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigurationProperties;

import java.util.*;

/**
 * A bananas message is comprised of some top level fields and then an attribute bag.
 * See BananasConfigKeys for our supported usage.
 */
public class BananasMessage implements AlertMessage
{
    private AlertLevel alertLevel = AlertLevel.INFO;
    private String host;
    private String service;
    private long time; //Note - NO timezone
    private String[] tags;
    private String description;
    private Map<String, Object> attributes;

    public BananasMessage()
    {
        // to prevent instantiation of BananaMessage without the builder
        attributes = new HashMap<>();
    }

    @Override
    @JsonIgnore
    //This class is marshalled into JSON and the bananas endpoint doesn't understand level. This is an application specific field
    //See status
    public AlertLevel getLevel()
    {
        return alertLevel;
    }

    @Override
    public void setLevel(AlertLevel alertLevel)
    {
        this.alertLevel = alertLevel;
    }

    public String getHost()
    {
        return this.host;
    }

    public String getService()
    {
        return this.service;
    }

    public String getState()
    {
        return this.alertLevel.name();
    }

    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    public long getTime()
    {
        return this.time;
    }

    public String[] getTags()
    {
        return this.tags;
    }

    //Allow configuration of TAGS so that we are not spammed with notifications on Slack
    void setTags(String[] tags)
    {
        this.tags = tags;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void putAttribute(String key, Object value)
    {
        if(value != null)
            this.attributes.put(key, value);
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setService(String service)
    {
        this.service = service;
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    public static BananasMessage fromConfigurationProperties(ConfigurationProperties properties)
    {
        return fromConfigurationProperties(properties, BananasConfigKeys.getAttributeKeys());
    }
    public static BananasMessage fromConfigurationProperties(ConfigurationProperties properties, Set<MessageAttributeKey> attributeTypeKeys)
    {
        BananasMessage message = new BananasMessage();
        message.setHost(properties.get(BananasConfigKeys.HOST));
        message.setService(properties.get(BananasConfigKeys.SERVICE));
        message.setTags((String[])properties.get(BananasConfigKeys.TAGS));
        message.setDescription(properties.get(BananasConfigKeys.DESCRIPTION));

        if(attributeTypeKeys != null)
        {
            for(MessageAttributeKey key : attributeTypeKeys)
            {
                if (key != null)
                {
                    message.putAttribute(key.getAttributeKey(), properties.get(key));
                }
            }

        }

        return message;
    }
}
