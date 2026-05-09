package com.suyash.mockcivilaviationexam.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.suyash.mockcivilaviationexam.data.billing.BillingManager
import com.suyash.mockcivilaviationexam.data.billing.SubscriptionRepository
import kotlinx.coroutines.flow.StateFlow

class SubscriptionViewModel(
    private val billingManager: BillingManager,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    val subscriptionState: StateFlow<BillingManager.SubscriptionState> =
        billingManager.subscriptionState

    val productDetails = billingManager.productDetails

    fun isSubscribed(): Boolean = subscriptionState.value.isSubscribed

    fun canTakeFreeExam(sectionId: String): Boolean {
        if (isSubscribed()) return true
        return subscriptionRepository.canTakeFreeExam(sectionId)
    }

    fun getRemainingFreeExams(sectionId: String): Int {
        if (isSubscribed()) return Int.MAX_VALUE
        return subscriptionRepository.getRemainingFreeExams(sectionId)
    }

    fun recordExamTaken(sectionId: String) {
        subscriptionRepository.recordExamTaken(sectionId)
    }

    fun launchPurchase(activity: Activity): Boolean {
        return billingManager.launchPurchaseFlow(activity)
    }

    fun clearError() {
        billingManager.clearError()
    }

    fun refreshSubscription() {
        billingManager.querySubscriptionStatus()
    }
}
