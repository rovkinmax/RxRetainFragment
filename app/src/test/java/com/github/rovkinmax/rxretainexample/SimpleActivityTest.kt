package com.github.rovkinmax.rxretainexample

import android.app.Activity
import android.app.FragmentManager
import com.github.rovkinmax.rxretain.RxRetainFactory
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ActivityController
import rx.Observable
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit

/**
 * @author Rovkin Max
 */
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
@RunWith(RobolectricGradleTestRunner::class)
class SimpleActivityTest {

    private lateinit var activity: Activity
    private lateinit var controller: ActivityController<Activity>
    private lateinit var fragmentManager: FragmentManager
    @Before
    fun setUp() {
        controller = Robolectric.buildActivity(Activity::class.java).create().start().visible()
        activity = controller.get()
        fragmentManager = activity.fragmentManager
    }

    @Test
    fun testSimpleRun() {
        val testSubscriber = TestSubscriber<Int>()
        RxRetainFactory.start(fragmentManager, Observable.range(0, 10), testSubscriber)

        testSubscriber.assertCompleted()
        testSubscriber.assertNoErrors()
        testSubscriber.assertValueCount(10)
        testSubscriber.assertReceivedOnNext((0..9).toArrayList())

    }

    @Test
    fun testThreadSimpleRun() {
        val observableThread = Observable.range(0, 10).bindToThread()
        val testSubscriber = TestSubscriber<Int>()
        RxRetainFactory.start(fragmentManager, observableThread, testSubscriber)

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertCompleted()
        testSubscriber.assertValueCount(10)
        testSubscriber.assertReceivedOnNext((0..9).toArrayList())
    }

    @Test
    fun testStartObservable() {

        var testSubscriber = TestSubscriber<Long>()
        RxRetainFactory.start(fragmentManager, Observable.timer(5, TimeUnit.SECONDS).bindToThread(), testSubscriber)
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)

        testSubscriber.unsubscribe()

        testSubscriber = Mockito.spy(TestSubscriber<Long>())
        RxRetainFactory.start(fragmentManager, Observable.range(0, 10).map { it.toLong() }.bindToThread(), testSubscriber)

        testSubscriber.awaitTerminalEvent()

        Mockito.verify(testSubscriber).onStart()
        testSubscriber.assertCompleted()
        testSubscriber.assertValueCount(1)
        testSubscriber.assertReceivedOnNext(arrayListOf(0.toLong()))
    }

    @Test
    fun testRestartObservable() {
        val testSubscriber = TestSubscriber<Long>()
        RxRetainFactory.start(fragmentManager, Observable.timer(5, TimeUnit.SECONDS).bindToThread(), testSubscriber)
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)

        val spySubscribers = Mockito.spy(TestSubscriber<Long>())
        RxRetainFactory.restart(fragmentManager, Observable.range(0, 10).map { it.toLong() }.bindToThread(), spySubscribers)
        testSubscriber.assertUnsubscribed()
        Mockito.verify(spySubscribers).onStart()
        spySubscribers.awaitTerminalEvent()

        spySubscribers.assertCompleted()
        spySubscribers.assertValueCount(10)
        spySubscribers.assertReceivedOnNext(((0L)..(9L)).toArrayList())
    }
}