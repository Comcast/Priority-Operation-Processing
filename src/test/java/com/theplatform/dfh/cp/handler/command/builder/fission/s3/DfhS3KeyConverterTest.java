package com.theplatform.dfh.cp.handler.command.builder.fission.s3;


import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectData;
import com.theplatform.commandbuilder.impl.build.handler.connect.ConnectDataImpl;
import com.theplatform.commandbuilder.impl.build.handler.connect.NoopConnectKey;
import com.theplatform.commandbuilder.impl.build.handler.connect.s3.S3Data;
import com.theplatform.commandbuilder.impl.build.handler.connect.s3.S3VhsConnect;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DfhS3KeyConverterTest
{
    private static final DfhS3KeyConverter keyConverter = new DfhS3KeyConverter();
    private static final String TEST_S3_ID = "test_s3_id";
    private static final String TEST_S3_SECRET = "test_s3_secret";
    private static final String bucket = "testbucket";
    private static final String pathToFile = "/path/to/test.file";
    private static final String s3Url = "https://"+bucket+".s3.amazonaws.com"+pathToFile;

    @Test
    public void testKeyConversion()
    {
        String passwordKey = S3ConnectConvert.password.name();
        String convertedPasswordKey = keyConverter.convertKey(passwordKey).name();
        assertThat(convertedPasswordKey).isEqualTo(S3ConnectConvert.password.getKey().name());

        String username = S3ConnectConvert.username.name();
        String convertedUserNameKey = keyConverter.convertKey(username).name();
        assertThat(convertedUserNameKey).isEqualTo(S3ConnectConvert.username.getKey().name());

        String other = "other";
        String convertedOtherNameKey = keyConverter.convertKey(other).name();
        assertThat(convertedOtherNameKey).isEqualTo(NoopConnectKey.NOOP_CONNECT_KEY);
    }

    @Test
    public void testS3Conversion()
    {
        ConnectData connectData = makeConenctData();
        S3VhsConnect s3VhsParser = new S3VhsConnect(keyConverter);
        S3Data s3Data = s3VhsParser.makeS3Data(connectData);
        assertThat(s3Data.getKey_id()).isEqualTo(TEST_S3_ID);
        assertThat(s3Data.getSecret_access_key()).isEqualTo(TEST_S3_SECRET);
        assertThat(s3Data.getMount()).isEqualTo(S3VhsConnect.DEFAULT_MOUNT);
    }

    @Test
    public void testS3Mount()
    {
        ConnectData connectData = makeConenctData();
        String testS3Mount = "test_S3_Mount";
        connectData.getParameters().put(S3ConnectConvert.mount.name(), testS3Mount);
        S3VhsConnect s3VhsParser = new S3VhsConnect(keyConverter);
        S3Data s3Data = s3VhsParser.makeS3Data(connectData);
        assertThat(s3Data.getMount()).isEqualTo(testS3Mount);
    }


    private ConnectData makeConenctData()
    {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put(S3ConnectConvert.username.name(), TEST_S3_ID);
        paramsMap.put(S3ConnectConvert.password.name(), TEST_S3_SECRET);
        return new ConnectDataImpl(s3Url, paramsMap);
    }
}