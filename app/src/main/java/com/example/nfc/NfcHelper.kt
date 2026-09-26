package com.example.nfc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings
import android.util.Log

sealed class NfcStatus {
    object SupportedAndEnabled : NfcStatus()
    object Disabled : NfcStatus()
    object NotSupported : NfcStatus()
}

object NfcHelper {
    private const val TAG = "NfcHelper"

    fun checkNfcStatus(context: Context): NfcStatus {
        val adapter = try {
            NfcAdapter.getDefaultAdapter(context)
        } catch (e: Exception) {
            null
        } ?: return NfcStatus.NotSupported

        return if (adapter.isEnabled) {
            NfcStatus.SupportedAndEnabled
        } else {
            NfcStatus.Disabled
        }
    }

    fun getNfcSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NFC_SETTINGS)
    }

    fun startTransfer(activity: Activity, url: String, onCompleted: (Boolean) -> Unit) {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter == null || !adapter.isEnabled) {
            onCompleted(false)
            return
        }

        // Устанавливаем NDEF URL для HostApduService (HCE Type 4 Tag)
        PayLinkApduService.currentUri = url
        PayLinkApduService.onTransferCompleteListener = { success ->
            activity.runOnUiThread {
                onCompleted(success)
            }
        }
        Log.d(TAG, "NFC HCE active with URL: $url")
    }

    fun stopTransfer(activity: Activity) {
        PayLinkApduService.currentUri = null
        PayLinkApduService.onTransferCompleteListener = null
        Log.d(TAG, "NFC HCE stopped")
    }
}
