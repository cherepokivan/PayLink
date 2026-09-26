package com.example.data.payment

import com.example.domain.model.Recipient
import com.example.domain.provider.PaymentLinkProvider

class YooMoneyPaymentLinkProviderImpl : PaymentLinkProvider {

    override suspend fun createPaymentLink(
        amount: Long,
        comment: String?,
        recipient: Recipient
    ): Result<String> {
        val wallet = recipient.yoomoneyWallet
        if (wallet.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Укажите кошелёк ЮMoney в настройках получателя"))
        }
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Сумма должна быть больше нуля"))
        }

        return try {
            val url = YooMoneyUrlBuilder.buildUrl(
                wallet = wallet,
                amount = amount,
                comment = comment
            )
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
