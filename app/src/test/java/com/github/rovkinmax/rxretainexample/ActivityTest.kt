package com.github.rovkinmax.rxretainexample

import com.github.rovkinmax.rxretainexample.activity.RxIntActivity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ActivityController
import rx.Observable
import rx.observers.TestSubscriber

/**
 * @author Rovkin Max
 */
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
@RunWith(RobolectricGradleTestRunner::class)
class ActivityTest {

    private lateinit var activity: RxIntActivity
    private lateinit var controller: ActivityController<RxIntActivity>
    @Before
    fun setUp() {
        controller = Robolectric.buildActivity(RxIntActivity::class.java).create().start().visible()
        activity = controller.get()
    }

    @Test
    fun testSimpleRun() {
        val fragment = activity.createRetainFragment(Observable.range(0, 10))
        val testSubscriber = TestSubscriber<Int>()
        fragment.subscribe(testSubscriber)

        testSubscriber.assertCompleted()
        testSubscriber.assertNoErrors()
        testSubscriber.assertValueCount(10)
        Assert.assertEquals(testSubscriber.onNextEvents, (0..9).toArrayList())

    }

    @Test
    fun testThreadSimpleRun() {
        val observableThread = Observable.range(0, 10).bindToThread()
        val testSubscriber = TestSubscriber<Int>()
        activity.createRetainFragment(observableThread).subscribe(testSubscriber)

        testSubscriber.awaitTerminalEvent()
        testSubscriber.assertCompleted()
        testSubscriber.assertValueCount(10)
        Assert.assertEquals(testSubscriber.onNextEvents, (0..9).toArrayList())
    }
}