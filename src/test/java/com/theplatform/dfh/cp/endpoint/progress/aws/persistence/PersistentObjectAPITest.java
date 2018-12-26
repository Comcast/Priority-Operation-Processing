package com.theplatform.dfh.cp.endpoint.progress.aws.persistence;

import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.facility.aws.persistence.PersistentResourcePool;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.PersistentOperationProgress;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            { ResourcePool.class, PersistentResourcePool.class }
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

        Assert.assertEquals(objectMethodNames.size(), persistentObjectMethodNames.size());
        Assert.assertEquals(objectMethodNames, persistentObjectMethodNames);
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