package com.github.rovkinmax.rxretainexample;

import java.lang.ref.WeakReference;

import rx.Observer;
import rx.Subscription;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action1;

/**
 * @author Rovkin Max
 */
public abstract class RetainSubscriber<T> implements Subscription, Observer<T> {

    private WeakReference<Subscription> mRefSubscription;
    private Subscription mSelfSubscription;
    private Action1<Throwable> mOnError;

    public RetainSubscriber() {
        this(null);
    }

    public RetainSubscriber(Subscription subscription) {
        setSubscription(subscription);
    }

    public void setSubscription(Subscription subscription) {
        mRefSubscription = new WeakReference<>(subscription);
    }

    public void setSelfSubscription(Subscription selfSubscription) {
        this.mSelfSubscription = selfSubscription;
    }

    @Override
    public void unsubscribe() {
        if (mSelfSubscription != null) {
            mSelfSubscription.unsubscribe();
        }

        if (mRefSubscription != null && mRefSubscription.get() != null) {
            mRefSubscription.get().unsubscribe();
        }
    }

    @Override
    public boolean isUnsubscribed() {
        return mSelfSubscription == null || mSelfSubscription.isUnsubscribed();

    }

    public Subscription getSubscription() {
        if (mRefSubscription == null) {
            return null;
        }
        return mRefSubscription.get();
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
