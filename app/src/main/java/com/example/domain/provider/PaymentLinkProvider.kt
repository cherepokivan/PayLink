package com.example.domain.provider

import com.example.domain.model.Recipient

interface PaymentLinkProvider {
    suspend fun createPaymentLink(
        amount: Long,
        comment: String?,
        recipient: Recipient
    ): Result<String>
}

interface SBPPaymentLinkProvider : PaymentLinkProvider {
    suspend fun createPaymentLink(
        amount: Long,
        phoneNumber: String,
        bankId: String,
        comment: String?
    ): Result<String>

    override suspend fun createPaymentLink(
        amount: Long,
        comment: String?,
        recipient: Recipient
    ): Result<String> {
        val phone = recipient.phoneNumber
        val bank = recipient.bankId
        if (phone.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Укажите номер телефона получателя для СБП"))
        }
        if (bank.isNullOrBlank()) {
            return Result.failure(IllegalArgumentException("Выберите банк получателя для СБП"))
        }
        return createPaymentLink(amount, phone, bank, comment)
    }
}
