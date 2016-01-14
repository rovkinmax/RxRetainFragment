package com.github.rovkinmax.rxretain;

/**
 * @author Rovkin Max
 */
public class EmptyObserver<T> extends RetainObserver<T> {
    @Override
    public void onNext(T t) {

    }
}
