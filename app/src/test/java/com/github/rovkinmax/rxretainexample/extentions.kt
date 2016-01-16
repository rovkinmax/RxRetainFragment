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