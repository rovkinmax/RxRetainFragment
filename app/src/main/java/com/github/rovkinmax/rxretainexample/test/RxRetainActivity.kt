package com.github.rovkinmax.rxretainexample.test

import android.app.Activity
import android.os.Bundle
import com.github.rovkinmax.rxretain.RxRetainFactory
import com.github.rovkinmax.rxretain.RxRetainFragment
import rx.Observable
import rx.Subscriber
import java.util.*

/**
 * @author Rovkin Max
 */


open class RxRetainActivity<T> : Activity(), TestableRxActivity<T> {


    private var isStarted = false;
    private var isCompleted = false
    private var error: Throwable? = null
    private var nextList = ArrayList<T>()
    private val observer = object : Subscriber<T>() {

        override fun onStart() {
            isStarted = true
        }

        override fun onNext(t: T) {
            nextList.add(t)
        }

        override fun onCompleted() {
            isCompleted = true
        }

        override fun onError(e: Throwable?) {
            error = e;
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun createRetainFragment(observable: Observable<T>, subscriber: Subscriber<T>, tag: String): RxRetainFragment<T> {
        return RxRetainFactory.create(fragmentManager, observable, subscriber, tag)
    }

    override fun createRetainFragment(observable: Observable<T>, tag: String): RxRetainFragment<T> {
        return createRetainFragment(observable, observer, tag)
    }

    override fun getNextList() = nextList

    override fun getError() = error

    override fun isStarted() = isStarted

    override fun isCompleted() = isCompleted

    override fun waitForNext() {
        throw UnsupportedOperationException()
    }

    override fun waitForStarted() {
        throw UnsupportedOperationException()
    }

    override fun waitForCompleted() {
        throw UnsupportedOperationException()
    }


}