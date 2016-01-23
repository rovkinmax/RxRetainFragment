package com.github.rovkinmax.rxretainexample

import android.app.Activity
import android.app.FragmentManager
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.filters.SdkSuppress
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.UiDevice
import com.github.rovkinmax.rxretain.RetainFactory
import com.github.rovkinmax.rxretainexample.test.*
import com.robotium.solo.Solo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.Observable
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit.SECONDS

/**
 * @author Rovkin Max
 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class RotationActivityTest {

    @get:Rule
    val rule = ActivityTestRule<TestableActivity>(TestableActivity::class.java, false, false)

    private lateinit var fragmentManager: FragmentManager
    private lateinit var activity: Activity
    private lateinit var device: UiDevice
    private lateinit var basePackage: String
    private lateinit var solo: Solo
    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.setOrientationNatural()
        device.unfreezeRotation()

        val context = InstrumentationRegistry.getContext()
        activity = rule.launchActivity(Intent(context, TestableActivity::class.java))
        solo = Solo(InstrumentationRegistry.getInstrumentation(), activity)
        basePackage = activity.packageName
        fragmentManager = activity.fragmentManager
    }

    @Test
    fun testUnsubscribeAfterActivityRotation() {
        val testSubscriber = TestSubscriber<Long>()
        RetainFactory.start(fragmentManager, Observable.timer(10, SECONDS).bindToThread(), testSubscriber)
        testSubscriber.awaitTerminalEvent(2, SECONDS)

        changeOrientationAndWait()
        testSubscriber.assertUnsubscribed()
    }

    @Test
    fun testAttachInToRunningObservableAfterRotation() {
        val testSubscriber = TestSubscriber<Int>()
        RetainFactory.start(fragmentManager, rangeWithDelay(0, 10, SECONDS.toMillis(10)).bindToThread(), testSubscriber)
        testSubscriber.awaitTerminalEvent(2, SECONDS)

        changeOrientationAndWait()
        testSubscriber.assertUnsubscribed()

        val secondSubscriber = TestSubscriber<Int>()
        //in this case may use any observable. I want use null, cause now it's ignored
        RetainFactory.start(fragmentManager, null, secondSubscriber)

        secondSubscriber.awaitIfNotUnsubscribed()

        secondSubscriber.assertCompleted()
        secondSubscriber.assertReceivedOnNext((0..9).toArrayList())
    }

    @Test
    fun testCacheAfterRotationWithStartMethod() {
        val testSubscriber = TestSubscriber<Int>()
        RetainFactory.start(fragmentManager, rangeWithDelay(0, 10, SECONDS.toMillis(2)).bindToThread(), testSubscriber)
        testSubscriber.awaitTerminalEvent(10, SECONDS)

        changeOrientationAndWait()

        val secondSubscription = TestSubscriber<Int>()
        //in this case may use any observable. I want use null, cause now it's ignored
        RetainFactory.start(fragmentManager, null, secondSubscription)
        secondSubscription.awaitTerminalEvent(10, SECONDS)

        secondSubscription.assertCompleted()
        secondSubscription.assertReceivedOnNext((0..9).toArrayList())
    }

    @Test
    fun testAttachInToTwoRunningObservablesAfterRotation() {
        val firstTag = "first tag"
        val secondTag = "second tag"
        var firstSubscriber = TestSubscriber<Int>()
        RetainFactory.start(fragmentManager, rangeWithDelay(0, 5, SECONDS.toMillis(5)).bindToThread(), firstSubscriber, firstTag)
        firstSubscriber.awaitTerminalEvent(1, SECONDS)

        var secondSubscriber = TestSubscriber<Int>()
        RetainFactory.start(fragmentManager, rangeWithDelay(0, 4, SECONDS.toMillis(8)).bindToThread(), secondSubscriber, secondTag)
        secondSubscriber.awaitTerminalEvent(2, SECONDS)

        changeOrientationAndWait()

        firstSubscriber.assertUnsubscribed()
        secondSubscriber.assertUnsubscribed()

        firstSubscriber = TestSubscriber()
        secondSubscriber = TestSubscriber()
        RetainFactory.start(fragmentManager, null, firstSubscriber, firstTag)
        RetainFactory.start(fragmentManager, null, secondSubscriber, secondTag)

        firstSubscriber.awaitIfNotUnsubscribed()
        secondSubscriber.awaitIfNotUnsubscribed()

        firstSubscriber.assertCompleted()
        firstSubscriber.assertReceivedOnNext((0..4).toArrayList())

        secondSubscriber.assertCompleted()
        secondSubscriber.assertReceivedOnNext((0..3).toArrayList())

    }

    @Test
    fun testCacheErrorAfterRotation() {
        val subscriber = TestSubscriber<Int>()
        RetainFactory.start(fragmentManager, errorObservable(TestException("ha")).bindToThread(), subscriber)
        subscriber.awaitTerminalEvent(2, SECONDS)

        changeOrientationAndWait()
        val subscriber2 = TestSubscriber<Int>()
        RetainFactory.start(fragmentManager, null, subscriber2)
        subscriber2.awaitIfNotUnsubscribed()
        subscriber2.assertError(TestException("ha"))
    }

    @Test
    fun testCachedErrorInToTwoObservablesAfterRotation() {
        val firstTag = "first tag"
        val secondTag = "second tag"
        var firstSubscriber = TestSubscriber<Int>()
        RetainFactory.start(fragmentManager, errorObservable(TestException("first")).bindToThread(), firstSubscriber, firstTag)
        firstSubscriber.awaitTerminalEvent(1, SECONDS)

        var secondSubscriber = TestSubscriber<Int>()
        RetainFactory.start(fragmentManager, errorObservable(TestException("second")).bindToThread(), secondSubscriber, secondTag)
        secondSubscriber.awaitTerminalEvent(2, SECONDS)

        changeOrientationAndWait()

        firstSubscriber.assertUnsubscribed()
        secondSubscriber.assertUnsubscribed()

        firstSubscriber = TestSubscriber()
        secondSubscriber = TestSubscriber()
        RetainFactory.start(fragmentManager, null, firstSubscriber, firstTag)
        RetainFactory.start(fragmentManager, null, secondSubscriber, secondTag)

        firstSubscriber.awaitIfNotUnsubscribed()
        secondSubscriber.awaitIfNotUnsubscribed()

        firstSubscriber.assertError(TestException("first"))
        secondSubscriber.assertError(TestException("second"))
    }

    @Test
    fun testClearCacheAfterUnsubscribeFragment() {
        val subscriber = TestSubscriber<Int>()
        val fragment = RetainFactory.start(fragmentManager, rangeWithDelay(0, 5, SECONDS.toMillis(5)).bindToThread(), subscriber)
        subscriber.awaitTerminalEvent(2, SECONDS)
        fragment.unsubscribe()

        subscriber.assertUnsubscribed()

        changeOrientationAndWait()

        val subscriber2 = TestSubscriber<Int>()
        RetainFactory.start(fragmentManager, null, subscriber2)
        subscriber2.awaitTerminalEvent(2, SECONDS)
        subscriber2.unsubscribe()

        subscriber2.assertNoTerminalEvent()
    }

    @Test
    fun testClearErrorCacheAfterUnsubscribeFragment() {
        val subscriber = TestSubscriber<Int>()
        val fragment = RetainFactory.start(fragmentManager, errorObservable(TestException("cached")).bindToThread(), subscriber)
        subscriber.awaitTerminalEvent(2, SECONDS)
        fragment.unsubscribe()

        subscriber.assertUnsubscribed()

        changeOrientationAndWait()

        val subscriber2 = TestSubscriber<Int>()
        RetainFactory.start(fragmentManager, null, subscriber2)
        subscriber2.awaitTerminalEvent(2, SECONDS)
        subscriber2.unsubscribe()

        subscriber2.assertNoTerminalEvent()
    }

    private fun changeOrientationAndWait() {
        device.setOrientationLeft()
        device.waitForWindowUpdate(basePackage, SECONDS.toMillis(5))
    }

    @After
    fun tearDown() {
        solo.finishOpenedActivities()
    }
}