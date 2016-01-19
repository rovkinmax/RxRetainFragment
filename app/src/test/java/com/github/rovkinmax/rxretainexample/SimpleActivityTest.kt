package com.github.rovkinmax.rxretainexample

import android.app.Activity
import android.app.FragmentManager
import com.github.rovkinmax.rxretain.EmptyObserver
import com.github.rovkinmax.rxretain.RxRetainFactory
import com.github.rovkinmax.rxretain.RxRetainFragment
import com.github.rovkinmax.rxretainexample.test.TestException
import com.github.rovkinmax.rxretainexample.test.bindToThread
import com.github.rovkinmax.rxretainexample.test.delayInThread
import com.github.rovkinmax.rxretainexample.test.rangeWithDelay
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ActivityController
import rx.Observable
import rx.Subscriber
import rx.exceptions.OnErrorNotImplementedException
import rx.observers.TestObserver
import rx.observers.TestSubscriber
import java.util.*
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
    public fun testRunByEmptySubscribe() {
        val firstSubscriber = TestSubscriber<Int>()
        createFragmentWithTimer("first", firstSubscriber).subscribe()
        firstSubscriber.assertCompleted()
        firstSubscriber.assertReceivedOnNext((0..9).toArrayList())
    }

    @Test
    public fun testErrorWithEmptySubscribeMethod() {
        val firstSubscriber = TestSubscriber<Int>()
        createFragmentWithOnErrorAction("second", firstSubscriber, TestException("Expected exception")).subscribe()
        firstSubscriber.assertError(TestException("Expected exception"))
    }

    @Test
    public fun testRunBySubscribeWithOnNextAction() {
        val list = ArrayList<Int>()
        createFragmentWithTimer("first").subscribe({ list.add(it) })
        Assert.assertEquals((0..9).toArrayList(), list)
    }

    @Test
    fun testErrorWithSubscribeOnNextAction() {
        var error: Throwable? = null
        try {
            createFragmentWithOnErrorAction("second", exception = TestException("Expected exception"))
                    .subscribe({ })
        } catch(e: OnErrorNotImplementedException) {
            error = e
        }
        Assert.assertNotNull(error)
    }

    @Test
    public fun testRunBySubscribeWithThreeCallbacks() {
        var isCompleted = false
        createFragmentWithTimer("second").subscribe({}, {}, { isCompleted = true })
        Assert.assertTrue(isCompleted)
    }

    @Test
    fun testErrorWithSubscribeByTreeCallbacks() {
        var isOnNextCalled = false
        var isCompleted = false
        var exception: Throwable? = null

        createFragmentWithOnErrorAction("second", exception = TestException("Expected exception"))
                .subscribe({ isOnNextCalled = true }, { exception = it }, { isCompleted = true })

        Assert.assertFalse(isOnNextCalled)
        Assert.assertFalse(isCompleted)
        Assert.assertEquals(TestException("Expected exception"), exception)
    }

    @Test
    public fun testRunBySubscribeWithObserver() {
        val testObserver = TestObserver<Int>()
        createFragmentWithTimer("first").subscribe(testObserver)
        testObserver.assertTerminalEvent()
        testObserver.assertReceivedOnNext((0..9).toArrayList())
    }

    @Test
    fun testErrorWithObserverSubscription() {
        val testObserver = TestObserver<Int>()
        createFragmentWithOnErrorAction("second", exception = TestException("Expected exception"))
                .subscribe(testObserver)
        testObserver.assertTerminalEvent()
        Assert.assertEquals(arrayListOf(TestException("Expected exception")), testObserver.onErrorEvents)
    }

    @Test
    public fun testRunBySubscribeWithSubscriber() {
        val testSubscriber = TestSubscriber<Int>()
        createFragmentWithTimer("first").subscribe(testSubscriber)
        testSubscriber.assertCompleted()
        testSubscriber.assertReceivedOnNext((0..9).toArrayList())
    }


    @Test
    fun testErrorWithSubscriberSubscription() {
        val testSubscriber = TestSubscriber<Int>()
        createFragmentWithOnErrorAction("second", exception = TestException("Expected exception"))
                .subscribe(testSubscriber)
        testSubscriber.assertTerminalEvent()
        testSubscriber.assertError(TestException("Expected exception"))
    }

    private fun createFragmentWithTimer(tag: String, subscriber: Subscriber<Int> = EmptyObserver<Int>()): RxRetainFragment<Int> {
        return RxRetainFactory.create(fragmentManager, Observable.range(0, 10), subscriber, tag)
    }

    private fun createFragmentWithOnErrorAction(tag: String, subscriber: Subscriber<Int> = EmptyObserver<Int>(), exception: Throwable): RxRetainFragment<Int> {
        return RxRetainFactory.create(fragmentManager, Observable.create { throw exception }, subscriber, tag)
    }

    @Test
    fun testStartObservable() {

        var testSubscriber = TestSubscriber<Long>()
        RxRetainFactory.start(fragmentManager, Observable.timer(5, TimeUnit.SECONDS).bindToThread(), testSubscriber)
        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)

        val spySubscribers = Mockito.spy(TestSubscriber<Long>())
        RxRetainFactory.start(fragmentManager, Observable.range(0, 10).map { it.toLong() }.bindToThread(), spySubscribers)
        testSubscriber.assertUnsubscribed()

        if (!spySubscribers.isUnsubscribed)
            spySubscribers.awaitTerminalEvent()

        Mockito.verify(spySubscribers).onStart()
        spySubscribers.assertCompleted()
        spySubscribers.assertValueCount(1)
        spySubscribers.assertReceivedOnNext(arrayListOf(0.toLong()))
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

    @Test
    fun testStartWithTwoDifferentTags() {
        val firstSubscriber = TestSubscriber<Long>()
        RxRetainFactory.start(fragmentManager, Observable.timer(5, TimeUnit.SECONDS).bindToThread(), firstSubscriber, "first tag")
        firstSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)

        val secondSubscriber = TestSubscriber<Long>()
        RxRetainFactory.start(fragmentManager, Observable.timer(5, TimeUnit.SECONDS).bindToThread(), secondSubscriber, "second tag")
        secondSubscriber.awaitTerminalEvent()
        if (!firstSubscriber.isUnsubscribed) {
            firstSubscriber.awaitTerminalEvent()
        }

        firstSubscriber.assertCompleted()
        secondSubscriber.assertCompleted()
    }

    @Test
    fun testFragmentUnsubscribeMethod() {
        val testSubscriber = TestSubscriber<Long>()
        val fragment = RxRetainFactory.start(fragmentManager, Observable.timer(50, TimeUnit.SECONDS).bindToThread(), testSubscriber)

        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)
        fragment.unsubscribe()

        testSubscriber.assertUnsubscribed()
        testSubscriber.assertNoTerminalEvent()
    }

    @Test
    fun testClearCacheAfterFragmentUnsubscribe() {
        val testSubscriber = TestSubscriber<Int>()
        val fragment = RxRetainFactory.start(fragmentManager, rangeWithDelay(0, 10).bindToThread(), testSubscriber)

        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)
        fragment.unsubscribe()

        testSubscriber.assertUnsubscribed()
        testSubscriber.assertNoTerminalEvent()

        val secondSubscriber = TestSubscriber<Int>()
        fragment.subscribe(secondSubscriber)
        secondSubscriber.awaitTerminalEvent()

        secondSubscriber.assertCompleted()
        secondSubscriber.assertReceivedOnNext((0..9).toArrayList())
    }


    @Test
    fun testCacheResultAfterUnsubscribeSubscription() {
        val testSubscriber = TestSubscriber<Int>()
        val fragment = RxRetainFactory.start(fragmentManager, rangeWithDelay(0, 5).bindToThread(), testSubscriber)

        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriber.unsubscribe()

        val secondSubscriber = TestSubscriber<Int>()
        fragment.subscribe(secondSubscriber)
        if (!secondSubscriber.isUnsubscribed) {
            secondSubscriber.awaitTerminalEvent()
        }

        testSubscriber.assertNoTerminalEvent()
        secondSubscriber.assertTerminalEvent()
        secondSubscriber.assertReceivedOnNext((0..4).toArrayList())
    }

    @Test
    fun testCacheErrorAfterUnsubscribeSubscription() {
        val testSubscriber = TestSubscriber<Int>()
        val fragment = RxRetainFactory.start(fragmentManager,
                Observable.create<Int> { delayInThread(5000) ;throw TestException("ha") }.bindToThread(), testSubscriber)

        testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)
        testSubscriber.unsubscribe()

        val secondSubscriber = TestSubscriber<Int>()
        fragment.subscribe(secondSubscriber)
        if (!secondSubscriber.isUnsubscribed) {
            secondSubscriber.awaitTerminalEvent()
        }

        testSubscriber.assertNoTerminalEvent()
        secondSubscriber.assertError(TestException("ha"))
    }
}