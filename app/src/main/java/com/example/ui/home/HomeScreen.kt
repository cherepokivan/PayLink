package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PaymentMethod
import com.example.domain.model.Recipient
import com.example.ui.theme.SbpBlue
import com.example.ui.theme.YooMoneyPurple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    recipient: Recipient,
    selectedMethod: PaymentMethod,
    onMethodChange: (PaymentMethod) -> Unit,
    onSubmitPayment: (amount: Long, comment: String?) -> Unit,
    onNavigateToRecipient: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var commentInput by remember { mutableStateOf("") }
    var isMethodMenuExpanded by remember { mutableStateOf(false) }

    var validationError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Получить",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    // Переключатель метода оплаты в правом верхнем углу
                    Box {
                        val methodBadgeColor = when (selectedMethod) {
                            PaymentMethod.YOOMONEY -> YooMoneyPurple
                            PaymentMethod.SBP -> SbpBlue
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(methodBadgeColor.copy(alpha = 0.12f))
                                .clickable { isMethodMenuExpanded = true }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("payment_method_selector")
                        ) {
                            Text(
                                text = selectedMethod.displayName,
                                fontWeight = FontWeight.Bold,
                                color = methodBadgeColor,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Выбрать способ оплаты",
                                tint = methodBadgeColor
                            )
                        }

                        DropdownMenu(
                            expanded = isMethodMenuExpanded,
                            onDismissRequest = { isMethodMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = YooMoneyPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = PaymentMethod.YOOMONEY.displayName,
                                            fontWeight = if (selectedMethod == PaymentMethod.YOOMONEY) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    onMethodChange(PaymentMethod.YOOMONEY)
                                    isMethodMenuExpanded = false
                                    validationError = null
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = SbpBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = PaymentMethod.SBP.displayName,
                                            fontWeight = if (selectedMethod == PaymentMethod.SBP) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    onMethodChange(PaymentMethod.SBP)
                                    isMethodMenuExpanded = false
                                    validationError = null
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Поле ввода суммы
            Text(
                text = "Сумма к получению",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }
                    amountInput = digits.take(7) // до 9 999 999 руб
                    validationError = null
                },
                placeholder = {
                    Text(
                        text = "500 ₽",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    if (amountInput.isNotEmpty()) {
                        IconButton(onClick = { amountInput = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Очистить сумму"
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input_field")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Быстрые чипы для сумм (+100, +500, +1000, +5000)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(100, 500, 1000, 3000, 5000).forEach { quickAmount ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            val current = amountInput.toLongOrNull() ?: 0L
                            amountInput = (current + quickAmount).toString()
                            validationError = null
                        },
                        label = { Text("+$quickAmount ₽") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Поле комментария
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Комментарий (необязательно)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${commentInput.length}/200",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (commentInput.length >= 200) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = commentInput,
                    onValueChange = {
                        if (it.length <= 200) {
                            commentInput = it
                        }
                    },
                    placeholder = { Text("За покупку, заказ или подарок") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        if (commentInput.isNotEmpty()) {
                            IconButton(onClick = { commentInput = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Очистить комментарий"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("comment_input_field")
                )
            }

            // Ошибка валидации
            if (validationError != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Большая кнопка: Получить деньги
            Button(
                onClick = {
                    val amount = amountInput.toLongOrNull() ?: 0L

                    // 13. Валидация
                    if (amount <= 0L) {
                        validationError = "Введите сумму"
                        return@Button
                    }

                    when (selectedMethod) {
                        PaymentMethod.YOOMONEY -> {
                            if (!recipient.isYooMoneyConfigured) {
                                validationError = "Укажите кошелёк ЮMoney в настройках"
                                scope.launch {
                                    snackbarHostState.showSnackbar("Укажите кошелёк ЮMoney")
                                }
                                return@Button
                            }
                        }
                        PaymentMethod.SBP -> {
                            if (recipient.phoneNumber.isNullOrBlank()) {
                                validationError = "Укажите номер телефона для СБП"
                                scope.launch {
                                    snackbarHostState.showSnackbar("Укажите номер телефона")
                                }
                                return@Button
                            }
                            if (recipient.bankId.isNullOrBlank()) {
                                validationError = "Выберите банк для СБП"
                                scope.launch {
                                    snackbarHostState.showSnackbar("Выберите банк")
                                }
                                return@Button
                            }
                        }
                    }

                    validationError = null
                    onSubmitPayment(amount, commentInput.trim().ifBlank { null })
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("get_money_button")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Получить деньги",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Карточка сохранённого получателя под кнопкой
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onNavigateToRecipient)
                    .testTag("home_recipient_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Получатель",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        when (selectedMethod) {
                            PaymentMethod.YOOMONEY -> {
                                Text(
                                    text = "ЮMoney: ${recipient.maskedYooMoneyWallet}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (recipient.isYooMoneyConfigured) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                            PaymentMethod.SBP -> {
                                Text(
                                    text = if (recipient.isSbpConfigured) {
                                        "СБП: ${recipient.maskedPhoneNumber} • ${recipient.bankName}"
                                    } else {
                                        "СБП: Не настроен (укажите телефон и банк)"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (recipient.isSbpConfigured) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Изменить получателя",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
