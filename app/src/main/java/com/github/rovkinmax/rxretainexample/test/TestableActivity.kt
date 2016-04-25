package com.github.rovkinmax.rxretainexample.test

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import java.util.*

/**
 * @author Rovkin Max
 */
open class TestableActivity : Activity() {
    val listMethods = ArrayList<String>()
    private lateinit var button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        button = Button(this)
        button.tag = "button"
        setContentView(button)
        addMethod("onCreate()")
    }

    override fun onStart() {
        super.onStart()
        addMethod("onStart()")
    }

    override fun onResume() {
        super.onResume()
        addMethod("onResume()")
    }

    override fun onPause() {
        super.onPause()
        addMethod("onPause()")
    }

    override fun onStop() {
        super.onStop()
        addMethod("onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        addMethod("onDestroy()")
    }

    private fun addMethod(string: String) {
        listMethods.add(string)
    }

    fun performClick(function: () -> Unit) {
        button.setOnClickListener { function() }
        button.performClick()
    }
}