package com.github.rovkinmax.rxretain;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * @author Rovkin Max
 */
public class RxLifecycleCallback<T> implements Application.ActivityLifecycleCallbacks {
    private RxRetainFragment<T> mRxRetainFragment;

    public RxLifecycleCallback(RxRetainFragment<T> rxRetainFragment) {
        mRxRetainFragment = rxRetainFragment;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {
        mRxRetainFragment.unsubscribeCurrentIfOption();
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}