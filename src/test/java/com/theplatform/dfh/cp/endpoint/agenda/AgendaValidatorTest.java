package com.theplatform.dfh.cp.endpoint.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonReferenceReplacer;
import com.theplatform.dfh.endpoint.api.ValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgendaValidatorTest
{
    private final String CUSTOMER_ID = "theCustomer";
    private JsonReferenceReplacer jsonReferenceReplacer = new JsonReferenceReplacer();
    private AgendaValidator validator;

    @BeforeMethod
    public void setup()
    {
        validator = new AgendaValidator();
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*The customer id must be specified on the agenda.*")
    public void testInvalidCustomer()
    {
        validator.validate(createAgenda(null));
    }

    @DataProvider
    public Object[][] invalidOperationsProvider()
    {
        return new Object[][]
            {
                { createAgenda(CUSTOMER_ID) },
                { createAgenda(CUSTOMER_ID, new ArrayList<>())}
            };
    }

    @Test(dataProvider = "invalidOperationsProvider", expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*No operations specified in Agenda.*")
    public void testInvalidOperations(Agenda agenda)
    {
        validator.validate(agenda);
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Operation names must be unique.*")
    public void testDuplicateOperationNames()
    {
        Operation op1 = new Operation();
        op1.setName("foo");
        Operation op2 = new Operation();
        op2.setName("FOo");
        List<Operation> operations = new ArrayList<>();
        operations.add(op1);
        operations.add(op2);

        validator.verifyUniqueOperationsName(operations);
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Operations must have names.*")
    public void testMissingOperationNames()
    {
        Operation op1 = new Operation();
        op1.setName("");

        validator.verifyUniqueOperationsName(Collections.singletonList(op1));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Invalid references found in operation.*")
    public void testInvalidReferences()
    {
        Agenda agenda = createAgenda(CUSTOMER_ID);
        Operation op1 = new Operation();
        op1.setName("Op1");
        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("arg", jsonReferenceReplacer.generateReference("Op2.out", "/"));
        op1.setPayload(payloadMap);
        agenda.setOperations(Collections.singletonList(op1));

        validator.validate(agenda);
    }

    @Test
    public void testValidReference()
    {
        Agenda agenda = createAgenda(CUSTOMER_ID);

        Operation op1 = new Operation();
        op1.setName("Op1");
        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("arg", jsonReferenceReplacer.generateReference("Op2.out", "/"));
        op1.setPayload(payloadMap);

        Operation op2 = new Operation();
        op2.setName("Op2");
        op2.setPayload(new HashMap<>());

        agenda.setOperations(Arrays.asList(op1, op2));

        validator.validate(agenda);
    }

    private Agenda createAgenda(String customerId)
    {
        Agenda agenda = new Agenda().setCustomerId(customerId);
        return agenda;
    }

    private Agenda createAgenda(String customerId, List<Operation> operations)
    {
        Agenda agenda = createAgenda(customerId);
        agenda.setOperations(operations);
        return agenda;
    }
}
