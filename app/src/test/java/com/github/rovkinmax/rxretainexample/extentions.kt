package com.github.rovkinmax.rxretainexample

import rx.Observable
import rx.schedulers.Schedulers

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
            try {
                Thread.sleep(stepDelay)
            } catch(e: Exception) {
            }
        }
        it.onCompleted()
    }
}