package com.comcast.pop.endpoint.validation;

import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.endpoint.base.validation.DataObjectValidator;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class CustomerValidator extends DataObjectValidator<Customer, DataObjectRequest<Customer>>
{
    private List<String> validationIssues;

    @Override
    public void validatePOST(DataObjectRequest<Customer> request)
    {
        super.validatePOST(request);
        validationIssues = new LinkedList<>();
        Customer customer = request.getDataObject();

        validateTitle(customer);
        processValidationIssues(validationIssues);
    }

    protected void validateTitle(Customer customer)
    {
        if(StringUtils.isBlank(customer.getTitle()))
            validationIssues.add("The title field must be specified on the Customer.");
    }
}
