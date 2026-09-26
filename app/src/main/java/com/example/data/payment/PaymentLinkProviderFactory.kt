package com.example.data.payment

import com.example.domain.model.PaymentMethod
import com.example.domain.provider.PaymentLinkProvider
import com.example.domain.provider.SBPPaymentLinkProvider
import com.example.domain.repository.SettingsRepository

class PaymentLinkProviderFactory(
    private val settingsRepository: SettingsRepository? = null
) {
    val yooMoneyProvider: PaymentLinkProvider = YooMoneyPaymentLinkProviderImpl()
    val sbpProvider: SBPPaymentLinkProvider = SBPPaymentLinkProviderImpl(settingsRepository)

    fun getProvider(method: PaymentMethod): PaymentLinkProvider = when (method) {
        PaymentMethod.YOOMONEY -> yooMoneyProvider
        PaymentMethod.SBP -> sbpProvider
    }
}
