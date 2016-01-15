package com.github.rovkinmax.rxretainexample.test

import com.github.rovkinmax.rxretain.RxRetainFragment
import rx.Observable
import rx.Subscriber

/**
 * @author Rovkin Max
 */

val TEST_RX_TAG = "TEST_RX_TAG"

interface TestableRxActivity<T> {

    fun createRetainFragment(observable: Observable<T>, subscriber: Subscriber<T>, tag: String = TEST_RX_TAG): RxRetainFragment<T>

    fun createRetainFragment(observable: Observable<T>, tag: String = TEST_RX_TAG): RxRetainFragment<T>

    @Throws(InterruptedException::class)
    fun waitForNext()

    @Throws(InterruptedException::class)
    fun waitForStarted()

    @Throws(InterruptedException::class)
    fun waitForCompleted()

    fun getNextList(): List<T>

    fun getError(): Throwable?

    fun isStarted(): Boolean

    fun isCompleted(): Boolean
}