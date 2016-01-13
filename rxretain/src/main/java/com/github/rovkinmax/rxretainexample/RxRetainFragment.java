package com.github.rovkinmax.rxretainexample;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.ReplaySubject;

/**
 * @author Rovkin Max
 */
public class RxRetainFragment<T> extends Fragment {
    private static final String DEFAULT_TAG = "RX_RETAIN_FRAGMENT_INSTANCE";

    private ReplaySubject<T> mReplaySubject;
    private WeakReference<Observable<T>> mObservable;
    private WeakReference<Subscription> mCurrentSubscription;
    private WeakReference<Subscription> mReplaySubscription;
    private RxLifecycleCallback<T> mRxLifecycleCallback = new RxLifecycleCallback<>(this);

    public static <T> RxRetainFragment<T> create(FragmentManager fragmentManager, Observable<T> observable) {
        return create(fragmentManager, observable, DEFAULT_TAG);
    }

    public static <T> RxRetainFragment<T> create(FragmentManager fragmentManager, Observable<T> observable, String tag) {
        RxRetainFragment<T> fragment = getFragmentByTag(fragmentManager, tag);
        if (fragment == null) {
            fragment = new RxRetainFragment<>(observable);
            fragmentManager.beginTransaction().add(fragment, tag).commit();
        }
        return fragment;
    }

    public static <T> RxRetainFragment<T> start(FragmentManager fragmentManager, Observable<T> observable) {
        return start(fragmentManager, observable, DEFAULT_TAG);
    }

    public static <T> RxRetainFragment<T> start(FragmentManager fragmentManager, Observable<T> observable, String tag) {
        RxRetainFragment<T> fragment = getFragmentByTag(fragmentManager, tag);
        if (fragment == null) {
            fragment = new RxRetainFragment<>(observable);
            fragmentManager.beginTransaction().add(fragment, tag).commit();
            fragment.initEmittingAndStart();
        }
        return fragment;
    }

    public static <T> RxRetainFragment<T> restart(FragmentManager fragmentManager, Observable<T> observable) {
        return restart(fragmentManager, observable, DEFAULT_TAG);
    }

    public static <T> RxRetainFragment<T> restart(FragmentManager fragmentManager, Observable<T> observable, String tag) {
        RxRetainFragment<T> fragment = getFragmentByTag(fragmentManager, tag);
        if (fragment == null) {
            fragment = new RxRetainFragment<>(observable);
            fragmentManager.beginTransaction().add(fragment, tag).commit();

        } else {
            fragment.clearCurrentObservableIfOption();
            fragment.setCurrentObservable(observable);
        }
        fragment.initEmittingAndStart();
        return fragment;
    }

    public static void stopExecution(FragmentManager fragmentManager) {
        stopExecution(fragmentManager, DEFAULT_TAG);
    }

    public static void stopExecution(FragmentManager fragmentManager, String tag) {
        RxRetainFragment fragment = getFragmentByTag(fragmentManager, tag);
        if (fragment != null) {
            fragment.cancelExecution();
        }
    }

    private static <T> RxRetainFragment<T> getFragmentByTag(FragmentManager fragmentManager, String tag) {
        return (RxRetainFragment<T>) fragmentManager.findFragmentByTag(tag);
    }

    public RxRetainFragment() {
    }

    @SuppressLint("ValidFragment")
    private RxRetainFragment(Observable<T> observable) {
        setCurrentObservable(observable);
    }


    private void setCurrentObservable(Observable<T> observable) {
        mObservable = new WeakReference<>(observable);
    }

    private void clearCurrentObservableIfOption() {
        if (mObservable != null) {
            mObservable.clear();
            mObservable = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (isAdded()) {
            getActivity().getApplication().registerActivityLifecycleCallbacks(mRxLifecycleCallback);
        }
    }

    @Override
    public void onStop() {
        if (isAdded()) {
            getActivity().getApplication().unregisterActivityLifecycleCallbacks(mRxLifecycleCallback);
        }
        super.onStop();
    }

    void unsubscribeCurrentIfOption() {
        if (mCurrentSubscription != null) {
            unsubscribeIfOption(mCurrentSubscription.get());
            mCurrentSubscription.clear();
        }
    }

    private void cancelExecution() {
        if (mReplaySubscription != null) {
            unsubscribeIfOption(mReplaySubscription.get());
            mReplaySubscription.clear();
        }
    }

    public Subscription subscribe(Action1<T> nextAction) {
        if (mReplaySubject == null) {
            initEmittingAndStart();
        }
        return setCurrentSubscription(mReplaySubject.subscribe(nextAction));
    }

    public Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError, final Action0 onComplete) {
        if (mReplaySubject == null) {
            initEmittingAndStart();
        }
        return setCurrentSubscription(mReplaySubject.subscribe(onNext, onError, onComplete));
    }

    public final Subscription subscribe(Subscriber<? super T> subscriber) {
        if (mReplaySubject == null) {
            initEmittingAndStart();
        }
        return setCurrentSubscription(mReplaySubject.subscribe(subscriber));
    }

    private Subscription setCurrentSubscription(Subscription subscription) {
        if (mReplaySubject == null) {
            initEmittingAndStart();
        }
        mCurrentSubscription = new WeakReference<>(subscription);
        return mCurrentSubscription.get();
    }

    private void initEmittingAndStart() {
        if (mReplaySubscription != null) {
            unsubscribeIfOption(mReplaySubscription.get());
        }
        if (mObservable != null && mObservable.get() != null) {
            startEmitting(mObservable.get());
        } else {
            throw new RuntimeException("Can't run. First you must create RxRetainFragment with not null observer");
        }
    }

    private void startEmitting(Observable<T> observable) {
        mReplaySubject = ReplaySubject.create();
        mReplaySubscription = new WeakReference<>(observable.subscribe(mReplaySubject));
    }

    private void unsubscribeIfOption(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
