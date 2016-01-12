package com.github.rovkinmax.rxretainexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RX_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startObservable(-1);
    }

    private void startObservable(int count) {
        Observable<Integer> observable = initReadyToUserObservable(count);
        RxRetainFragment.start(getFragmentManager(), observable)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Log.d(TAG, "Value" + (integer + 1));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                }, new Action0() {
                    @Override
                    public void call() {
                    }
                });
    }

    private Observable<Integer> initReadyToUserObservable(int count) {
        return buildDelayRange(count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        restartObservable(11);
                    }
                });
    }

    private void restartObservable(int count) {
        Observable<Integer> observable = initReadyToUserObservable(count);
        RxRetainFragment.restart(getFragmentManager(), observable)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Log.d(TAG, "Value" + (integer + 1));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                }, new Action0() {
                    @Override
                    public void call() {
                    }
                });
    }

    private Observable<Integer> buildDelayRange(final int maxRange) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                if (maxRange < 0) {
                    throw new IllegalArgumentException();
                }
                for (int i = 0; i < maxRange; i++) {
                    subscriber.onNext(i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public void onBackPressed() {
        RxRetainFragment.stopExecution(getFragmentManager());
        super.onBackPressed();
    }
}
