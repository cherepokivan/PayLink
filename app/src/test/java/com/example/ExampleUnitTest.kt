package com.example

import com.example.data.banks.BankCatalog
import com.example.data.payment.SBPPaymentLinkProviderImpl
import com.example.data.payment.YooMoneyUrlBuilder
import com.example.domain.model.PaymentMethod
import com.example.domain.model.Recipient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class ExampleUnitTest {

    @Test
    fun testYooMoneyUrl_ExactFormat() {
        val url = YooMoneyUrlBuilder.buildUrl(
            wallet = "410011234567890",
            amount = 500,
            comment = "Za pokupku"
        )

        val expected = "https://yoomoney.ru/quickpay/confirm.xml?receiver=410011234567890&quickpay-form=shop&targets=Za+pokupku&paymentType=SB&sum=500"
        assertEquals(expected, url)
    }

    @Test
    fun testYooMoneyUrl_CyrillicAndSpecialCharactersEncoding() {
        val comment = "За покупку & подарок №1 #2 100%?"
        val url = YooMoneyUrlBuilder.buildUrl(
            wallet = "410011234567890",
            amount = 1250,
            comment = comment
        )

        assertTrue(url.startsWith("https://yoomoney.ru/quickpay/confirm.xml?"))
        assertTrue(url.contains("receiver=410011234567890"))
        assertTrue(url.contains("quickpay-form=shop"))
        assertTrue(url.contains("paymentType=SB"))
        assertTrue(url.contains("sum=1250"))

        // Проверяем, что нет сырых пробелов или неэкранированных символов
        assertFalse(url.contains(" "))
        assertFalse(url.contains("№"))

        // Извлекаем параметр targets и декодируем
        val targetsParam = url.substringAfter("&targets=").substringBefore("&")
        val decoded = URLDecoder.decode(targetsParam, StandardCharsets.UTF_8.name())
        assertEquals(comment, decoded)
    }

    @Test
    fun testYooMoneyUrl_EmptyComment() {
        val url = YooMoneyUrlBuilder.buildUrl(
            wallet = "410011234567890",
            amount = 300,
            comment = null
        )

        assertEquals(
            "https://yoomoney.ru/quickpay/confirm.xml?receiver=410011234567890&quickpay-form=shop&paymentType=SB&sum=300",
            url
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testYooMoneyUrl_ZeroAmountThrows() {
        YooMoneyUrlBuilder.buildUrl(
            wallet = "410011234567890",
            amount = 0
        )
    }

    @Test
    fun testBankCatalog_SearchAndFind() {
        val tbank = BankCatalog.findById("100000000004")
        assertNotNull(tbank)
        assertEquals("Т-Банк", tbank?.name)

        val sberSearchResults = BankCatalog.search("сбер")
        assertTrue(sberSearchResults.any { it.id == "10000000111" || it.name.contains("Сбер") })
    }

    @Test
    fun testRecipientMasking() {
        val recipient = Recipient(
            yoomoneyWallet = "410011234567890",
            phoneNumber = "+79991234567",
            bankId = "100000000004",
            bankName = "Т-Банк"
        )

        assertEquals("•••• 7890", recipient.maskedYooMoneyWallet)
        assertEquals("+7 ••• •••-45-67", recipient.maskedPhoneNumber)
        assertTrue(recipient.isConfiguredFor(PaymentMethod.YOOMONEY))
        assertTrue(recipient.isConfiguredFor(PaymentMethod.SBP))
    }

    @Test
    fun testSbpProvider_GeneratesRealLink() = runBlocking {
        val sbpProvider = SBPPaymentLinkProviderImpl()
        val result = sbpProvider.createPaymentLink(
            amount = 1500,
            phoneNumber = "+79991234567",
            bankId = "100000000004",
            comment = "Обед в кафе"
        )

        assertTrue(result.isSuccess)
        val link = result.getOrThrow()
        assertTrue(link.startsWith("https://qr.nspk.ru/transfer?"))
        assertTrue(link.contains("phone=79991234567"))
        assertTrue(link.contains("sum=1500"))
        assertTrue(link.contains("bank=100000000004"))
    }
}
