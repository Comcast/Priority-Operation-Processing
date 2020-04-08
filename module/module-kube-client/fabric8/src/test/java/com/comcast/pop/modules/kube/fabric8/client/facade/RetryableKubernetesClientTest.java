package com.comcast.pop.modules.kube.fabric8.client.facade;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.mockito.Mockito.mock;

public class RetryableKubernetesClientTest
{
    private RetryableKubernetesClient retryableKubernetesClient;
    private DefaultKubernetesClient mockKubernetesClient;

    // kubernetes output is nanosecond based
    private final String RFC3339_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
        .ofPattern(RFC3339_FORMAT)
        .withZone(ZoneId.of("UTC"));

    private final Long TEST_TIME_MS = 1566861570381L;
    private final String TEST_TIME_STRING = "2019-08-26T23:19:30.381101127Z";

    @BeforeMethod
    public void setup()
    {
        mockKubernetesClient = mock(DefaultKubernetesClient.class, Mockito.RETURNS_DEEP_STUBS);
        retryableKubernetesClient = new RetryableKubernetesClient(mockKubernetesClient);
    }

    @DataProvider
    public Object[][] datePrefixProvider()
    {
        return new Object[][]
            {
                {null, null},
                {"", null},
                {dateTimeFormatter.format(Instant.ofEpochMilli(0)), 0L },
                {dateTimeFormatter.format(Instant.ofEpochMilli(0)) + " extra data", 0L },
                {dateTimeFormatter.format(Instant.ofEpochMilli(0)) + "extra data", null },
                {dateTimeFormatter.format(Instant.ofEpochMilli(0).plusMillis(1000)), 1000L },
                // real example from kubernetes
                {TEST_TIME_STRING, TEST_TIME_MS},
                {TEST_TIME_STRING + " other stuff", TEST_TIME_MS},
                {TEST_TIME_STRING + " other stuff\n", TEST_TIME_MS},
                {
                    "2019-08-26T23:19:24.381101123Z other stuff\n"
                    + "2019-08-26T23:19:25.381101124Z other stuff\n"
                    + "2019-08-26T23:19:26.381101125Z other stuff\n"
                    + "2019-08-26T23:19:27.381101126Z other stuff\n"
                    + TEST_TIME_STRING + " other stuff\n",
                    TEST_TIME_MS},
                {
                    "2019-08-26T23:19:24.381101123Z other stuff\n"
                        + "2019-08-26T23:19:25.381101124Z other stuff\n"
                        + "2019-08-26T23:19:26.381101125Z other stuff\n"
                        + "2019-08-26T23:19:27.381101126Z other stuff\n"
                        + TEST_TIME_STRING + " other stuff",
                    TEST_TIME_MS}
            };
    }

    @Test(dataProvider = "datePrefixProvider")
    public void testParseStringDatePrefix(String input, Long expected)
    {
        Assert.assertEquals(retryableKubernetesClient.parseDatePrefix(input), expected);
    }
}
