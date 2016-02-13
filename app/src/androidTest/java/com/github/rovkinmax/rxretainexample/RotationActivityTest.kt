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
import org.junit.*
import org.junit.runner.RunWith
import rx.Observable
import rx.observers.TestSubscriber
import rx.schedulers.TestScheduler
import rx.subjects.TestSubject
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
        clearFragments()
    }

    @Test
    fun testUnsubscribeAfterActivityRotation() {
        val testSubscriber = TestSubscriber<Long>()
        runOnUIAndWait {
            RetainFactory.start(fragmentManager, Observable.timer(10, SECONDS).bindToThread(), testSubscriber)
        }
        testSubscriber.awaitTerminalEvent(2, SECONDS)

        changeOrientationAndWait()
        testSubscriber.assertUnsubscribed()
    }

    @Test
    fun testAttachInToRunningObservableAfterRotation() {
        val testSubscriber = TestSubscriber<Int>()
        runOnUIAndWait { RetainFactory.start(fragmentManager, rangeWithDelay(0, 10, SECONDS.toMillis(10)).bindToThread(), testSubscriber) }
        testSubscriber.awaitTerminalEvent(2, SECONDS)

        changeOrientationAndWait()
        testSubscriber.assertUnsubscribed()

        val secondSubscriber = TestSubscriber<Int>()
        //in this case may use any observable. I want use null, cause now it's ignored
        runOnUIAndWait { RetainFactory.start(fragmentManager, null, secondSubscriber) }

        secondSubscriber.awaitIfNotUnsubscribed()

        secondSubscriber.assertCompleted()
        secondSubscriber.assertReceivedOnNext((0..9).toCollection(arrayListOf<Int>()))
    }

    @Test
    fun testCacheAfterRotationWithStartMethod() {
        val testSubscriber = TestSubscriber<Int>()
        runOnUIAndWait { RetainFactory.start(fragmentManager, rangeWithDelay(0, 10, SECONDS.toMillis(2)).bindToThread(), testSubscriber) }
        testSubscriber.awaitTerminalEvent(10, SECONDS)

        changeOrientationAndWait()

        val secondSubscription = TestSubscriber<Int>()
        //in this case may use any observable. I want use null, cause now it's ignored
        runOnUIAndWait { RetainFactory.start(fragmentManager, null, secondSubscription) }
        secondSubscription.awaitTerminalEvent(10, SECONDS)

        secondSubscription.assertCompleted()
        secondSubscription.assertReceivedOnNext((0..9).toCollection(arrayListOf<Int>()))
    }

    @Test
    fun testAttachInToTwoRunningObservablesAfterRotation() {
        val firstTag = "first tag"
        val secondTag = "second tag"
        var firstSubscriber = TestSubscriber<Int>()
        runOnUIAndWait { RetainFactory.start(fragmentManager, rangeWithDelay(0, 5, SECONDS.toMillis(5)).bindToThread(), firstSubscriber, firstTag) }
        firstSubscriber.awaitTerminalEvent(1, SECONDS)

        var secondSubscriber = TestSubscriber<Int>()
        runOnUIAndWait { RetainFactory.start(fragmentManager, rangeWithDelay(0, 4, SECONDS.toMillis(8)).bindToThread(), secondSubscriber, secondTag) }
        secondSubscriber.awaitTerminalEvent(2, SECONDS)

        changeOrientationAndWait()

        firstSubscriber.assertUnsubscribed()
        secondSubscriber.assertUnsubscribed()

        firstSubscriber = TestSubscriber()
        secondSubscriber = TestSubscriber()
        runOnUIAndWait { RetainFactory.start(fragmentManager, null, firstSubscriber, firstTag) }
        runOnUIAndWait { RetainFactory.start(fragmentManager, null, secondSubscriber, secondTag) }

        firstSubscriber.awaitIfNotUnsubscribed()
        secondSubscriber.awaitIfNotUnsubscribed()

        firstSubscriber.assertCompleted()
        firstSubscriber.assertReceivedOnNext((0..4).toCollection(arrayListOf<Int>()))

        secondSubscriber.assertCompleted()
        secondSubscriber.assertReceivedOnNext((0..3).toCollection(arrayListOf<Int>()))

    }

    @Test
    fun testCacheErrorAfterRotation() {
        val subscriber = TestSubscriber<Int>()
        runOnUIAndWait { RetainFactory.start(fragmentManager, errorObservable(TestException("ha")).bindToThread(), subscriber) }
        subscriber.awaitTerminalEvent(2, SECONDS)

        changeOrientationAndWait()
        val subscriber2 = TestSubscriber<Int>()
        runOnUIAndWait { RetainFactory.start(fragmentManager, null, subscriber2) }
        subscriber2.awaitIfNotUnsubscribed()
        subscriber2.assertError(TestException("ha"))
    }

    @Test
    fun testCachedErrorInToTwoObservablesAfterRotation() {
        val firstTag = "first tag"
        val secondTag = "second tag"
        var firstSubscriber = TestSubscriber<Int>()
        runOnUIAndWait { RetainFactory.start(fragmentManager, errorObservable(TestException("first")).bindToThread(), firstSubscriber, firstTag) }
        firstSubscriber.awaitTerminalEvent(1, SECONDS)

        var secondSubscriber = TestSubscriber<Int>()
        runOnUIAndWait { RetainFactory.start(fragmentManager, errorObservable(TestException("second")).bindToThread(), secondSubscriber, secondTag) }
        secondSubscriber.awaitTerminalEvent(2, SECONDS)

        changeOrientationAndWait()

        firstSubscriber.assertUnsubscribed()
        secondSubscriber.assertUnsubscribed()

        firstSubscriber = TestSubscriber()
        secondSubscriber = TestSubscriber()
        runOnUIAndWait { RetainFactory.start(fragmentManager, null, firstSubscriber, firstTag) }
        runOnUIAndWait { RetainFactory.start(fragmentManager, null, secondSubscriber, secondTag) }

        firstSubscriber.awaitIfNotUnsubscribed()
        secondSubscriber.awaitIfNotUnsubscribed()

        firstSubscriber.assertError(TestException("first"))
        secondSubscriber.assertError(TestException("second"))
    }

    @Test
    fun testClearCacheAfterUnsubscribeFragment() {
        val subscriber = TestSubscriber<Int>()
        val fragment = runOnUIAndWait { RetainFactory.start(fragmentManager, rangeWithDelay(0, 5, SECONDS.toMillis(5)).bindToThread(), subscriber) }
        subscriber.awaitTerminalEvent(2, SECONDS)
        fragment.unsubscribe()

        subscriber.assertUnsubscribed()

        changeOrientationAndWait()

        val subscriber2 = TestSubscriber<Int>()
        runOnUIAndWait { RetainFactory.start(fragmentManager, null, subscriber2) }
        subscriber2.awaitTerminalEvent(2, SECONDS)
        subscriber2.unsubscribe()

        subscriber2.assertNoTerminalEvent()
    }

    @Test
    fun testClearErrorCacheAfterUnsubscribeFragment() {
        val subscriber = TestSubscriber<Int>()
        val fragment = runOnUIAndWait { RetainFactory.start(fragmentManager, errorObservable(TestException("cached")).bindToThread(), subscriber) }
        subscriber.awaitTerminalEvent(2, SECONDS)
        fragment.unsubscribe()

        subscriber.assertUnsubscribed()

        changeOrientationAndWait()

        val subscriber2 = TestSubscriber<Int>()
        runOnUIAndWait { RetainFactory.start(fragmentManager, null, subscriber2) }
        subscriber2.awaitTerminalEvent(2, SECONDS)
        subscriber2.unsubscribe()

        subscriber2.assertNoTerminalEvent()
    }


    @Test
    fun testUnsubscribeAfterFinished() {
        val testSubscriber = TestSubscriber<Long>()
        val scheduler = TestScheduler()
        val testObservable = TestSubject.create<Long>(scheduler)
        runOnUIAndWait { RetainFactory.start(fragmentManager, testObservable, testSubscriber) }
        testSubscriber.awaitTerminalEvent(2, SECONDS)

        solo.finishOpenedActivities()
        testSubscriber.assertUnsubscribed()
        Assert.assertFalse(testObservable.hasObservers())
    }

    @Test
    fun testComposeBinding() {
        var subscriber = TestSubscriber<Int>()
        val subscription = runOnUIAndWait {
            rangeWithDelay(0, 5, SECONDS.toMillis(5))
                    .bindToThread()
                    .compose(RetainFactory.bindToRetain(fragmentManager, "compose tag"))
                    .subscribe(subscriber)
        }

        subscriber.awaitTerminalEvent(1, SECONDS)

        changeOrientationAndWait()
        subscriber.assertUnsubscribed()
        Assert.assertTrue(subscription.isUnsubscribed)

        val secondSubscriber = TestSubscriber<Int>()

        runOnUIAndWait {
            rangeWithDelay(0, 2, SECONDS.toMillis(5))
                    .bindToThread()
                    .compose(RetainFactory.bindToRetain(fragmentManager, "compose tag"))
                    .subscribe(secondSubscriber)
        }


        secondSubscriber.awaitTerminalEvent()
        secondSubscriber.assertReceivedOnNext((0..4).toCollection(arrayListOf()))
    }

    @Test
    fun testErrorWithCompose() {
        var subscriber = TestSubscriber<Int>()
        val subscription = runOnUIAndWait {
            errorObservable(TestException("Expected exception"))
                    .bindToThread()
                    .compose(RetainFactory.bindToRetain(fragmentManager, "compose tag [2]"))
                    .subscribe(subscriber)
        }

        subscriber.awaitTerminalEvent(1, SECONDS)

        changeOrientationAndWait()
        subscriber.assertUnsubscribed()
        Assert.assertTrue(subscription.isUnsubscribed)

        val secondSubscriber = TestSubscriber<Int>()

        runOnUIAndWait {
            errorObservable(TestException("Unexpected exception"))
                    .bindToThread()
                    .compose(RetainFactory.bindToRetain(fragmentManager, "compose tag [2]"))
                    .subscribe(secondSubscriber)
        }

        secondSubscriber.awaitTerminalEvent()
        secondSubscriber.assertError(TestException("Expected exception"))
    }

    private fun changeOrientationAndWait() {
        device.setOrientationLeft()
        device.waitForWindowUpdate(basePackage, SECONDS.toMillis(5))
    }

    fun <T> runOnUIAndWait(func: () -> T): T {
        var result: T? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = func() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        return result!!
    }

    @After
    fun tearDown() {
        solo.finishOpenedActivities()
    }

    private fun clearFragments() {
        val fragment = fragmentManager.findFragmentByTag("RX_RETAIN_FRAGMENT_INSTANCE")
        if (fragment != null) {
            fragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commit()
            runOnUIAndWait { fragmentManager.executePendingTransactions() }
        }
    }
}