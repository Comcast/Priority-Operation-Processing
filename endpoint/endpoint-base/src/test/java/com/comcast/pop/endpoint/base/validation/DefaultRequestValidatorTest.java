package com.comcast.pop.endpoint.base.validation;

import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ValidationException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultRequestValidatorTest
{
    private DefaultRequestValidator<ServiceRequest<Integer>> defaultRequestValidator = new DefaultRequestValidator<>();

    @DataProvider
    public Object[][] validationIssuesProvider()
    {
        return new Object[][]
            {
                {null, false, false},
                {generateValidationIssues(0), false, false},
                {generateValidationIssues(1), true, false},
                {generateValidationIssues(defaultRequestValidator.getMaxValidationIssuesToReport()), true, false},
                {generateValidationIssues(defaultRequestValidator.getMaxValidationIssuesToReport() + 1), true, true},
                {generateValidationIssues(defaultRequestValidator.getMaxValidationIssuesToReport() * 2), true, true}
            };
    }

    @Test(dataProvider = "validationIssuesProvider")
    public void testProcessValidationIssues(List<String> validationIssues, final boolean EXPECT_ERROR, final boolean EXPECT_TRUNCATED)
    {
        try
        {
            defaultRequestValidator.processValidationIssues(validationIssues);
            if(EXPECT_ERROR)
                Assert.fail("Expected ValidationException");
        }
        catch(ValidationException e)
        {
            String message = e.getMessage();
            Assert.assertNotNull(message);
            Assert.assertEquals(message.contains(DefaultRequestValidator.VALIDATION_TRUNCATION_MESSAGE), EXPECT_TRUNCATED);
            if(validationIssues == null) return;

            int endIndex = Math.min(defaultRequestValidator.getMaxValidationIssuesToReport(), validationIssues.size());
            for(int idx = 0; idx < endIndex; idx++)
            {
                String issue = validationIssues.get(idx);
                Assert.assertTrue(message.contains(issue), "Expected to find validation issue: " + issue);
            }
            // make sure no truncated items made it in
            for(int idx = endIndex; idx < validationIssues.size(); idx++)
            {
                String issue = validationIssues.get(idx);
                Assert.assertFalse(message.contains(issue), "Unexpected validation issue present (should have been truncated): " + issue);
            }
        }
    }

    private List<String> generateValidationIssues(int count)
    {
        return IntStream.range(0, count).mapToObj(i -> UUID.randomUUID().toString() + String.valueOf(i)).collect(Collectors.toList());
    }
}
