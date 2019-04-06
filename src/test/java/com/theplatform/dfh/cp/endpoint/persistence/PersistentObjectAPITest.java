package com.theplatform.dfh.cp.endpoint.persistence;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.PersistentAgenda;
import com.theplatform.dfh.cp.endpoint.facility.aws.persistence.PersistentCustomer;
import com.theplatform.dfh.cp.endpoint.facility.aws.persistence.PersistentInsight;
import com.theplatform.dfh.cp.endpoint.facility.aws.persistence.PersistentResourcePool;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.PersistentOperationProgress;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.PersistentAgendaProgress;
import com.theplatform.dfh.cp.endpoint.transformrequest.aws.persistence.PersistentTransform;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 */
public class PersistentObjectAPITest
{
    public List<String> ignoreGetMethods = Arrays.asList("getClass");

    @DataProvider
    Object[][] getAPIClasses()
    {
        return new Object[][] {
            { AgendaProgress.class, PersistentAgendaProgress.class },
            { OperationProgress.class, PersistentOperationProgress.class },
            { ResourcePool.class, PersistentResourcePool.class },
            { Customer.class, PersistentCustomer.class },
            { Insight.class, PersistentInsight.class },
            { Agenda.class, PersistentAgenda.class },
            { TransformRequest.class, PersistentTransform.class }
        };
    }

    @Test(dataProvider = "getAPIClasses")
    public void testAllGettersOverridden(Class dataObjectClass, Class persistentObjectClass)
    {
        List<Method> dataObjectMethods = Arrays.asList(dataObjectClass.getMethods());
        dataObjectMethods = getGetters(dataObjectMethods);
        List<String> objectMethodNames = getMethodNames(dataObjectMethods);

        List<Method> persistentObjectMethods = new ArrayList<>(Arrays.asList(persistentObjectClass.getMethods()));
        // only keep overridden methods
        persistentObjectMethods.removeIf(m -> !isMethodOverridden(m));
        persistentObjectMethods = getGetters(persistentObjectMethods);
        List<String> persistentObjectMethodNames = getMethodNames(persistentObjectMethods);

        Collections.sort(objectMethodNames);
        Collections.sort(persistentObjectMethodNames);
        if(objectMethodNames.size() != persistentObjectMethodNames.size())
        {
            // make an easy to debug assert
            Assert.assertEquals(String.join(",", objectMethodNames), String.join(",", persistentObjectMethodNames));
        }
        for(int idx = 0; idx < objectMethodNames.size(); idx++)
        {
            Assert.assertEquals(objectMethodNames.get(idx), persistentObjectMethodNames.get(idx));
        }
    }

    public List<Method> getGetters(List<Method> allMethods)
    {
        List<Method> getters = new ArrayList<>();

        for (Method m : allMethods)
        {
            if (m.getName().startsWith("get") && !ignoreGetMethods.contains(m.getName()))
            {
                getters.add(m);
            }
        }

        return getters;
    }

    public static boolean isMethodOverridden(Method myMethod) {
        Class<?> declaringClass = myMethod.getDeclaringClass();
        if (declaringClass.equals(Object.class)) {
            return false;
        }
        try {
            declaringClass.getSuperclass().getMethod(myMethod.getName(), myMethod.getParameterTypes());
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public List<String> getMethodNames(List<Method> methods)
    {
        return methods.stream().map(Method::getName).collect(Collectors.toList());
    }
}