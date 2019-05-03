package com.theplatform.dfh.cp.modules.kube.fabric8.client.logging;

import com.theplatform.dfh.cp.modules.kube.client.LogLineAccumulator;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class K8LogReaderTest
{
    @Test
    public void testIOClose() throws Exception
    {

        K8LogReader k8LogReader = new K8LogReader("podNameFoo", logLineAccumulator, null);
        LogWatch logWatch = mock(LogWatch.class);

        ByteArrayOutputStream originalByteArrayOutputStream = new ByteArrayOutputStream(1000);
        PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);

        when(logWatch.getOutput()).thenReturn(in);
        k8LogReader.observeRuntimeLog(logWatch);


        originalByteArrayOutputStream.write("Hey!\n".getBytes());

        new Thread(new Runnable() {
            public void run () {
                try {

                    originalByteArrayOutputStream.writeTo(out);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        originalByteArrayOutputStream.writeTo(out);

        Thread.sleep(1000);
    }


    LogLineAccumulator logLineAccumulator = new LogLineAccumulator()
    {
        @Override
        public void appendLine(String s)
        {
            System.out.println(new Date().toString() + " " + s);
        }

        @Override
        public List<String> takeAll()
        {
            return null;
        }

        @Override
        public boolean isAllLogDataRequired()
        {
            return false;
        }

        @Override
        public void setCompletion(Runnable runnable)
        {

        }

        @Override
        public void forceCompletion()
        {

        }

        @Override
        public void setCompletionIdentifier(String s)
        {

        }
    };
}
