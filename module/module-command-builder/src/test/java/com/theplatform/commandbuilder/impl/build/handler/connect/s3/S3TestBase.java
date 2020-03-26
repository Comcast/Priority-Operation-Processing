package com.theplatform.commandbuilder.impl.build.handler.connect.s3;

import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectData;
import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectDataImpl;
import com.theplatform.commandbuilder.impl.build.handler.connect.KeyConversion;
import org.testng.annotations.BeforeTest;

import java.util.HashMap;
import java.util.Map;

public class S3TestBase
{
    protected static final String TEST_S3_ID = "test_s3_id";
    protected static final String TEST_S3_SECRET = "test_s3_secret";

    protected String bucket = "testbucket";
    protected String pathToFile = "/path/to/test.file";
    protected String mount = "/var/s3";
    protected KeyConversion keyConvert = new TestKeyConverter();
    protected KeyConversion keyConversion = new TestKeyConverter();
    protected S3VhsConnect s3VhsParser = new S3VhsConnect(keyConversion);
    protected String s3FilePath;




    protected String s3Url = "https://"+bucket+".s3.amazonaws.com"+pathToFile;
    protected String s3RegionalUrl = "https://"+bucket+".s3-test-region.amazonaws.com"+pathToFile;
    protected String withS3Scheme = "s3://"+bucket+".s3-test-region.amazonaws.com"+pathToFile;
    protected String nonS3Url = "https://testProject.lambda.amazonaws.com"+pathToFile;

    public ConnectData connectData;
    public ConnectData nonS3connectData;
    public S3Data s3Data;

    /**
     * Note that this method may or may not be called during testing depending on the version of testng.  It appears to
     * be more robust to explicitly call this method from within a 'BeforeTest' call in any subclasses
     */
    @BeforeTest
    public void makeConnectData()
    {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(S3ConnectionKeys.ID.name(), TEST_S3_ID);
        paramsMap.put(S3ConnectionKeys.SECRET.name(), TEST_S3_SECRET);
        connectData = new ConnectDataImpl(s3Url, paramsMap);
        nonS3connectData = new ConnectDataImpl(nonS3Url, connectData.getParameters());

        s3Data = s3VhsParser.makeS3Data(connectData);
        s3FilePath = s3Data.getUrl();
    }
}
