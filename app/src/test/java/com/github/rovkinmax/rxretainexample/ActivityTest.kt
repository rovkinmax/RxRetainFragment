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
        val fragment = activity.createRetainFragment(Observable.create { for (i in 0..9) it.onNext(i); it.onCompleted() })
        val testSubscriber = TestSubscriber<Int>()
        fragment.subscribe(testSubscriber)

        testSubscriber.assertCompleted()
        testSubscriber.assertNoErrors()
        Assert.assertEquals(testSubscriber.onNextEvents.size, 10)

    }
}