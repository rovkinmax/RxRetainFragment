package com.github.rovkinmax.rxretainexample

import rx.Observable
import rx.schedulers.Schedulers
import java.util.*

/**
 * @author Rovkin Max
 */
fun <T> Observable<T>.bindToThread(): Observable<T> {
    return this.subscribeOn(Schedulers.newThread())
            .observeOn(Schedulers.newThread())
}

fun IntRange.toArrayList() = ArrayList((start..endInclusive).toList())