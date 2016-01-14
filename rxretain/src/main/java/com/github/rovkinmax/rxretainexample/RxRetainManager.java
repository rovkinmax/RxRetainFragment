package com.github.rovkinmax.rxretainexample;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.ReplaySubject;

/**
 * @author Rovkin Max
 */
class RxRetainManager<T> {
    private ReplaySubject<T> mReplaySubject;
    private WeakReference<Observable<T>> mRefObservable;
    private WeakReference<RetainSubscriber<T>> mRefObserver;
    private WeakReference<Subscription> mRefReplaySubscription;

    public RxRetainManager(Observable<T> observable) {
        setObservable(observable);
        setObserver(new EmptySubscriber<T>());
    }

    public void setObservable(Observable<T> observable) {
        mRefObservable = new WeakReference<>(observable);
    }


    public void subscribe(Action1<T> onNext) {
        start();
        mRefObserver.get().setSubscription(mReplaySubject.subscribe(onNext));
    }

    public void subscribe(Action1<T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        start();
        mRefObserver.get().setOnError(onError);
        mRefObserver.get().setSubscription(mReplaySubject.subscribe(onNext, onError, onCompleted));
    }

    public void start() {
        initObserverIfNull();
        if (mReplaySubject == null) {
            mReplaySubject = ReplaySubject.create();
            subscribeObserver();
            if (hasObservable()) {
                mRefReplaySubscription = new WeakReference<>(getObservable().subscribe(mReplaySubject));
            } else {
                throw new RuntimeException("Can't run. First you must create RxRetainFragment with not null observer");
            }
        }
    }


    private void initObserverIfNull() {
        if (!hasObserver()) {
            setObserver(new EmptySubscriber<T>());
        }
    }

    public void setObserver(RetainSubscriber<T> observer) {
        mRefObserver = new WeakReference<>(observer);
        if (mReplaySubject != null && observer != null) {
            observer.onStarted();
            subscribeObserver();
        }
    }

    private void subscribeObserver() {
        getObserver().setSelfSubscription(mReplaySubject.subscribe(getObserver()));
    }

    public void stop() {
        unsubscribeCurrentIfOption();

        if (mRefReplaySubscription != null) {
            unsubscribeIfOption(mRefReplaySubscription.get());
        }
        mReplaySubject = null;
    }

    public void unsubscribeCurrentIfOption() {
        if (hasObserver()) {
            unsubscribeIfOption(mRefObserver.get());
            unsubscribeIfOption(mRefObserver.get().getSubscription());
            mRefObserver.clear();
            mRefObserver = null;
        }
    }

    boolean hasObserver() {
        return mRefObserver != null && getObserver() != null;
    }

    private RetainSubscriber<T> getObserver() {
        return mRefObserver.get();
    }

    boolean hasObservable() {
        return mRefObservable != null && getObservable() != null;
    }

    private Observable<T> getObservable() {
        return mRefObservable.get();
    }

    private void unsubscribeIfOption(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
