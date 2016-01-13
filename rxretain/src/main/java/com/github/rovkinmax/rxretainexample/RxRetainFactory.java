package com.github.rovkinmax.rxretainexample;

import android.app.FragmentManager;

import rx.Observable;

/**
 * @author Rovkin Max
 */
public final class RxRetainFactory {
    private static final String DEFAULT_TAG = "RX_RETAIN_FRAGMENT_INSTANCE";

    public static <T> RxRetainFragment<T> create(FragmentManager fragmentManager, Observable<T> observable) {
        return create(fragmentManager, observable, new EmptySubscriber<T>());
    }

    public static <T> RxRetainFragment<T> create(FragmentManager fragmentManager, Observable<T> observable, RetainSubscriber<T> observer) {
        return create(fragmentManager, observable, observer, DEFAULT_TAG);
    }

    public static <T> RxRetainFragment<T> create(FragmentManager fragmentManager, Observable<T> observable, RetainSubscriber<T> observer, String tag) {
        return initFragment(fragmentManager, observable, observer, tag);
    }


    public static <T> RxRetainFragment<T> restart(FragmentManager fragmentManager, Observable<T> observable) {
        return restart(fragmentManager, observable, new EmptySubscriber<T>());
    }

    public static <T> RxRetainFragment<T> restart(FragmentManager fragmentManager, Observable<T> observable, RetainSubscriber<T> observer) {
        return restart(fragmentManager, observable, observer, DEFAULT_TAG);
    }

    public static <T> RxRetainFragment<T> restart(FragmentManager fragmentManager, Observable<T> observable, RetainSubscriber<T> observer, String tag) {
        RxRetainFragment<T> fragment = initFragment(fragmentManager, observable, observer, tag);
        fragment.stop();
        fragment.setObservable(observable);
        fragment.setObserver(observer);
        fragment.start();
        return fragment;
    }

    public static <T> RxRetainFragment<T> start(FragmentManager fragmentManager, Observable<T> observable, EmptySubscriber<T> observer) {
        return start(fragmentManager, observable, observer, DEFAULT_TAG);
    }

    public static <T> RxRetainFragment<T> start(FragmentManager fragmentManager, Observable<T> observable, RetainSubscriber<T> observer, String tag) {
        RxRetainFragment<T> fragment = initFragment(fragmentManager, observable, observer, tag);
        fragment.start();
        return fragment;
    }

    private static <T> RxRetainFragment<T> initFragment(FragmentManager fragmentManager, Observable<T> observable, RetainSubscriber<T> observer, String tag) {
        RxRetainFragment<T> fragment = getFragmentByTag(fragmentManager, tag);
        if (fragment == null) {
            fragment = new RxRetainFragment<>(observable);
            fragmentManager.beginTransaction().add(fragment, tag).commit();
        }
        if (!fragment.hasObservable()) {
            fragment.setObservable(observable);
        }
        fragment.setObserver(observer);
        return fragment;
    }


    private static <T> RxRetainFragment<T> getFragmentByTag(FragmentManager fragmentManager, String tag) {
        return (RxRetainFragment<T>) fragmentManager.findFragmentByTag(tag);
    }
}
