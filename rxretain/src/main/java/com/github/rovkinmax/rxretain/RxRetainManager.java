package com.github.rovkinmax.rxretain;

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
    private Subscription mRefReplaySubscription;

    private CompositeSubscription mCurrentSubscriptions = new CompositeSubscription();
    private Observable<T> mRefObservable;
    private Subscriber<? super T> mRefSubscriber;


    RxRetainManager(Observable<T> observable) {
        setObservable(observable);
    }

    void setObservable(Observable<T> observable) {
        mRefObservable = observable;
    }

    void subscribe(Action1<T> onNext) {
        start();
        addCurrentSubscription(mReplaySubject.subscribe(onNext));
    }

    void subscribe(Action1<T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        start();
        addCurrentSubscription(mReplaySubject.subscribe(onNext, onError, onCompleted));
    }

    void subscribe(final Subscriber<? super T> subscriber) {
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
                mRefReplaySubscription = getObservable().subscribe(mReplaySubject);
            } else {
                throw new RuntimeException("Can't run. First you must create RetainWrapper with not null observer");
            }
            subscribeObserver();
        }
    }

    void setSubscriber(Subscriber<? super T> observer) {
        if (mRefSubscriber != null && mRefSubscriber != observer) {
            return;
        }
        mRefSubscriber = observer;
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
        return mRefReplaySubscription;
    }

    void unsubscribeCurrentIfOption() {
        unsubscribeIfOption(mCurrentSubscriptions);
        mCurrentSubscriptions.clear();
        mCurrentSubscriptions = new CompositeSubscription();
        mRefSubscriber = null;
    }

    boolean hasSubscriber() {
        return mRefSubscriber != null;
    }

    private Subscriber<? super T> getObserver() {
        return mRefSubscriber;
    }

    boolean hasObservable() {
        return mRefObservable != null;
    }

    private Observable<T> getObservable() {
        return mRefObservable;
    }

    private void unsubscribeIfOption(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
