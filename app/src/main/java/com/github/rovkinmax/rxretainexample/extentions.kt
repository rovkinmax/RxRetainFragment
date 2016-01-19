package com.github.rovkinmax.rxretainexample

import rx.Observable
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @author Rovkin Max
 */
fun <T> Observable<T>.bindToThread(): Observable<T> {
    return this.subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
}

fun rangeWithDelay(start: Int, count: Int, totalDelay: Long = 5000): Observable<Int> {
    return Observable.create {
        val stepDelay = if (count > 0) totalDelay / (count) else totalDelay
        for (i in (start..(start + count - 1))) {
            it.onNext(i)
            delayInThread(stepDelay)
        }
        it.onCompleted()
    }
}

fun delayInThread(stepDelay: Long) {
    try {
        Thread.sleep(stepDelay)
    } catch(e: Exception) {
    }
}

fun <T> TestSubscriber<T>.awaitIfNotUnsubscribed(timeOutInSeconds: Long = 10): Unit {
    if (!isUnsubscribed)
        awaitTerminalEvent(timeOutInSeconds, TimeUnit.SECONDS)
}