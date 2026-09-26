package com.example.data.payment

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object YooMoneyUrlBuilder {
    private const val BASE_URL = "https://yoomoney.ru/quickpay/confirm.xml"
    const val MAX_COMMENT_LENGTH = 200

    fun buildUrl(
        wallet: String,
        amount: Long,
        comment: String? = null
    ): String {
        val trimmedWallet = wallet.trim()
        require(trimmedWallet.isNotBlank()) { "Укажите номер кошелька ЮMoney" }
        require(amount > 0) { "Сумма должна быть больше нуля" }

        val cleanWallet = trimmedWallet.filter { it.isDigit() }
        val finalWallet = if (cleanWallet.isNotEmpty()) cleanWallet else trimmedWallet
        val encodedWallet = URLEncoder.encode(finalWallet, StandardCharsets.UTF_8.name())
        val formattedSum = amount.toString()

        val sb = StringBuilder(BASE_URL)
        sb.append("?receiver=").append(encodedWallet)
        sb.append("&quickpay-form=shop")

        val sanitizedComment = comment?.trim()?.take(MAX_COMMENT_LENGTH)
        if (!sanitizedComment.isNullOrEmpty()) {
            val encodedTargets = URLEncoder.encode(sanitizedComment, StandardCharsets.UTF_8.name())
            sb.append("&targets=").append(encodedTargets)
        }

        sb.append("&paymentType=SB")
        sb.append("&sum=").append(formattedSum)

        return sb.toString()
    }
}
