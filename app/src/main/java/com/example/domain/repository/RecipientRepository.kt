package com.example.domain.repository

import com.example.domain.model.Recipient
import kotlinx.coroutines.flow.Flow

interface RecipientRepository {
    val recipientFlow: Flow<Recipient>
    suspend fun saveRecipient(recipient: Recipient)
    suspend fun clearRecipient()
}
