package com.github.rovkinmax.rxretainexample;

import rx.Observer;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action1;

/**
 * @author Rovkin Max
 */
public abstract class RetainObserver<T> implements Observer<T> {

    private Action1<Throwable> mOnError;

    public RetainObserver() {
    }


    public void onStarted() {
    }

    @Override
    public void onCompleted() {
    }

    public void setOnError(Action1<Throwable> onError) {
        mOnError = onError;
    }

    @Override
    public void onError(Throwable e) {
        if (mOnError == null) {
            throw new OnErrorNotImplementedException(e);
        }
    }

    @Override
    public abstract void onNext(T t);
}
