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
class ActivityTest {

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
        val fragment = RxRetainFactory.create(fragmentManager, Observable.range(0, 10))
        val testSubscriber = TestSubscriber<Int>()
        fragment.subscribe(testSubscriber)

        testSubscriber.assertCompleted()
        testSubscriber.assertNoErrors()
        testSubscriber.assertValueCount(10)
        testSubscriber.assertReceivedOnNext((0..9).toArrayList())

    }

    @Test
    fun testThreadSimpleRun() {
        val observableThread = Observable.range(0, 10).bindToThread()
        val testSubscriber = TestSubscriber<Int>()
        RxRetainFactory.create(fragmentManager, observableThread).subscribe(testSubscriber)

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertCompleted()
        testSubscriber.assertValueCount(10)
        testSubscriber.assertReceivedOnNext((0..9).toArrayList())
    }

    @Test
    fun testStartObservable() {

        var testSubscriber = TestSubscriber<Long>()
        RxRetainFactory.start(fragmentManager, Observable.timer(10, TimeUnit.SECONDS).bindToThread(), testSubscriber)
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
}