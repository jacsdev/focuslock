package com.developermind.focuslock

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.developermind.focuslock.data.billing.BillingClientWrapper
import com.developermind.focuslock.data.billing.DonationRepository
import com.developermind.focuslock.util.LocaleManager

class FocusLockApplication : Application() {

    val billingClientWrapper: BillingClientWrapper by lazy { BillingClientWrapper(this) }
    val donationRepository: DonationRepository by lazy { DonationRepository(billingClientWrapper) }

    override fun onCreate() {
        super.onCreate()
        val savedTag = LocaleManager(this).getSavedLanguageTag()
        LocaleManager.applyLocale(savedTag)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "focuslock_monitor"
    }
}
