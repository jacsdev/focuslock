package com.developermind.focuslock

import android.app.Application
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
    }
}
