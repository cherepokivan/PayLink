package com.example.domain.model

enum class PaymentMethod(val displayName: String) {
    YOOMONEY("ЮMoney"),
    SBP("СБП")
}

enum class PaymentTransport(val displayName: String) {
    QR("QR-код"),
    NFC("NFC")
}

data class Recipient(
    val yoomoneyWallet: String? = null,
    val phoneNumber: String? = null,
    val bankId: String? = null,
    val bankName: String? = null
) {
    val isYooMoneyConfigured: Boolean
        get() = !yoomoneyWallet.isNullOrBlank()

    val isSbpConfigured: Boolean
        get() = !phoneNumber.isNullOrBlank() && !bankId.isNullOrBlank()

    val isFullyConfigured: Boolean
        get() = isYooMoneyConfigured && isSbpConfigured

    val isAnyConfigured: Boolean
        get() = isYooMoneyConfigured || isSbpConfigured

    fun isConfiguredFor(method: PaymentMethod): Boolean = when (method) {
        PaymentMethod.YOOMONEY -> isYooMoneyConfigured
        PaymentMethod.SBP -> isSbpConfigured
    }

    val maskedYooMoneyWallet: String
        get() {
            val wallet = yoomoneyWallet?.trim().orEmpty()
            return if (wallet.length > 4) {
                "•••• " + wallet.takeLast(4)
            } else if (wallet.isNotEmpty()) {
                wallet
            } else {
                "Не настроен"
            }
        }

    val maskedPhoneNumber: String
        get() {
            val digits = phoneNumber?.filter { it.isDigit() }.orEmpty()
            return if (digits.length >= 10) {
                val last4 = digits.takeLast(4)
                val part1 = last4.substring(0, 2)
                val part2 = last4.substring(2)
                "+7 ••• •••-$part1-$part2"
            } else if (phoneNumber.isNullOrBlank()) {
                "Не настроен"
            } else {
                phoneNumber
            }
        }
}

data class PaymentRequest(
    val amount: Long,
    val comment: String?,
    val paymentMethod: PaymentMethod,
    val paymentUrl: String?
)

data class Bank(
    val id: String,
    val name: String,
    val shortName: String = name,
    val isPopular: Boolean = false
)
