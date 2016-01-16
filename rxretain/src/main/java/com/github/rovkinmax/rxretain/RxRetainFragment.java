package com.github.rovkinmax.rxretain;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;

import rx.Observable;
import rx.Subscriber;
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
        createNewManager(observable);
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

    public void subscribe() {
        mManager.subscribe(new EmptyObserver<T>());
    }

    public void subscribe(Subscriber<T> subscriber) {
        mManager.subscribe(subscriber);
    }

    RxRetainManager<T> getManager() {
        return mManager;
    }

    public void unsubscribe() {
        mManager.stop();
    }

    private void createNewManager(Observable<T> observable) {
        mManager = new RxRetainManager<>(observable);
        mRxLifecycleCallback.setManager(mManager);
    }
}