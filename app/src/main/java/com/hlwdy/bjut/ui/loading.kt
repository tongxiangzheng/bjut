package com.hlwdy.bjut.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.hlwdy.bjut.R

class LoadingDialog private constructor(context: Context) {
    private val dialog: Dialog = Dialog(context, R.style.TransparentDialog)

    init {
        dialog.setContentView(R.layout.dialog_loading)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    companion object {
        private var instance: LoadingDialog? = null

        @Synchronized
        fun getInstance(context: Context): LoadingDialog {
            if (instance == null) {
                instance = LoadingDialog(context)
            }
            return instance!!
        }
    }

    fun show() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}