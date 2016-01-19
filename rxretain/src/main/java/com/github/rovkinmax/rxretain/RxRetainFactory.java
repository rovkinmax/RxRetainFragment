package com.github.rovkinmax.rxretain;

import android.app.FragmentManager;

import rx.Observable;
import rx.Subscriber;

/**
 * @author Rovkin Max
 */
public final class RxRetainFactory {
    private static final String DEFAULT_TAG = "RX_RETAIN_FRAGMENT_INSTANCE";

    private RxRetainFactory() {
    }

    public static <T> RxRetainFragment<T> create(FragmentManager fragmentManager, Observable<T> observable) {
        return create(fragmentManager, observable, new EmptyObserver<T>());
    }

    public static <T> RxRetainFragment<T> create(FragmentManager fragmentManager, Observable<T> observable, String tag) {
        return create(fragmentManager, observable, new EmptyObserver<T>(), tag);
    }

    public static <T> RxRetainFragment<T> create(FragmentManager fragmentManager, Observable<T> observable, Subscriber<T> subscriber) {
        return create(fragmentManager, observable, subscriber, DEFAULT_TAG);
    }

    public static <T> RxRetainFragment<T> create(FragmentManager fragmentManager, Observable<T> observable, Subscriber<T> subscriber, String tag) {
        RxRetainFragment<T> fragment = initFragment(fragmentManager, observable, tag);
        if (!fragment.getManager().hasSubscriber()) {
            fragment.getManager().setSubscriber(subscriber);
        }
        return fragment;
    }


    public static <T> RxRetainFragment<T> restart(FragmentManager fragmentManager, Observable<T> observable) {
        return restart(fragmentManager, observable, new EmptyObserver<T>());
    }

    public static <T> RxRetainFragment<T> restart(FragmentManager fragmentManager, Observable<T> observable, Subscriber<T> subscriber) {
        return restart(fragmentManager, observable, subscriber, DEFAULT_TAG);
    }

    public static <T> RxRetainFragment<T> restart(FragmentManager fragmentManager, Observable<T> observable, Subscriber<T> subscriber, String tag) {
        RxRetainFragment<T> fragment = initFragment(fragmentManager, observable, tag);
        fragment.unsubscribe();
        fragment.getManager().setObservable(observable);
        fragment.getManager().setSubscriber(subscriber);
        fragment.getManager().start();
        return fragment;
    }

    /**
     * Start Observer with empty subscriber and default tag or subscribe for previous if it already running.
     * If you want drop observable for this tag see {@link RxRetainFragment#unsubscribeAndDropObservable()}.
     * Also all subscriber will be unsubscribed. Also all subscriber will be unsubscribed.
     * If you want subscribe without unsubscribe previous subscriber use {@link RxRetainFactory#create(FragmentManager, Observable, String)}
     * or {@link RxRetainFactory#create(FragmentManager, Observable)} with {@link RxRetainFragment#subscribe()} methods.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @return instance of {@link RxRetainFragment}
     */
    public static <T> RxRetainFragment<T> start(FragmentManager fragmentManager, Observable<T> observable) {
        return start(fragmentManager, observable, new EmptyObserver<T>());
    }


    /**
     * Start Observer with default tag or subscribe for previous if it already running.
     * If you want drop observable for this tag see {@link RxRetainFragment#unsubscribeAndDropObservable()}.
     * Also all subscriber will be unsubscribed. Also all subscriber will be unsubscribed.
     * If you want subscribe without unsubscribe previous subscriber use {@link RxRetainFactory#create(FragmentManager, Observable, String)}
     * or {@link RxRetainFactory#create(FragmentManager, Observable)} with {@link RxRetainFragment#subscribe()} methods.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @param subscriber      the {@link Subscriber} that will handle emissions and notifications from the Observable
     * @return instance of {@link RxRetainFragment}
     */
    public static <T> RxRetainFragment<T> start(FragmentManager fragmentManager, Observable<T> observable, Subscriber<T> subscriber) {
        return start(fragmentManager, observable, subscriber, DEFAULT_TAG);
    }

    /**
     * Start Observer with tag or subscribe for previous if it already running.
     * If you want drop observable for this tag see {@link RxRetainFragment#unsubscribeAndDropObservable()}.
     * Also all subscriber will be unsubscribed.
     * If you want subscribe without unsubscribe previous subscriber use {@link RxRetainFactory#create(FragmentManager, Observable, String)}
     * or {@link RxRetainFactory#create(FragmentManager, Observable)} with {@link RxRetainFragment#subscribe()} methods.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @param subscriber      the {@link Subscriber} that will handle emissions and notifications from the Observable
     * @param tag             tag for create or find fragment
     * @return instance of {@link RxRetainFragment}
     */
    public static <T> RxRetainFragment<T> start(FragmentManager fragmentManager, Observable<T> observable, Subscriber<T> subscriber, String tag) {
        RxRetainFragment<T> fragment = initFragment(fragmentManager, observable, tag);
        fragment.getManager().unsubscribeCurrentIfOption();
        fragment.getManager().setSubscriber(subscriber);
        fragment.getManager().start();
        return fragment;
    }

    private static <T> RxRetainFragment<T> initFragment(FragmentManager fragmentManager, Observable<T> observable, String tag) {
        RxRetainFragment<T> fragment = getFragmentByTag(fragmentManager, tag);
        if (fragment == null) {
            fragment = new RxRetainFragment<>(observable);
            fragmentManager.beginTransaction().add(fragment, tag).commit();
        }
        if (!fragment.getManager().hasObservable()) {
            fragment.getManager().setObservable(observable);
        }

        return fragment;
    }


    private static <T> RxRetainFragment<T> getFragmentByTag(FragmentManager fragmentManager, String tag) {
        return (RxRetainFragment<T>) fragmentManager.findFragmentByTag(tag);
    }
}
