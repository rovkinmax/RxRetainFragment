package com.github.rovkinmax.rxretain;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subjects.ReplaySubject;

/**
 * @author Rovkin Max
 */
public class RxRetainFragment<T> extends Fragment {
    private ReplaySubject<T> mReplaySubject;
    private Subscription mCurrentSubscription;
    private static final String DEFAULT_TAG = "RX_RETAIN_FRAGMENT_INSTANCE";

    public static <T> RxRetainFragment<T> build(FragmentManager fragmentManager, Observable<T> observable) {
        return build(fragmentManager, observable, DEFAULT_TAG);
    }

    public static <T> RxRetainFragment<T> build(FragmentManager fragmentManager, Observable<T> observable, String tag) {
        RxRetainFragment<T> fragment = (RxRetainFragment<T>) fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new RxRetainFragment<>(observable);
            fragmentManager.beginTransaction().add(fragment, tag).commit();
        }
        return fragment;
    }

    public RxRetainFragment() {
    }

    @SuppressLint("ValidFragment")
    private RxRetainFragment(Observable<T> observable) {
        mReplaySubject = ReplaySubject.create();
        observable.subscribe(mReplaySubject);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getActivity().getApplication().registerActivityLifecycleCallbacks(new RxLifecycleCallback<>(this));
    }

    void unsubscribeCurrentIfOption() {
        if (mCurrentSubscription != null && mCurrentSubscription.isUnsubscribed()) {
            mCurrentSubscription.unsubscribe();
        }
    }


    public Subscription subscribe(Action1<T> nextAction) {
        mCurrentSubscription = mReplaySubject.subscribe(nextAction);
        return mCurrentSubscription;
    }

    public final Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError, final Action0 onComplete) {
        mCurrentSubscription = mReplaySubject.subscribe(onNext, onError, onComplete);
        return mCurrentSubscription;
    }
}
