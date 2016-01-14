package com.github.rovkinmax.rxretainexample;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * @author Rovkin Max
 */
public class RxRetainFragment<T> extends Fragment {

    private RxRetainManager<T> mManager;
    private RxLifecycleCallback<T> mRxLifecycleCallback = new RxLifecycleCallback<>(this);

    public RxRetainFragment() {
    }

    @SuppressLint("ValidFragment")
    RxRetainFragment(Observable<T> observable) {
        mManager = new RxRetainManager<>(observable);
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

    public void subscribe(Action1<T> onNext) {
        mManager.subscribe(onNext);
    }

    public void subscribe(Action1<T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        mManager.subscribe(onNext, onError, onCompleted);
    }

    public void start() {
        mManager.start();
    }

    void setObserver(RetainSubscriber<T> observer) {
        mManager.setObserver(observer);
    }

    void setObservable(Observable<T> observable) {
        mManager.setObservable(observable);
    }

    void stop() {
        mManager.stop();
    }

    void unsubscribeCurrentIfOption() {
        mManager.unsubscribeCurrentIfOption();
    }

    boolean hasObservable() {
        return mManager.hasObservable();
    }
}