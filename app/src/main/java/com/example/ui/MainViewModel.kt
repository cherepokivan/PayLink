package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.payment.PaymentLinkProviderFactory
import com.example.domain.model.PaymentMethod
import com.example.domain.model.PaymentRequest
import com.example.domain.model.Recipient
import com.example.domain.repository.AppSettings
import com.example.domain.repository.RecipientRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    object Payment : Screen()
    object RecipientSettings : Screen()
    object Settings : Screen()
}

class MainViewModel(
    private val recipientRepository: RecipientRepository,
    private val settingsRepository: SettingsRepository,
    private val paymentLinkProviderFactory: PaymentLinkProviderFactory
) : ViewModel() {

    val recipientState: StateFlow<Recipient> = recipientRepository.recipientFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Recipient()
        )

    val settingsState: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedMethod = MutableStateFlow(PaymentMethod.YOOMONEY)
    val selectedMethod: StateFlow<PaymentMethod> = _selectedMethod.asStateFlow()

    private val _currentPaymentRequest = MutableStateFlow<PaymentRequest?>(null)
    val currentPaymentRequest: StateFlow<PaymentRequest?> = _currentPaymentRequest.asStateFlow()

    private val _paymentError = MutableStateFlow<String?>(null)
    val paymentError: StateFlow<String?> = _paymentError.asStateFlow()

    private val _hasPromptedFirstLaunch = MutableStateFlow(false)
    val hasPromptedFirstLaunch: StateFlow<Boolean> = _hasPromptedFirstLaunch.asStateFlow()

    init {
        // Проверяем первый запуск: если данных нет, перенаправляем на настройку получателя
        viewModelScope.launch {
            recipientRepository.recipientFlow.collect { recipient ->
                if (!recipient.isAnyConfigured && !_hasPromptedFirstLaunch.value) {
                    _hasPromptedFirstLaunch.value = true
                    _currentScreen.value = Screen.RecipientSettings
                }
            }
        }
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _selectedMethod.value = method
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun createPayment(amount: Long, comment: String?) {
        viewModelScope.launch {
            val recipient = recipientState.value
            val method = selectedMethod.value
            val provider = paymentLinkProviderFactory.getProvider(method)

            val result = provider.createPaymentLink(amount, comment, recipient)
            result.onSuccess { url ->
                _currentPaymentRequest.value = PaymentRequest(
                    amount = amount,
                    comment = comment,
                    paymentMethod = method,
                    paymentUrl = url
                )
                _paymentError.value = null
                _currentScreen.value = Screen.Payment
            }.onFailure { error ->
                _paymentError.value = error.message ?: "Ошибка формирования ссылки"
            }
        }
    }

    fun saveRecipient(recipient: Recipient) {
        viewModelScope.launch {
            recipientRepository.saveRecipient(recipient)
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDynamicColors(enabled)
        }
    }

    class Factory(
        private val recipientRepository: RecipientRepository,
        private val settingsRepository: SettingsRepository,
        private val paymentLinkProviderFactory: PaymentLinkProviderFactory
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(recipientRepository, settingsRepository, paymentLinkProviderFactory) as T
        }
    }
}
