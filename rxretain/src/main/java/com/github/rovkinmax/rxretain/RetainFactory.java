package com.github.rovkinmax.rxretain;

import android.app.FragmentManager;

import rx.Observable;
import rx.Subscriber;

/**
 * @author Rovkin Max
 */
public final class RetainFactory {
    private static final String DEFAULT_TAG = "RX_RETAIN_FRAGMENT_INSTANCE";

    private RetainFactory() {
        throw new AssertionError("No instance");
    }


    /**
     * Get {@link RetainWrapper} with old observable or create new instance with new observable for default tag.
     * If you want drop observable for this tag see {@link RetainWrapper#unsubscribeAndDropObservable()}.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @return instance of {@link RetainWrapper}
     */
    public static <T> RetainWrapper<T> create(FragmentManager fragmentManager, Observable<T> observable) {
        return create(fragmentManager, observable, new EmptySubscriber<T>());
    }

    /**
     * Get {@link RetainWrapper} with old observable or create new instance with new observable for tag.
     * If you want drop observable for this tag see {@link RetainWrapper#unsubscribeAndDropObservable()}.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @param tag             tag for create or find fragment
     * @return instance of {@link RetainWrapper}
     */
    public static <T> RetainWrapper<T> create(FragmentManager fragmentManager, Observable<T> observable, String tag) {
        return create(fragmentManager, observable, new EmptySubscriber<T>(), tag);
    }


    /**
     * Get {@link RetainWrapper} with old observable and old subscriber or create new instance with new observable and new subscriber for default tag.
     * If you want drop observable for this tag see {@link RetainWrapper#unsubscribeAndDropObservable()}.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @param subscriber      the {@link Subscriber} that will handle emissions and notifications from the Observable
     * @return instance of {@link RetainWrapper}
     */
    public static <T> RetainWrapper<T> create(FragmentManager fragmentManager, Observable<T> observable, Subscriber<T> subscriber) {
        return create(fragmentManager, observable, subscriber, DEFAULT_TAG);
    }

    /**
     * Get {@link RetainWrapper} with old observable and old subscriber or create new instance with new observable and new subscriber for tag.
     * If you want drop observable for this tag see {@link RetainWrapper#unsubscribeAndDropObservable()}.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @param subscriber      the {@link Subscriber} that will handle emissions and notifications from the Observable
     * @param tag             tag for create or find fragment
     * @return instance of {@link RetainWrapper}
     */
    public static <T> RetainWrapper<T> create(FragmentManager fragmentManager, Observable<T> observable, Subscriber<? super T> subscriber, String tag) {
        RetainWrapper<T> fragment = initFragment(fragmentManager, observable, tag);
        if (!fragment.getManager().hasSubscriber()) {
            fragment.getManager().setSubscriber(subscriber);
        }
        return fragment;
    }


    /**
     * Restart previous observable with empty subscriber and default tag or start new if not exists
     * If you want drop observable for this tag see {@link RetainWrapper#unsubscribeAndDropObservable()}.
     * Also all subscriber will be unsubscribed. Also all subscriber will be unsubscribed.
     * If you want subscribe without unsubscribe previous subscriber use {@link RetainFactory#create(FragmentManager, Observable, String)}
     * or {@link RetainFactory#create(FragmentManager, Observable)} with {@link RetainWrapper#subscribe()} methods.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @return instance of {@link RetainWrapper}
     */
    public static <T> RetainWrapper<T> restart(FragmentManager fragmentManager, Observable<T> observable) {
        return restart(fragmentManager, observable, new EmptySubscriber<T>());
    }


    /**
     * Restart previous observable with default tag or start observable and subscribe on it if not exists
     * If you want drop observable for this tag see {@link RetainWrapper#unsubscribeAndDropObservable()}.
     * Also all subscriber will be unsubscribed. Also all subscriber will be unsubscribed.
     * If you want subscribe without unsubscribe previous subscriber use {@link RetainFactory#create(FragmentManager, Observable, String)}
     * or {@link RetainFactory#create(FragmentManager, Observable)} with {@link RetainWrapper#subscribe()} methods.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @param subscriber      the {@link Subscriber} that will handle emissions and notifications from the Observable
     * @return instance of {@link RetainWrapper}
     */
    public static <T> RetainWrapper<T> restart(FragmentManager fragmentManager, Observable<T> observable, Subscriber<T> subscriber) {
        return restart(fragmentManager, observable, subscriber, DEFAULT_TAG);
    }

    /**
     * Restart previous observable  with tag or start observable.
     * If you want drop observable for this tag see {@link RetainWrapper#unsubscribeAndDropObservable()}.
     * Also all subscriber will be unsubscribed.
     * If you want subscribe without unsubscribe previous subscriber use {@link RetainFactory#create(FragmentManager, Observable, String)}
     * or {@link RetainFactory#create(FragmentManager, Observable)} with {@link RetainWrapper#subscribe()} methods.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @param subscriber      the {@link Subscriber} that will handle emissions and notifications from the Observable
     * @param tag             tag for create or find fragment
     * @return instance of {@link RetainWrapper}
     */
    public static <T> RetainWrapper<T> restart(FragmentManager fragmentManager, Observable<T> observable, Subscriber<T> subscriber, String tag) {
        RetainWrapper<T> fragment = initFragment(fragmentManager, observable, tag);
        fragment.unsubscribe();
        fragment.getManager().setObservable(observable);
        fragment.getManager().setSubscriber(subscriber);
        fragment.getManager().start();
        return fragment;
    }

    /**
     * Start observable with empty subscriber and default tag or subscribe for previous if it already running.
     * If you want drop observable for this tag see {@link RetainWrapper#unsubscribeAndDropObservable()}.
     * Also all subscriber will be unsubscribed. Also all subscriber will be unsubscribed.
     * If you want subscribe without unsubscribe previous subscriber use {@link RetainFactory#create(FragmentManager, Observable, String)}
     * or {@link RetainFactory#create(FragmentManager, Observable)} with {@link RetainWrapper#subscribe()} methods.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @return instance of {@link RetainWrapper}
     */
    public static <T> RetainWrapper<T> start(FragmentManager fragmentManager, Observable<T> observable) {
        return start(fragmentManager, observable, new EmptySubscriber<T>());
    }


    /**
     * Start observable with default tag or subscribe for previous if it already running.
     * If you want drop observable for this tag see {@link RetainWrapper#unsubscribeAndDropObservable()}.
     * Also all subscriber will be unsubscribed. Also all subscriber will be unsubscribed.
     * If you want subscribe without unsubscribe previous subscriber use {@link RetainFactory#create(FragmentManager, Observable, String)}
     * or {@link RetainFactory#create(FragmentManager, Observable)} with {@link RetainWrapper#subscribe()} methods.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @param subscriber      the {@link Subscriber} that will handle emissions and notifications from the Observable
     * @return instance of {@link RetainWrapper}
     */
    public static <T> RetainWrapper<T> start(FragmentManager fragmentManager, Observable<T> observable, Subscriber<T> subscriber) {
        return start(fragmentManager, observable, subscriber, DEFAULT_TAG);
    }

    /**
     * Start observable with tag or subscribe for previous if it already running.
     * If you want drop observable for this tag see {@link RetainWrapper#unsubscribeAndDropObservable()}.
     * Also all subscriber will be unsubscribed.
     * If you want subscribe without unsubscribe previous subscriber use {@link RetainFactory#create(FragmentManager, Observable, String)}
     * or {@link RetainFactory#create(FragmentManager, Observable)} with {@link RetainWrapper#subscribe()} methods.
     *
     * @param fragmentManager fragment manager current activity. need for attach fragment for activity
     * @param observable      observable for execution. Ignoring if this method was called with the same tag.
     * @param subscriber      the {@link Subscriber} that will handle emissions and notifications from the Observable
     * @param tag             tag for create or find fragment
     * @return instance of {@link RetainWrapper}
     */
    public static <T> RetainWrapper<T> start(FragmentManager fragmentManager, Observable<T> observable, Subscriber<T> subscriber, String tag) {
        RetainWrapper<T> fragment = initFragment(fragmentManager, observable, tag);
        fragment.getManager().unsubscribeCurrentIfOption();
        fragment.getManager().setSubscriber(subscriber);
        fragment.getManager().start();
        return fragment;
    }

    private static <T> RetainWrapper<T> initFragment(FragmentManager fragmentManager, Observable<T> observable, String tag) {
        RetainWrapper<T> fragment = getFragmentByTag(fragmentManager, tag);
        if (fragment == null) {
            fragment = new RetainWrapper<>(observable);
            fragmentManager.beginTransaction().add(fragment, tag).commit();
            fragmentManager.executePendingTransactions();
        }
        if (!fragment.getManager().hasObservable()) {
            fragment.getManager().setObservable(observable);
        }

        return fragment;
    }


    @SuppressWarnings("unchecked")
    private static <T> RetainWrapper<T> getFragmentByTag(FragmentManager fragmentManager, String tag) {
        return (RetainWrapper<T>) fragmentManager.findFragmentByTag(tag);
    }

    public static <T> Observable.Transformer<? super T, T> bindToRetain(final FragmentManager fragmentManager) {
        return bindToRetain(fragmentManager, DEFAULT_TAG);
    }

    public static <T> Observable.Transformer<? super T, T> bindToRetain(final FragmentManager fragmentManager, final String tag) {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(final Observable<T> observable) {

                return Observable.create(new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        create(fragmentManager, observable, tag).subscribe(subscriber);
                    }
                });
            }
        };
    }
}
