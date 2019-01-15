package com.theplatform.dfh.cp.handler.executor.impl.registry.podconfig;

public class LogbackData
{
    private static final String PACKAGER_SYSLOG_DATA = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "\n" +
            "<configuration>\n" +
            "\n" +
            "    <appender name=\"syslogServiceAppender\" class=\"ch.qos.logback.classic.net.SyslogAppender\">\n" +
            "        <filter class=\"com.theplatform.module.logging.extension.logback.filter.LevelMatchFilter\" />\n" +
            "        <syslogHost>syslog.logging</syslogHost>\n" +
            "        <facility>local0</facility>\n" +
            "        <port>514</port>\n" +
            "\n" +
            "        <!--debugging-->\n" +
            "        <!--<syslogHost>10.112.14.70</syslogHost>-->\n" +
            "        <!--<facility>local0</facility>-->\n" +
            "        <!--<port>5140</port>-->\n" +
            "\n" +
            "        <throwableExcluded>true</throwableExcluded>\n" +
            "        <!-- WARNING: THE HARD CODED CLUSTER MUST BE RESOLVED ?? -->\n" +
            "        <suffixPattern>fhpkm {\"index\": \"service\", \"ccds_cluster_title\": \"lab-main-t-aor-fhpkm-t01\", \"type\": \"fhpkm-service\", \"event\": \"%d %X{CID} %-5p [%t] %c: %replace(%m){'(?=[\\\"\\\\])', '\\\\'}%replace( %replace(%xException){'(?=[\\\"\\\\])', '\\\\'} ){'\\n', '\\\\n'}\"}%n</suffixPattern>\n" +
            "    </appender>\n" +
            "\n" +
            "    <appender name=\"asyncLogAppender\" class=\"com.theplatform.module.logging.extension.logback.appender.AsyncAppender\">\n" +
            "        <appender-ref ref=\"syslogServiceAppender\" />\n" +
            "    </appender>\n" +
            "\n" +
            "    <logger name=\"org.springframework\" level=\"ERROR\" />\n" +
            "    <logger name=\"org.apache\" level=\"ERROR\" />\n" +
            "    <logger name=\"httpclient.wire.header\" level=\"ERROR\" />\n" +
            "    <logger name=\"com.theplatform.web.marshalling\" level=\"ERROR\" />\n" +
            "    <logger name=\"io.fabric8.kubernetes.client.Config\" level=\"INFO\" />\n" +
            "\n" +
            "    <root level=\"${LogLevel:-DEBUG}\">\n" +
            "        <appender-ref ref=\"asyncLogAppender\" />\n" +
            "    </root>\n" +
            "\n" +
            "</configuration>";


    public static String getPackagerSyslogData()
    {
        return PACKAGER_SYSLOG_DATA;
    }
}
