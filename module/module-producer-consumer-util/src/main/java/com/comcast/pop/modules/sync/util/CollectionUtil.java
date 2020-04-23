package com.comcast.pop.modules.sync.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CollectionUtil
{
    /**
     * Splits a collection into parts with a specified size.
     * @param collection The collection to split
     * @param subsetSize The size of each subset to create
     * @param <T> The type of objects in the collection
     * @return The split version of the collection as a number of subsets, or null if the incoming collection is empty or the subset size is less than 1
     */
    public static <T> Collection<List<T>> split(Collection<T> collection, final int subsetSize)
    {
        if(collection == null || subsetSize < 1)
            return null;

        AtomicInteger subsetCounter = new AtomicInteger();
        return collection.stream().collect(Collectors.groupingBy(it -> subsetCounter.getAndIncrement() / subsetSize)).values();
    }
}
