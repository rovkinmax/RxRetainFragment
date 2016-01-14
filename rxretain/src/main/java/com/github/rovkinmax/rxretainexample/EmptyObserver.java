package com.github.rovkinmax.rxretainexample;

/**
 * @author Rovkin Max
 */
public class EmptyObserver<T> extends RetainObserver<T> {
    @Override
    public void onNext(T t) {

    }
}
