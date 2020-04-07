package com.theplatform.dfh.cp.endpoint.validation;

import com.comcast.pop.endpoint.api.progress.ProgressSummaryRequest;
import com.comcast.pop.endpoint.api.DefaultServiceRequest;
import com.comcast.pop.endpoint.api.ValidationException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ProgressServiceValidatorTest
{
    private ProgressServiceValidator validator = new ProgressServiceValidator();

    @DataProvider
    public Object[][] missingLinkIdProvider()
    {
        return new Object[][]
            {
                {null},
                {new ProgressSummaryRequest()}
            };
    }

    @Test(dataProvider = "missingLinkIdProvider", expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*LinkId is required.*")
    public void testMissingLinkId(ProgressSummaryRequest request)
    {
        validator.validatePOST(new DefaultServiceRequest<>(request));
    }
}
