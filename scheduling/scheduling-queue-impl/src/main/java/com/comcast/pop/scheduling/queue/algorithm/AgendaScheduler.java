package com.comcast.pop.scheduling.queue.algorithm;

import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.scheduling.queue.InsightScheduleInfo;

import java.util.List;

public interface AgendaScheduler
{
    /**
     * Returns the ReadyAgenda objects to be scheduled.
     * @param requestedCount The number of ReadyAgenda objects requested
     * @param insight The insight to schedule with
     * @param insightScheduleInfo The queue information for the insight
     * @return A list of ReadyAgenda object or null
     */
    List<ReadyAgenda> schedule(int requestedCount, Insight insight, InsightScheduleInfo insightScheduleInfo);
}
