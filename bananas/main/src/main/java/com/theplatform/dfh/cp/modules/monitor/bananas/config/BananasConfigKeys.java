package com.theplatform.dfh.cp.modules.monitor.bananas.config;

import com.theplatform.dfh.cp.modules.monitor.config.ConfigKey;
import com.theplatform.dfh.cp.modules.monitor.config.ConfigKeys;
import com.theplatform.dfh.cp.modules.monitor.config.converter.StringArrayPropertyConverter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BananasConfigKeys implements ConfigKeys<ConfigKey>
{
    public static final MessageAttributeKey<Integer> TIME_OUT = new MessageAttributeKey<>("bananas.timeout", null, null, Integer.class);

    public static final ConfigKey<String> DESCRIPTION = new ConfigKey<>("bananas.message.description", null, String.class);
    public static final ConfigKey<String> HOST = new ConfigKey<>("bananas.message.host", null, String.class);
    public static final ConfigKey<String> SERVICE = new ConfigKey<>("bananas.message.service", null, String.class);

    //attributes
    public static final MessageAttributeKey<String[]> TAGS = new MessageAttributeKey<>("bananas.message.tags", null, "tags", String[].class, new StringArrayPropertyConverter(","));
    public static final MessageAttributeKey<String> ORIGIN = new MessageAttributeKey<>("bananas.message.origin", null, "origin", String.class);
    public static final MessageAttributeKey<String> SERVICE_CODE = new MessageAttributeKey<>("bananas.message.service.code", null, "service_code", String.class);
    public static final MessageAttributeKey<String> TSG = new MessageAttributeKey<>("bananas.message.tsg", null, "tsg", String.class);
    public static final MessageAttributeKey<Integer> RETRY_TIME_OUT = new MessageAttributeKey<>("bananas.retry.timeout", null, null, Integer.class);

    public static final MessageAttributeKey<String> ZENOSS_DEVICE = new MessageAttributeKey<>("bananas.message.zenoss.device", null, "zenoss_device", String.class);
    public static final MessageAttributeKey<String> ZENOSS_COMPONENT = new MessageAttributeKey<>("bananas.message.zenoss.component", null, "zenoss_component", String.class);
    public static final MessageAttributeKey<String> ZENOSS_DESCRIPTION = new MessageAttributeKey<>("bananas.message.zenoss.description", null, "zenoss_summary", String.class);
    public static final MessageAttributeKey<String> ZENOSS_INSTANCE = new MessageAttributeKey<>("bananas.message.zenoss.instance", null, "zenoss_instance", String.class);
    public static final MessageAttributeKey<String> ZENOSS_GUID = new MessageAttributeKey<>("bananas.message.zenoss.guid", UUID.randomUUID().toString(), "guid", String.class);

    public static final MessageAttributeKey<String> GRAPHITE_LINK = new MessageAttributeKey<>("bananas.graphite.link", null, "graphite", String.class);
    public static final MessageAttributeKey<String> GRAPHITE_IMAGE = new MessageAttributeKey<>("bananas.graphite.image", null, "graphite_img", String.class);

    public static final MessageAttributeKey<String> SLACK_CHANNEL = new MessageAttributeKey<>("bananas.message.slack.channel", null, "slack_channel", String.class);
    public static final Set<MessageAttributeKey> attributeKeys = new HashSet<>(Arrays.asList(
        ORIGIN,
        SERVICE_CODE,
        TSG,
        ZENOSS_DEVICE,
        ZENOSS_COMPONENT,
        ZENOSS_DESCRIPTION,
        ZENOSS_INSTANCE,
        ZENOSS_GUID,
        GRAPHITE_LINK,
        GRAPHITE_IMAGE,
        SLACK_CHANNEL
    ));
    public static final Set<ConfigKey> keys = new HashSet<>(Arrays.asList(
        HOST,
        DESCRIPTION,
        TAGS,
        SERVICE,
        TIME_OUT,
        RETRY_TIME_OUT,
        ORIGIN,
        SERVICE_CODE,
        TSG,
        ZENOSS_DEVICE,
        ZENOSS_COMPONENT,
        ZENOSS_DESCRIPTION,
        ZENOSS_INSTANCE,
        ZENOSS_GUID,
        GRAPHITE_LINK,
        GRAPHITE_IMAGE,
        SLACK_CHANNEL
    ));


    @Override
    public Set<ConfigKey> getKeys()
    {
        return keys;
    }

    public static Set<MessageAttributeKey> getAttributeKeys() {
         return attributeKeys;
    }
}
