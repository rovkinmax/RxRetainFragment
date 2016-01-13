package com.github.rovkinmax.rxretainexample;

/**
 * @author Rovkin Max
 */
public class EmptySubscriber<T> extends RetainSubscriber<T> {
    @Override
    public void onNext(T t) {

    }
}
