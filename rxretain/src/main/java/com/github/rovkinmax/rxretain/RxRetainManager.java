package com.github.rovkinmax.rxretain;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
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
    private WeakReference<Subscriber<T>> mRefSubscriber;


    RxRetainManager(Observable<T> observable) {
        setObservable(observable);
    }

    void setObservable(Observable<T> observable) {
        mRefObservable = new WeakReference<>(observable);
    }

    void subscribe(Action1<T> onNext) {
        start();
        addCurrentSubscription(mReplaySubject.subscribe(onNext));
    }

    void subscribe(Action1<T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        start();
        addCurrentSubscription(mReplaySubject.subscribe(onNext, onError, onCompleted));
    }

    void subscribe(final Subscriber<T> subscriber) {
        start();
        addCurrentSubscription(mReplaySubject.subscribe(subscriber));
    }

    void subscribe(final Observer<T> observer) {
        start();
        addCurrentSubscription(mReplaySubject.subscribe(observer));
    }

    void start() {
        if (mReplaySubject == null) {
            mReplaySubject = ReplaySubject.create();
            if (hasObservable()) {
                mRefReplaySubscription = new WeakReference<>(getObservable().subscribe(mReplaySubject));
            } else {
                throw new RuntimeException("Can't run. First you must create RxRetainFragment with not null observer");
            }
            subscribeObserver();
        }
    }

    void setSubscriber(Subscriber<T> observer) {
        if (mRefSubscriber != null && mRefSubscriber.get() != observer) {
            return;
        }
        mRefSubscriber = new WeakReference<>(observer);
        if (mReplaySubject != null && observer != null) {
            subscribeObserver();
        }
    }

    private void subscribeObserver() {
        if (hasSubscriber()) {
            addCurrentSubscription(mReplaySubject.subscribe(getObserver()));
        }
    }

    private void addCurrentSubscription(Subscription subscription) {
        mCurrentSubscriptions.add(subscription);
    }

    void stop() {
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

    void unsubscribeCurrentIfOption() {
        unsubscribeIfOption(mCurrentSubscriptions);
        mCurrentSubscriptions.clear();
        mCurrentSubscriptions = new CompositeSubscription();
        if (mRefSubscriber != null) {
            mRefSubscriber.clear();
        }
        mRefSubscriber = null;
    }

    boolean hasSubscriber() {
        return mRefSubscriber != null && getObserver() != null;
    }

    private Subscriber<T> getObserver() {
        return mRefSubscriber.get();
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
