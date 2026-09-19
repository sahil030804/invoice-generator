package com.kjbilling.app

import android.app.Application
import com.kjbilling.app.di.AppContainer

class KJInvoiceApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
