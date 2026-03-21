package com.quickbite.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuickBiteApplication : Application() {
    
    companion object {
        const val BASE_URL = "http://10.0.2.2:8080/api/"
        const val PREF_NAME = "quickbite_prefs"
        const val KEY_TOKEN = "jwt_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_ROLE = "user_role"
    }
}
