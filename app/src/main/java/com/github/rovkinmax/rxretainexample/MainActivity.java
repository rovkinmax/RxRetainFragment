package com.github.rovkinmax.rxretainexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.rovkinmax.rxretain.EmptySubscriber;
import com.github.rovkinmax.rxretain.RetainFactory;
import com.github.rovkinmax.rxretain.RetainWrapper;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RX_TAG";
    private RetainWrapper<Integer> mRotateExample;

    private Observable<Integer> mObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRotateExample = RetainFactory.create(getFragmentManager(), initReadyToUserObservable(10), new EmptySubscriber<Integer>() {
            @Override
            public void onNext(Integer integer) {
                printInt(integer);
            }
        }, "ROTATION_PRINTER");

        initReadyToUserObservable(10)
                .compose(RetainFactory.<Integer>bindToRetain(getFragmentManager(), "ROTATION_PRINTER"))
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Toast.makeText(MainActivity.this, "start", Toast.LENGTH_LONG).show();
                    }
                }).subscribe();

    }

    public void openSecondActivity(View v) {
        startActivity(new Intent(this, SecondActivity.class));
    }

    public void simpleRun(View v) {
        RetainFactory.start(getFragmentManager(), initReadyToUserObservable(10), null)
                .subscribe(buildPrintAction());
    }

    public void restartObservable(View v) {
        RetainFactory.restart(getFragmentManager(), initReadyToUserObservable(-10, 0), null)
                .subscribe(buildPrintAction());
    }

    public void restartIfError(View v) {
        startObservable(-1);
    }

    public void startTwoObservable(View v) {
        RetainFactory.start(getFragmentManager(), initReadyToUserObservable(-11, -1), null, "NEGATIVE_PRINTER")
                .subscribe(buildPrintAction());

        RetainFactory.start(getFragmentManager(), initReadyToUserObservable(0, 10), null, "POSITIVE_PRINTER")
                .subscribe(buildPrintAction());
    }


    private void startObservable(int count) {
        final String tag = "RESUMABLE_PRINTER";
        Observable<Integer> observable = initReadyToUserObservable(count)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        restartObservable(11, tag);
                    }
                });
        RetainFactory.start(getFragmentManager(), observable, null, tag)
                .subscribe(buildPrintAction(), new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                }, new Action0() {
                    @Override
                    public void call() {
                    }
                });

    }

    private void restartObservable(int count, String tag) {
        Observable<Integer> observable = initReadyToUserObservable(count);
        RetainFactory.restart(getFragmentManager(), observable, null, tag)
                .subscribe(buildPrintAction(), new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                }, new Action0() {
                    @Override
                    public void call() {
                    }
                });
    }

    public void rotateExample(View view) {
        mObservable.subscribe();
    }

    private Observable<Integer> initReadyToUserObservable(int count) {
        return initReadyToUserObservable(0, count);
    }

    private Observable<Integer> initReadyToUserObservable(int min, int max) {
        return buildDelayRange(min, max)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    private Action1<Integer> buildPrintAction() {
        return new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                printInt(integer);
            }
        };
    }

    private void printInt(Integer integer) {
        Log.d(TAG, "Value" + (integer + 1));
    }

    private Observable<Integer> buildDelayRange(final int min, final int max) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                if (min > max) {
                    throw new IllegalArgumentException();
                }
                for (int i = min; i < max; i++) {
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
}
