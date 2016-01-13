package com.github.rovkinmax.rxretainexample;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import java.lang.ref.WeakReference;

import rx.Observable;
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
    private WeakReference<Subscription> mCurrentSubscription;
    private WeakReference<Subscription> mReplaySubscription;
    private RxLifecycleCallback<T> mRxLifecycleCallback = new RxLifecycleCallback<>(this);

    public static <T> RxRetainFragment<T> start(FragmentManager fragmentManager, Observable<T> observable) {
        return start(fragmentManager, observable, DEFAULT_TAG);
    }

    public static <T> RxRetainFragment<T> start(FragmentManager fragmentManager, Observable<T> observable, String tag) {
        RxRetainFragment<T> fragment = getFragmentByTag(fragmentManager, tag);
        if (fragment == null) {
            fragment = new RxRetainFragment<>(observable);
            fragmentManager.beginTransaction().add(fragment, tag).commit();
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
            fragment.initEmitting(observable);
        }
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
        initEmitting(observable);
    }

    private void initEmitting(Observable<T> observable) {
        if (mReplaySubscription != null) {
            unsubscribeIfOption(mReplaySubscription.get());
        }
        mReplaySubject = ReplaySubject.create();
        mReplaySubscription = new WeakReference<>(observable.subscribe(mReplaySubject));
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
        mCurrentSubscription = new WeakReference<>(mReplaySubject.subscribe(nextAction));
        return mCurrentSubscription.get();
    }

    public final Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError, final Action0 onComplete) {
        mCurrentSubscription = new WeakReference<>(mReplaySubject.subscribe(onNext, onError, onComplete));
        return mCurrentSubscription.get();
    }

    private void unsubscribeIfOption(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
