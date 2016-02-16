package com.github.rovkinmax.rxretainexample

import com.github.rovkinmax.rxretainexample.test.TestException
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @author Rovkin Max
 */
@RunWith(JUnit4::class)
class UtilTest {
    @Test
    fun testEqualsTestException() {
        val one = TestException("Expected")
        val two = TestException("Expected")
        Assert.assertEquals(one, two)
    }

    @Test
    fun testNotEqualsTestException() {
        val one = TestException("Expected")
        val two = TestException("Unexpected")
        Assert.assertNotEquals(one, two)
    }
}