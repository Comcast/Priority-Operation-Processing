package com.cts.fission.scheduling.queue.algorithm;

import com.comcast.pop.api.facility.Insight;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class FIFOAgendaSchedulerTest extends BaseAgendaSchedulerTest
{
    private FIFOAgendaScheduler scheduler;

    @BeforeMethod
    public void setup()
    {
        insight.setId(UUID.randomUUID().toString());
        mockReadyAgendaPersister = mock(ObjectPersister.class);
        scheduler = new FIFOAgendaScheduler(mockReadyAgendaPersister);
        insight = new Insight();
        insight.setId(UUID.randomUUID().toString());
    }

    @Test
    public void testScheduleNoReadyAgendas() throws Throwable
    {
        doReturn(new DataObjectFeed<ReadyAgenda>()).when(mockReadyAgendaPersister).retrieve(anyList());
        List<ReadyAgenda> readyAgendas = scheduler.schedule(1, insight, insightScheduleInfo);
        Assert.assertNotNull(readyAgendas);
        Assert.assertEquals(readyAgendas.size(), 0);
    }

    @Test
    public void testScheduleOneReadyAgenda() throws Throwable
    {
        List<String> customerIds = IntStream.range(0, 5).mapToObj(i -> Integer.toString(i)).collect(Collectors.toList());
        doReturn(createReadyAgendaFeed(customerIds, 1)).when(mockReadyAgendaPersister).retrieve(anyList());

        List<ReadyAgenda> readyAgendas = scheduler.schedule(1, insight, insightScheduleInfo);
        Assert.assertNotNull(readyAgendas);
        Assert.assertEquals(readyAgendas.size(), 1);
        Assert.assertEquals(readyAgendas.get(0).getCustomerId(), customerIds.get(0));
    }

    @Test
    public void testScheduleExcessReadyAgendas() throws Throwable
    {
        List<String> customerIds = new ArrayList<>();
        List<String> expectedIds = IntStream.range(0, 5).mapToObj(i -> Integer.toString(i)).collect(Collectors.toList());
        customerIds.addAll(expectedIds);
        customerIds.addAll(IntStream.range(5, 10).mapToObj(i -> Integer.toString(i)).collect(Collectors.toList()));
        doReturn(createReadyAgendaFeed(customerIds, 1)).when(mockReadyAgendaPersister).retrieve(anyList());

        List<ReadyAgenda> readyAgendas = scheduler.schedule(5, insight, insightScheduleInfo);
        Assert.assertNotNull(readyAgendas);
        Assert.assertEquals(readyAgendas.size(), 5);
        Assert.assertTrue(
            readyAgendas.stream().map(ReadyAgenda::getCustomerId).collect(Collectors.toList())
                .containsAll(expectedIds)
        );
    }
}
