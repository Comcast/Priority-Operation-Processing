package com.comcast.pop.commandbuilder.impl.build.handler.connect.s3;

import com.comcast.pop.commandbuilder.impl.build.handler.connect.ConnectData;
import com.comcast.pop.commandbuilder.impl.build.handler.connect.ConnectDataImpl;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class S3VhsConnectTest extends S3TestBase
{

    @BeforeTest
    public void init()
    {
        makeConnectData();
    }

    @DataProvider(name = "provideUrls")
    Object[][] provideUrls()
    {
     return new Object[][]
             {
                     {s3Url, Boolean.TRUE},
                     {s3RegionalUrl, Boolean.TRUE},
                     {withS3Scheme, Boolean.TRUE},
                     {nonS3Url, Boolean.FALSE}

             };
    }

    @Test(dataProvider = "provideUrls")
    public void testIsS3(String url, boolean expectedResult)
    {
        assertThat(S3TypeUtil.isParsable(url)).isEqualTo(expectedResult);
    }

    @Test
    public void testGetBucket()
    {
        assertThat(makeS3Data(s3Url).getBucket()).isEqualTo(bucket);
        assertThat(makeS3Data(s3RegionalUrl).getBucket()).isEqualTo(bucket);
    }


    @Test
    public void testGetFilePath()
    {
        assertThat(makeS3Data(s3Url).getUrl()).isEqualTo("\"" + mount + pathToFile + "\"");
        assertThat(makeS3Data(s3RegionalUrl).getUrl()).isEqualTo("\"" + mount + pathToFile + "\"");
    }

    private S3Data makeS3Data(String s3Url)
    {
        return s3VhsParser.makeS3Data(makeInput(s3Url));
    }

    private ConnectData makeInput(String s3Url)
    {
        Map<String,String> params = new HashMap<>();
        ConnectData connectData = new ConnectDataImpl(s3Url, params);
        return connectData;
    }
}