package com.github.rovkinmax.rxretain;

import rx.Subscriber;

/**
 * @author Rovkin Max
 */
public class EmptySubscriber<T> extends Subscriber<T> {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(T t) {

    }
}
