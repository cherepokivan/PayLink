package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.local.PayLinkDataStore
import com.example.data.payment.PaymentLinkProviderFactory
import com.example.domain.repository.ThemeMode
import com.example.ui.home.HomeScreen
import com.example.ui.MainViewModel
import com.example.ui.payment.PaymentScreen
import com.example.ui.Screen
import com.example.ui.recipient.RecipientScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.PayLinkTheme

class MainActivity : ComponentActivity() {

    private val dataStore by lazy { PayLinkDataStore(applicationContext) }
    private val providerFactory by lazy { PaymentLinkProviderFactory(dataStore) }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(dataStore, dataStore, providerFactory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settingsState.collectAsState()
            val recipient by viewModel.recipientState.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val selectedMethod by viewModel.selectedMethod.collectAsState()
            val currentPayment by viewModel.currentPaymentRequest.collectAsState()

            val isDarkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            PayLinkTheme(
                darkTheme = isDarkTheme,
                dynamicColor = settings.dynamicColors
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        is Screen.Home -> {
                            HomeScreen(
                                recipient = recipient,
                                selectedMethod = selectedMethod,
                                onMethodChange = { viewModel.setPaymentMethod(it) },
                                onSubmitPayment = { amount, comment ->
                                    viewModel.createPayment(amount, comment)
                                },
                                onNavigateToRecipient = { viewModel.navigateTo(Screen.RecipientSettings) },
                                onNavigateToSettings = { viewModel.navigateTo(Screen.Settings) }
                            )
                        }

                        is Screen.Payment -> {
                            val payment = currentPayment
                            if (payment?.paymentUrl != null) {
                                PaymentScreen(
                                    amount = payment.amount,
                                    comment = payment.comment,
                                    paymentMethod = payment.paymentMethod,
                                    paymentUrl = payment.paymentUrl,
                                    recipient = recipient,
                                    onNavigateBack = { viewModel.navigateTo(Screen.Home) }
                                )
                            } else {
                                viewModel.navigateTo(Screen.Home)
                            }
                        }

                        is Screen.RecipientSettings -> {
                            val isFirstTime = !recipient.isAnyConfigured
                            RecipientScreen(
                                currentRecipient = recipient,
                                isFirstLaunch = isFirstTime,
                                onSaveRecipient = { updated ->
                                    viewModel.saveRecipient(updated)
                                    viewModel.navigateTo(Screen.Home)
                                },
                                onNavigateBack = { viewModel.navigateTo(Screen.Home) }
                            )
                        }

                        is Screen.Settings -> {
                            SettingsScreen(
                                recipient = recipient,
                                settings = settings,
                                onNavigateBack = { viewModel.navigateTo(Screen.Home) },
                                onNavigateToRecipient = { viewModel.navigateTo(Screen.RecipientSettings) },
                                onThemeModeChange = { viewModel.setThemeMode(it) },
                                onDynamicColorsChange = { viewModel.setDynamicColors(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
