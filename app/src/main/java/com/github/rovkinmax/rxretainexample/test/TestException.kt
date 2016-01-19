package com.github.rovkinmax.rxretainexample.test

/**
 * @author Rovkin Max
 */
class TestException(detailMessage: String?) : RuntimeException(detailMessage) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        if (other !is TestException) return false
        if (!(message?.equals(other.message) ?: false)) return false
        return true
    }

    override fun hashCode(): Int {
        return message?.hashCode() ?: 0
    }
}