package com.theplatform.dfh.cp.handler.command.builder.fission.s3;

import com.theplatform.commandbuilder.impl.build.handler.NoopConnectKey;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

public class DfhS3KeyConverterTest
{
    DfhS3KeyConverter keyConverter = new DfhS3KeyConverter();

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

}