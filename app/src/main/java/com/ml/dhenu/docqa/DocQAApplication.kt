package com.ml.dhenu.docqa

import android.app.Application
import com.ml.dhenu.docqa.data.ObjectBoxStore
import com.ml.dhenu.docqa.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class DocQAApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DocQAApplication)
            modules(AppModule().module)
        }
        ObjectBoxStore.init(this)
    }
}
