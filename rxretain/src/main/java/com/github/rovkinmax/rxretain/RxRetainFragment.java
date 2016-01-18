package com.github.rovkinmax.rxretain;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * @author Rovkin Max
 */
public class RxRetainFragment<T> extends Fragment {

    private RxRetainManager<T> mManager;
    private final RxLifecycleCallback<T> mRxLifecycleCallback = new RxLifecycleCallback<>();

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
        getActivity().getApplication().registerActivityLifecycleCallbacks(mRxLifecycleCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getApplication().unregisterActivityLifecycleCallbacks(mRxLifecycleCallback);
    }

    /**
     * Subscribes to an ReplayObject which subscribed on Observable and ignores {@code onNext}, {@code onCompleted} and {@code onError} emissions.
     *
     * @see <a href="http://reactivex.io/documentation/operators/subscribe.html">ReactiveX operators documentation: Subscribe</a>
     */
    public RxRetainFragment<T> subscribe() {
        mManager.subscribe(new EmptyObserver<T>());
        return this;
    }

    /**
     * Subscribes to an ReplayObject which subscribed on Observable and provides a callback to handle the items it emits.
     *
     * @param onNext the {@code Action1<T>} you have designed to accept emissions from the Observable
     * @throws IllegalArgumentException       if {@code onNext} is null
     *                                        if the Observable calls {@code onError}
     * @throws OnErrorNotImplementedException if the Observable calls {@code onError}
     * @see <a href="http://reactivex.io/documentation/operators/subscribe.html">ReactiveX operators documentation: Subscribe</a>
     */
    public RxRetainFragment<T> subscribe(Action1<T> onNext) {
        mManager.subscribe(onNext);
        return this;
    }

    /**
     * Subscribes to an ReplayObject which subscribed on Observable and provides callbacks to handle the items it emits and any error or
     * completion notification it issues.
     *
     * @param onNext      the {@code Action1<T>} you have designed to accept emissions from the Observable
     * @param onError     the {@code Action1<Throwable>} you have designed to accept any error notification from the
     *                    Observable
     * @param onCompleted the {@code Action0} you have designed to accept a completion notification from the
     *                    Observable
     * @throws IllegalArgumentException if {@code onNext} is null, or
     *                                  if {@code onError} is null, or
     *                                  if {@code onComplete} is null
     * @see <a href="http://reactivex.io/documentation/operators/subscribe.html">ReactiveX operators documentation: Subscribe</a>
     */
    public RxRetainFragment<T> subscribe(Action1<T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        mManager.subscribe(onNext, onError, onCompleted);
        return this;
    }


    /**
     * Subscribes to an ReplayObject which subscribed on Observable and provides a Subscriber that implements functions to handle the items the
     * Observable emits and any error or completion notification it issues.
     * <p/>
     * A typical implementation of {@code subscribe} does the following:
     * <ol>
     * <li>It stores a reference to the Subscriber in a collection object, such as a {@code List<T>} object.</li>
     * <li>It returns a reference to the {@link Subscription} interface. This enables Subscribers to
     * unsubscribe, that is, to stop receiving items and notifications before the Observable completes, which
     * also invokes the Subscriber's {@link Subscriber#onCompleted onCompleted} method.</li>
     * </ol><p>
     * An {@code Observable<T>} instance is responsible for accepting all subscriptions and notifying all
     * Subscribers. Unless the documentation for a particular {@code Observable<T>} implementation indicates
     * otherwise, Subscriber should make no assumptions about the order in which multiple Subscribers will
     * receive their notifications.
     * <p/>
     * For more information see the
     * <a href="http://reactivex.io/documentation/observable.html">ReactiveX documentation</a>.
     *
     * @param subscriber the {@link Subscriber} that will handle emissions and notifications from the Observable
     * @throws IllegalStateException          if {@code subscribe} is unable to obtain an {@code OnSubscribe<>} function
     * @throws IllegalArgumentException       if the {@link Subscriber} provided as the argument to {@code subscribe} is {@code null}
     * @throws OnErrorNotImplementedException if the {@link Subscriber}'s {@code onError} method is null
     * @throws RuntimeException               if the {@link Subscriber}'s {@code onError} method itself threw a {@code Throwable}
     * @see <a href="http://reactivex.io/documentation/operators/subscribe.html">ReactiveX operators documentation: Subscribe</a>
     */
    public RxRetainFragment<T> subscribe(Subscriber<T> subscriber) {
        mManager.subscribe(subscriber);
        return this;
    }

    /**
     * Subscribes to an ReplayObject which subscribed on Observable and provides an Observer that implements functions to handle the items the
     * Observable emits and any error or completion notification it issues.
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>{@code subscribe} does not operate by default on a particular {@link Scheduler}.</dd>
     * </dl>
     *
     * @param observer the Observer that will handle emissions and notifications from the Observable
     * @see <a href="http://reactivex.io/documentation/operators/subscribe.html">ReactiveX operators documentation: Subscribe</a>
     */
    public RxRetainFragment<T> subscribe(final Observer<T> observer) {
        mManager.subscribe(observer);
        return this;
    }

    RxRetainManager<T> getManager() {
        return mManager;
    }

    /**
     * Stop execution of Observable. It work like {@link Subscription#unsubscribe()}
     */
    public void unsubscribe() {
        mManager.stop();
    }

    private void createNewManager(Observable<T> observable) {
        mManager = new RxRetainManager<>(observable);
        mRxLifecycleCallback.setManager(mManager);
    }
}