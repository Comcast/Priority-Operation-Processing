package com.theplatform.dfh.cp.endpoint.base.validation;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataObjectValidatorTest
{
    private DataObjectValidator<Agenda, DefaultDataObjectRequest<Agenda>> validator;

    @BeforeMethod
    public void setup()
    {
        validator = new DataObjectValidator<>();
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Unable to POST a null object")
    public void testPOSTValidationNullObject()
    {
        DefaultDataObjectRequest<Agenda> request = createAgendaRequest(null);
        validator.validatePOST(request);
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Unable to PUT a null object")
    public void testPUTValidationNullObject()
    {
        DefaultDataObjectRequest<Agenda> request = createAgendaRequest(null);
        validator.validatePUT(request);
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Unable to PUT an object without specifying an id")
    public void testPUTValidationNullId()
    {
        DefaultDataObjectRequest<Agenda> request = createAgendaRequest(null);
        request.setDataObject(new Agenda());
        validator.validatePUT(request);
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "Mismatched Id in URL and on input object: URLId: theURLId and ObjectId: theAgendaId")
    public void testPUTValidationMismatchedId()
    {
        final String URL_ID = "theURLId";
        final String AGENDA_ID = "theAgendaId";
        DefaultDataObjectRequest<Agenda> request = createAgendaRequest(URL_ID);
        Agenda agenda = new Agenda();
        agenda.setId(AGENDA_ID);
        request.setDataObject(agenda);
        validator.validatePUT(request);
    }

    @Test
    public void testPUTValidationRequestIdOnly()
    {
        final String URL_ID = "theURLId";
        DefaultDataObjectRequest<Agenda> request = createAgendaRequest(URL_ID);
        request.setDataObject(new Agenda());
        validator.validatePUT(request);
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*The customerId field must be specified.*")
    public void testInvalidCustomer()
    {
        // This is a unified validator for all object types
        DefaultDataObjectRequest<Agenda> request = createAgendaRequest("");
        request.setDataObject(new Agenda());
        validator.validatePOST(request);
    }

    private DefaultDataObjectRequest<Agenda> createAgendaRequest(String id)
    {
        DefaultDataObjectRequest<Agenda> request = new DefaultDataObjectRequest<>();
        request.setId(id);
        return request;
    }
}
