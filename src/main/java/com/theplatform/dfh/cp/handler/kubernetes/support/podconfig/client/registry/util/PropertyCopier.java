package com.theplatform.dfh.cp.handler.kubernetes.support.podconfig.client.registry.util;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.FluentPropertyBeanIntrospector;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class encompasses the following SO articles:
 * - https://stackoverflow.com/questions/24865923/how-to-merge-two-complex-objects-in-java
 * - https://stackoverflow.com/questions/22743765/beanutils-not-works-for-chain-setter
 *
 * copyProperties() can _deep_ copy an object ignoring nulls AND supports decorator/fluent style setters
 */
public class PropertyCopier {

    /**
     * Copies all properties from sources to destination, does not copy null values and any nested objects will attempted to be
     * either cloned or copied into the existing object. This is recursive. Should not cause any infinite recursion.
     * @param dest object to copy props into (will mutate)
     * @param sources
     * @param <T> dest
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static <T> T copyProperties(T dest, Object... sources) throws IllegalAccessException, InvocationTargetException {
        // to keep from any chance infinite recursion lets limit each object to 1 instance at a time in the stack
        final List<Object> lookingAt = new ArrayList<>();

        BeanUtilsBean recursiveBeanUtils = new BeanUtilsBean() {

            /**
             * Check if the class name is an internal one
             * @param name
             * @return
             */
            private boolean isInternal(String name) {
                return name.startsWith("java.") || name.startsWith("javax.")
                        || name.startsWith("com.sun.") || name.startsWith("javax.")
                        || name.startsWith("oracle.");
            }

            /**
             * Override to ensure that we dont end up in infinite recursion
             * @param dest
             * @param orig
             * @throws IllegalAccessException
             * @throws InvocationTargetException
             */
            @Override
            public void copyProperties(Object dest, Object orig) throws IllegalAccessException, InvocationTargetException {
                this.getPropertyUtils().addBeanIntrospector(new FluentPropertyBeanIntrospector());

                try {
                    // if we have an object in our list, that means we hit some sort of recursion, stop here.
                    if(lookingAt.stream().anyMatch(o->o == dest)) {
                        return; // recursion detected
                    }
                    lookingAt.add(dest);
                    super.copyProperties(dest, orig);
                } finally {
                    lookingAt.remove(dest);
                }
            }

            @Override
            public void copyProperty(Object dest, String name, Object value) throws IllegalAccessException, InvocationTargetException {
                // dont copy over null values
                if (value != null) {
                    // attempt to check if the value is a pojo we can clone using nested calls
                    if(!value.getClass().isPrimitive() && !value.getClass().isSynthetic() && !isInternal(value.getClass().getName())) {
                        try {
                            super.getPropertyUtils().addBeanIntrospector(new FluentPropertyBeanIntrospector());
                            Object prop = super.getPropertyUtils().getProperty(dest, name);
                            // get current value, if its null then clone the value and set that to the value
                            if(prop == null) {
                                super.setProperty(dest, name, super.cloneBean(value));
                            } else {
                                // get the destination value and then recursively call
                                copyProperties(prop, value);
                            }
                        } catch (NoSuchMethodException e) {
                            return;
                        } catch (InstantiationException e) {
                            throw new RuntimeException("Nested property could not be cloned.", e);
                        }
                    } else {
                        super.copyProperty(dest, name, value);
                    }
                }
            }
        };

        for(Object source : sources) {
            recursiveBeanUtils.copyProperties(dest, source);
        }

        return dest;
    }
}
