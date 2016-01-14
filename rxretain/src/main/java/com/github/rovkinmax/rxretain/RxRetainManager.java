package com.github.rovkinmax.rxretain;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.ReplaySubject;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Rovkin Max
 */
class RxRetainManager<T> {
    private ReplaySubject<T> mReplaySubject;
    private WeakReference<Subscription> mRefReplaySubscription;

    private CompositeSubscription mCurrentSubscriptions = new CompositeSubscription();
    private WeakReference<Observable<T>> mRefObservable;
    private WeakReference<RetainObserver<T>> mRefObserver;


    public RxRetainManager(Observable<T> observable) {
        setObservable(observable);
        setObserver(new EmptyObserver<T>());
    }

    public void setObservable(Observable<T> observable) {
        mRefObservable = new WeakReference<>(observable);
    }


    public void subscribe(Action1<T> onNext) {
        start();
        addCurrentSubscription(mReplaySubject.subscribe(onNext));
    }

    public void subscribe(Action1<T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        start();
        mRefObserver.get().setOnError(onError);
        addCurrentSubscription(mReplaySubject.subscribe(onNext, onError, onCompleted));
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
            setObserver(new EmptyObserver<T>());
        }
    }

    public void setObserver(RetainObserver<T> observer) {
        mRefObserver = new WeakReference<>(observer);
        if (mReplaySubject != null && observer != null) {
            observer.onStarted();
            subscribeObserver();
        }
    }

    private void subscribeObserver() {
        addCurrentSubscription(mReplaySubject.subscribe(getObserver()));
    }

    private void addCurrentSubscription(Subscription subscription) {
        mCurrentSubscriptions.add(subscription);
    }

    public void stop() {
        unsubscribeCurrentIfOption();
        unsubscribeIfOption(getReplaySubscription());
        mRefReplaySubscription = null;
        mReplaySubject = null;
    }

    private Subscription getReplaySubscription() {
        if (mRefReplaySubscription == null) {
            return null;
        }
        return mRefReplaySubscription.get();
    }

    public void unsubscribeCurrentIfOption() {
        unsubscribeIfOption(mCurrentSubscriptions);
        mCurrentSubscriptions.clear();
        mCurrentSubscriptions = new CompositeSubscription();
        if (mRefObserver != null) {
            mRefObserver.clear();
        }
        mRefObserver = null;
    }

    boolean hasObserver() {
        return mRefObserver != null && getObserver() != null;
    }

    private RetainObserver<T> getObserver() {
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
