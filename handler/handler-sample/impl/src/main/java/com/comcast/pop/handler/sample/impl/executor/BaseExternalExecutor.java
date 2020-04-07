package com.comcast.pop.handler.sample.impl.executor;

import com.comast.pop.handler.base.perform.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class BaseExternalExecutor implements Executor<List<String>>
{
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public BaseExternalExecutor()
    {

    }
}