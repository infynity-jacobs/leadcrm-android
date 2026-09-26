package com.infynity.leadcrm

import android.app.Application
import com.infynity.leadcrm.core.common.AppContainer

class LeadCrmApplication : Application() {

    val appContainer: AppContainer by lazy {
        AppContainer(this)
    }
}
