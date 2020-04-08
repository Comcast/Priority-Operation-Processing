package com.comcast.pop.commandbuilder.impl.build.handler;

import com.comcast.pop.commandbuilder.impl.build.handler.utilities.PathUtil;
import org.testng.annotations.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class PathUtilTest
{

    @Test
    public void testQuoteFilepath()
    {
        assertSoftly(softly ->
        {
            softly.assertThat(PathUtil.quoteFilepath(null)).isNull();
            softly.assertThat(PathUtil.quoteFilepath("")).isEmpty();

            String unQuotedText = "abc def";
            String quotedText = "\"" + unQuotedText + "\"";

            softly.assertThat(PathUtil.quoteFilepath(quotedText)).isEqualTo(quotedText);
            softly.assertThat(PathUtil.quoteFilepath(unQuotedText)).isEqualTo(quotedText);
            });
    }
}