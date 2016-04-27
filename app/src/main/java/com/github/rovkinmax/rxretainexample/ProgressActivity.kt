package com.github.rovkinmax.rxretainexample

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import com.github.rovkinmax.rxretain.EmptySubscriber
import com.github.rovkinmax.rxretain.RetainFactory
import com.github.rovkinmax.rxretain.RetainWrapper
import com.github.rovkinmax.rxretainexample.test.rangeWithDelay
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @author Rovkin Max
 */
class ProgressActivity : Activity() {
    private var dialog: ProgressDialog? = null
    private var wrapper: RetainWrapper<Int>? = null

    val observable = rangeWithDelay(0, 100, TimeUnit.SECONDS.toMillis(120))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val button = Button(this).apply {
            text = "click"
        }
        setContentView(button)
        button.setOnClickListener {
            wrapper?.subscribe()
        }

    }

    override fun onResume() {
        super.onResume()
        wrapper = RetainFactory.create(fragmentManager, observable, object : EmptySubscriber<Int>() {
            override fun onStart() {
                dialog = ProgressDialog(this@ProgressActivity)
                dialog!!.show()
            }

            override fun onNext(t: Int) {
                if (dialog?.isShowing ?: false)
                    dialog?.progressBar?.progress = t
            }

            override fun onCompleted() {
                hideDialog()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        hideDialog()
    }

    private fun hideDialog() {
        dialog?.dismiss()
    }
}


class ProgressDialog(context: Context) : Dialog(context) {
    lateinit var progressBar: ProgressBar

    init {
        progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        progressBar.max = 100
        setContentView(progressBar)
    }
}