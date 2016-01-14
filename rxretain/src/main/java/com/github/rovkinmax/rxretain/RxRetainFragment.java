package com.github.rovkinmax.rxretain;

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
    private RxLifecycleCallback<T> mRxLifecycleCallback = new RxLifecycleCallback<>();

    public RxRetainFragment() {
    }

    @SuppressLint("ValidFragment")
    RxRetainFragment(Observable<T> observable) {
        mManager = new RxRetainManager<>(observable);
        mRxLifecycleCallback.setManager(mManager);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (isAdded()) {
            getActivity().getApplication().unregisterActivityLifecycleCallbacks(mRxLifecycleCallback);
            getActivity().getApplication().registerActivityLifecycleCallbacks(mRxLifecycleCallback);
        }
    }

    @Override
    public void onStop() {
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

    RxRetainManager<T> getManager() {
        return mManager;
    }
}