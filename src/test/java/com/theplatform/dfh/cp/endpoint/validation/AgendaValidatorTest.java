package com.theplatform.dfh.cp.endpoint.validation;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.tokens.AgendaToken;
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
import java.util.UUID;

public class AgendaValidatorTest extends BaseValidatorTest<Agenda>
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
        validator.validatePOST(createRequest(createAgenda(null)));
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
        validator.validatePOST(createRequest(agenda));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Operation names must be unique.*")
    public void testDuplicateOperationNames()
    {
        validator.verifyUniqueOperationsName(Arrays.asList(
            createOperation("foo"),
            createOperation("FOo")
        ));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Operations must have names.*")
    public void testMissingOperationNames()
    {
        validator.verifyUniqueOperationsName(Collections.singletonList(createOperation("")));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Invalid references found in operation.*")
    public void testInvalidReferences()
    {
        Agenda agenda = createAgenda(CUSTOMER_ID);
        agenda.setOperations(Collections.singletonList(createOperation("Op1", "Op2.out")));

        validator.validatePOST(createRequest(agenda));
    }

    @Test
    public void testFissionReference()
    {
        Agenda agenda = createAgenda(CUSTOMER_ID);
        agenda.setOperations(Collections.singletonList(createOperation("Op1", AgendaToken.AGENDA_ID.getToken())));

        validator.validatePOST(createRequest(agenda));
    }

    @Test
    public void testValidReference()
    {
        Agenda agenda = createAgenda(CUSTOMER_ID);
        agenda.setOperations(Arrays.asList(
            createOperation("Op1", "Op2.out"),
            createOperation("Op2")
        ));

        validator.validatePOST(createRequest(agenda));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*There is a circular reference.*")
    public void testCircularReferenceSimple()
    {
        Agenda agenda = createAgenda(CUSTOMER_ID);
        agenda.setOperations(Collections.singletonList(createOperation("Op1", "Op1.out")));

        validator.validatePOST(createRequest(agenda));
    }

    @DataProvider
    public Object[][] circularReferenceProvider()
    {
        return new Object[][]
            {
                {
                    // direct loop
                    Arrays.asList(
                        createOperation("Op1", "Op2.out"),
                        createOperation("Op2", "Op3.out"),
                        createOperation("Op3", "Op4.out"),
                        createOperation("Op4", "Op1.out")
                    )
                },
                {
                    // indirect loop
                    Arrays.asList(
                        createOperation("Op1", "Op2.out", "Op3.out", "Op4.out"),
                        createOperation("Op2", "Op3.out"),
                        createOperation("Op3", "Op4.out"),
                        createOperation("Op4", "Op2.out")
                    )
                }
            };
    }

    @Test(dataProvider = "circularReferenceProvider", expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*There is a circular reference.*")
    public void testCircularReference(List<Operation> operations)
    {
        Agenda agenda = createAgenda(CUSTOMER_ID);
        agenda.setOperations(operations);
        validator.validatePOST(createRequest(agenda));
    }

    private Operation createOperation(String name)
    {
        Operation operation = new Operation();
        operation.setName(name);
        operation.setPayload(new HashMap<>());
        return operation;
    }

    private Operation createOperation(String name, String... references)
    {
        Operation operation = createOperation(name);
        Map<String, String> payloadMap = new HashMap<>();
        Arrays.stream(references).forEach(ref ->
            payloadMap.put(UUID.randomUUID().toString(), jsonReferenceReplacer.generateReference(ref, "/"))
        );
        operation.setPayload(payloadMap);
        return operation;
    }

    private Agenda createAgenda(String customerId)
    {
        Agenda agenda = new Agenda();
        agenda.setCustomerId(customerId);
        return agenda;
    }

    private Agenda createAgenda(String customerId, List<Operation> operations)
    {
        Agenda agenda = createAgenda(customerId);
        agenda.setOperations(operations);
        return agenda;
    }
}
