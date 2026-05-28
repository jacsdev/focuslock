package com.developermind.focuslock

import android.app.Application
import android.content.Intent
import android.os.Build
import com.developermind.focuslock.data.billing.BillingClientWrapper
import com.developermind.focuslock.data.billing.DonationRepository
import com.developermind.focuslock.service.KeepAliveService
import com.developermind.focuslock.util.LocaleManager

class FocusLockApplication : Application() {

    val billingClientWrapper: BillingClientWrapper by lazy { BillingClientWrapper(this) }
    val donationRepository: DonationRepository by lazy { DonationRepository(billingClientWrapper) }

    override fun onCreate() {
        super.onCreate()
        val savedTag = LocaleManager(this).getSavedLanguageTag()
        LocaleManager.applyLocale(savedTag)
        startKeepAlive()
    }

    private fun startKeepAlive() {
        try {
            val intent = Intent(this, KeepAliveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            // ForegroundServiceStartNotAllowedException (API 31+) if the process starts
            // in a context where foreground services are restricted — the AccessibilityService
            // callback in onServiceConnected() will retry.
        }
    }
}
