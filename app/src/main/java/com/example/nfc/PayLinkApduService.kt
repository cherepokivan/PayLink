package com.example.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.io.ByteArrayOutputStream
import java.util.Arrays

/**
 * Host-based Card Emulation (HCE) для эмуляции стандартного NFC Forum Type 4 Tag.
 *
 * Передаёт ТОЛЬКО NDEF URI record с платёжной ссылкой.
 * Никаких банковских данных, номеров карт, PIN или CVV.
 */
class PayLinkApduService : HostApduService() {

    companion object {
        private const val TAG = "PayLinkApduService"

        // AID: NFC Forum Type 4 Tag Application (D2 76 00 00 85 01 01)
        private val AID_NDEF = byteArrayOf(
            0xD2.toByte(), 0x76.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x85.toByte(), 0x01.toByte(), 0x01.toByte()
        )

        // Capability Container (CC) File ID: E1 03
        private val CC_FILE_ID = byteArrayOf(0xE1.toByte(), 0x03.toByte())

        // NDEF File ID: E1 04
        private val NDEF_FILE_ID = byteArrayOf(0xE1.toByte(), 0x04.toByte())

        // Status words
        private val STATUS_SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val STATUS_FAILED = byteArrayOf(0x6F.toByte(), 0x00.toByte())

        // Capability Container content (15 bytes standard Type 4 CC)
        private val CC_FILE = byteArrayOf(
            0x00, 0x0F, // CCLEN (15 bytes)
            0x20,       // Mapping Version 2.0
            0x00, 0x3B, // MLe (max R-APDU size 59 bytes)
            0x00, 0x34, // MLc (max C-APDU size 52 bytes)
            0x04,       // NDEF File Control TLV
            0x06,       // Length
            0xE1.toByte(), 0x04.toByte(), // File ID E104
            0x04, 0x00, // Max NDEF size 1024 bytes
            0x00,       // Read access: free
            0xFF.toByte() // Write access: disabled
        )

        @Volatile
        var currentUri: String? = null
            set(value) {
                field = value
                updateNdefPayload(value)
            }

        @Volatile
        private var ndefFilePayload: ByteArray = byteArrayOf()

        var onTransferCompleteListener: ((Boolean) -> Unit)? = null

        private fun updateNdefPayload(uri: String?) {
            if (uri.isNullOrBlank()) {
                ndefFilePayload = byteArrayOf()
                return
            }
            try {
                val record = NdefRecord.createUri(uri)
                val msg = NdefMessage(record)
                val msgBytes = msg.toByteArray()

                // NDEF File begins with 2-byte length (NLEN) followed by NDEF message bytes
                val nlen = msgBytes.size
                val out = ByteArrayOutputStream()
                out.write((nlen shr 8) and 0xFF)
                out.write(nlen and 0xFF)
                out.write(msgBytes)
                ndefFilePayload = out.toByteArray()
                Log.d(TAG, "NDEF payload updated for URL: $uri (len=${msgBytes.size})")
            } catch (e: Exception) {
                Log.e(TAG, "Error building NDEF message", e)
                ndefFilePayload = byteArrayOf()
            }
        }
    }

    private var selectedFile: ByteArray? = null

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null || commandApdu.size < 4) {
            return STATUS_FAILED
        }

        val cla = commandApdu[0]
        val ins = commandApdu[1]
        val p1 = commandApdu[2]
        val p2 = commandApdu[3]

        // SELECT command (INS = A4)
        if (ins == 0xA4.toByte()) {
            if (p1 == 0x04.toByte()) {
                // Select by AID
                val lc = commandApdu[4].toInt() and 0xFF
                if (commandApdu.size >= 5 + lc) {
                    val aid = Arrays.copyOfRange(commandApdu, 5, 5 + lc)
                    if (aid.contentEquals(AID_NDEF)) {
                        selectedFile = null
                        Log.d(TAG, "Select NDEF Application OK")
                        return STATUS_SUCCESS
                    }
                }
            } else if (p1 == 0x00.toByte()) {
                // Select by File ID
                val lc = commandApdu[4].toInt() and 0xFF
                if (commandApdu.size >= 5 + lc) {
                    val fileId = Arrays.copyOfRange(commandApdu, 5, 5 + lc)
                    if (fileId.contentEquals(CC_FILE_ID)) {
                        selectedFile = CC_FILE_ID
                        Log.d(TAG, "Select CC file OK")
                        return STATUS_SUCCESS
                    } else if (fileId.contentEquals(NDEF_FILE_ID)) {
                        selectedFile = NDEF_FILE_ID
                        Log.d(TAG, "Select NDEF file OK")
                        return STATUS_SUCCESS
                    }
                }
            }
            return STATUS_FAILED
        }

        // READ BINARY command (INS = B0)
        if (ins == 0xB0.toByte()) {
            val offset = ((p1.toInt() and 0xFF) shl 8) or (p2.toInt() and 0xFF)
            val le = if (commandApdu.size > 4) commandApdu[4].toInt() and 0xFF else 0

            val fileData = when {
                selectedFile.contentEquals(CC_FILE_ID) -> CC_FILE
                selectedFile.contentEquals(NDEF_FILE_ID) -> ndefFilePayload
                else -> null
            }

            if (fileData != null) {
                if (offset >= fileData.size) {
                    return STATUS_FAILED
                }
                val lengthToRead = if (le == 0) {
                    fileData.size - offset
                } else {
                    minOf(le, fileData.size - offset)
                }

                val result = ByteArray(lengthToRead + 2)
                System.arraycopy(fileData, offset, result, 0, lengthToRead)
                result[result.size - 2] = STATUS_SUCCESS[0]
                result[result.size - 1] = STATUS_SUCCESS[1]

                // If finished reading NDEF payload, notify success!
                if (selectedFile.contentEquals(NDEF_FILE_ID) && (offset + lengthToRead >= fileData.size)) {
                    Log.d(TAG, "NDEF URL fully transmitted via NFC!")
                    onTransferCompleteListener?.invoke(true)
                }

                return result
            }
            return STATUS_FAILED
        }

        return STATUS_FAILED
    }

    override fun onDeactivated(reason: Int) {
        selectedFile = null
        Log.d(TAG, "HCE onDeactivated reason=$reason")
    }
}
