package com.github.rovkinmax.rxretainexample

import android.app.FragmentManager
import android.content.Context
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.filters.SdkSuppress
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.test.uiautomator.UiDevice
import com.github.rovkinmax.rxretain.RetainFactory
import com.github.rovkinmax.rxretainexample.test.TestableActivity
import com.github.rovkinmax.rxretainexample.test.bindToThread
import com.github.rovkinmax.rxretainexample.test.rangeWithDelay
import com.robotium.solo.Solo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit.SECONDS

/**
 * @author Rovkin Max
 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class ComposeBindTest {
    @get:Rule
    val rule = ActivityTestRule<TestableActivity>(TestableActivity::class.java, false, false)
    private lateinit var context: Context
    private lateinit var activity: TestableActivity
    private lateinit var fragmentManager: FragmentManager
    private lateinit var solo: Solo
    private lateinit var device: UiDevice
    private lateinit var basePackage: String

    @Before
    fun setUp() {
        setUpDevice()

        context = InstrumentationRegistry.getContext()
        activity = rule.launchActivity(Intent(context, TestableActivity::class.java))
        solo = Solo(InstrumentationRegistry.getInstrumentation(), activity)
        fragmentManager = activity.fragmentManager

        basePackage = activity.packageName
    }

    private fun setUpDevice() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.setOrientationNatural()
        device.unfreezeRotation()
    }

    @Test
    fun testSimpleCompose() {
        val subscriber = TestSubscriber<Int>()
        rangeWithDelay(0, 5, SECONDS.toMillis(1))
                .bindToThread()
                .compose(RetainFactory.bindToRetain(fragmentManager))
                .subscribeOnUI(subscriber)

        subscriber.awaitTerminalEvent()
        subscriber.assertCompleted()
    }

    @Test
    fun testRotationUnsubscribe() {
        val subscriber = TestSubscriber<Int>()
        rangeWithDelay(0, 5, SECONDS.toMillis(10))
                .bindToThread()
                .compose(RetainFactory.bindToRetain(fragmentManager, "unsubscribe tag"))
                .subscribeOnUI(subscriber)

        subscriber.awaitTerminalEvent(1, SECONDS)

        changeOrientationAndWait()

        subscriber.assertUnsubscribed()
    }


    @Test
    fun testBindAfterRotation() {
        val subscriber = TestSubscriber<Int>()
        rangeWithDelay(0, 5, SECONDS.toMillis(5))
                .bindToThread()
                .compose(RetainFactory.bindToRetain(fragmentManager, "bind rotation"))
                .subscribeOnUI(subscriber)

        subscriber.awaitTerminalEvent(1, SECONDS)
        changeOrientationAndWait()

        val secondSubscriber = TestSubscriber<Int>()
        rangeWithDelay(0, 5, SECONDS.toMillis(5))
                .bindToThread()
                .compose(RetainFactory.bindToRetain(fragmentManager, "bind rotation"))
                .subscribeOnUI(secondSubscriber)

        secondSubscriber.awaitTerminalEvent()
        secondSubscriber.assertReceivedOnNext((0..4).toCollection(arrayListOf()))
    }

    @Test
    fun testRunByClick() {
        val subscriber = TestSubscriber<Int>()
        val observable = rangeWithDelay(0, 5, SECONDS.toMillis(5))
                .bindToThread()
                .compose(RetainFactory.bindToRetain(fragmentManager, "run by click"))


        runOnUIAndWait {
            activity.performClick({ observable.subscribe(subscriber) })
        }

        subscriber.awaitTerminalEvent()
        subscriber.assertReceivedOnNext((0..4).toCollection(arrayListOf()))

    }

    @After
    fun tearDown() {
        solo.finishOpenedActivities()
        solo.finalize()
    }

    private fun changeOrientationAndWait() {
        device.setOrientationLeft()
        device.waitForWindowUpdate(basePackage, SECONDS.toMillis(5))
    }

    private fun <T> Observable<T>.subscribeOnUI(subscriber: Observer<T>): Subscription {
        return runOnUIAndWait { subscribe(subscriber) }
    }

    private fun <T> runOnUIAndWait(func: () -> T): T {
        var result: T? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = func() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        return result!!
    }
}