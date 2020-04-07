package com.comcast.pop.handler.reaper.impl;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class ReaperTestUtil
{
    public static List<Pod> createPodList(int count)
    {
        List<Pod> pods = new LinkedList<>();
        IntStream.range(0, count).forEach(i ->
        {
            Pod pod = new Pod();
            ObjectMeta objectMeta = new ObjectMeta();
            objectMeta.setName(Integer.toString(i));
            pod.setMetadata(objectMeta);
            pods.add(pod);
        });
        return pods;
    }
}
