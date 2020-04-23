package com.comcast.pop.modules.kube.fabric8.client.logging;

import com.comcast.pop.modules.kube.client.logging.LogLineObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class LogLineObserverImpl implements LogLineObserver
{
    private static Logger logger = LoggerFactory.getLogger(LogLineObserverImpl.class);

    private Subject<String, String> lineSubject = PublishSubject.create();
    private Subject<Collection<String>, Collection<String>> multiLineSubject = PublishSubject.create();

    private String podName;

    public LogLineObserverImpl(Consumer<String> consumer, Consumer<Collection<String>> multiLineConsumer)
    {
        lineSubject.subscribe(wrapConsumerInSubscriber(consumer));
        multiLineSubject.subscribe(wrapMultiLineConsumerInSubscriber(multiLineConsumer));
    }

    protected Subscriber<String> wrapConsumerInSubscriber(final Consumer<String> consumer)
    {
        return new Subscriber<String>()
        {
            @Override
            public void onNext(String s)
            {
                consumer.accept(s);
            }

            @Override
            public void onCompleted()
            {
                logger.debug("onCompleted called from single-line subscriber.");
                this.unsubscribe();
            }

            @Override
            public void onError(Throwable e)
            {
                logger.error("Error", e);
            }
        };
    }

    protected Subscriber<Collection<String>> wrapMultiLineConsumerInSubscriber(final Consumer<Collection<String>> consumer)
    {
        return new Subscriber<Collection<String>>()
        {
            @Override
            public void onNext(Collection<String> s)
            {
                consumer.accept(s);
            }

            @Override
            public void onCompleted()
            {
                logger.debug("onCompleted called from multi-line subscriber.");
                this.unsubscribe();
            }

            @Override
            public void onError(Throwable e)
            {
                logger.error("Error", e);
            }
        };
    }

    public LogLineObserverImpl(Subscriber<String> subscriber)
    {
        lineSubject.subscribe(subscriber);
    }

    public void addSubscriber(Subscriber<String> subscriber)
    {
        lineSubject.subscribe(subscriber);
    }

    @Override
    public void addConsumer(Consumer<String> consumer)
    {
        lineSubject.subscribe(wrapConsumerInSubscriber(consumer));
    }

    @Override
    public LogLineObserver setPodName(String podName)
    {
        this.podName = podName;
        return this;
    }

    @Override
    public void send(String s)
    {
        lineSubject.onNext(s);
        multiLineSubject.onNext(Collections.singletonList(s));
    }

    @Override
    public void done()
    {
        lineSubject.onCompleted();
        multiLineSubject.onCompleted();
    }

    @Override
    public void addAggregateConsumer(Consumer<Collection<String>> consumer)
    {
        multiLineSubject.subscribe(wrapMultiLineConsumerInSubscriber(consumer));
    }

    @Override
    public void send(Collection<String> lines)
    {
        multiLineSubject.onNext(lines);
        lines.forEach(lineSubject::onNext);
    }
}
