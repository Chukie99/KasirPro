package com.kasirpro.printer

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.widget.Toast
import com.kasirpro.data.model.TransactionItem
import com.kasirpro.utils.CurrencyFormatter
import com.kasirpro.utils.DateFormatter
import java.nio.charset.StandardCharsets

/**
 * ReceiptPrinter — formats a sales receipt (ESC/POS) and sends it
 * to a paired Bluetooth thermal printer (58mm or 80mm).
 *
 * Uses raw ESC/POS byte sequences so no external image library is needed.
 */
object ReceiptPrinter {

    private const val PAPER_WIDTH_58 = 32   // characters per line (58mm)

    /**
     * Prints a full receipt. If no printer is connected, shows a toast.
     * @return true if print command sent, false otherwise.
     */
    fun printReceipt(
        context: Context,
        storeName: String,
        storeAddress: String,
        storePhone: String,
        items: List<TransactionItem>,
        subtotal: Long,
        tax: Long,
        discount: Long,
        total: Long,
        paymentMethod: String,
        timestamp: Long = System.currentTimeMillis(),
    ): Boolean {
        val prefs = context.getSharedPreferences("kasirpro_prefs", Context.MODE_PRIVATE)
        val mac = prefs.getString("kasirpro_printer_address", null)
        if (mac == null) {
            Toast.makeText(context, "Printer belum terhubung.", Toast.LENGTH_SHORT).show()
            return false
        }

        val bluetoothMgr = BluetoothManager(context)
        val socket = bluetoothMgr.connectSocket(mac)
        if (socket == null) {
            Toast.makeText(context, "Gagal sambung ke printer.", Toast.LENGTH_SHORT).show()
            return false
        }

        val receipt = buildReceipt(
            storeName = storeName,
            storeAddress = storeAddress,
            storePhone = storePhone,
            items = items,
            subtotal = subtotal,
            tax = tax,
            discount = discount,
            total = total,
            paymentMethod = paymentMethod,
            timestamp = timestamp,
        )

        val success = try {
            socket.outputStream?.write(receipt.toByteArray(StandardCharsets.US_ASCII))
            true
        } catch (_: Exception) {
            false
        }
        if (success) {
            Toast.makeText(context, "Struk dicetak.", Toast.LENGTH_SHORT).show()
            // Feed & cut (form feed + cut command for most Epson printers)
            try { socket.outputStream?.write(byteArrayOf(0x0A, 0x0A, 0x1D, 0x56, 0x41, 0x00)) } catch (_: Exception) {}
        }
        bluetoothMgr.disconnect(socket)
        return success
    }

    /**
     * Builds the ESC/POS formatted receipt as a String.
     */
    private fun buildReceipt(
        storeName: String,
        storeAddress: String,
        storePhone: String,
        items: List<TransactionItem>,
        subtotal: Long,
        tax: Long,
        discount: Long,
        total: Long,
        paymentMethod: String,
        timestamp: Long,
    ): String {
        val sb = StringBuilder()
        sb.append("--------------------------------\n")
        sb.append(center("KasirPro") + "\n")
        sb.append(center(storeName) + "\n")
        sb.append(center(storeAddress.takeIf { it.isNotBlank() } ?: "-") + "\n")
        sb.append(center(storePhone.takeIf { it.isNotBlank() } ?: "-") + "\n")
        sb.append("Tel: " + (storePhone.takeIf { it.isNotBlank() } ?: "-") + "\n")
        sb.append("--------------------------------\n")
        sb.append("Tanggal: " + DateFormatter.formatDateTime(timestamp) + "\n")
        sb.append("Meja: -\n")
        sb.append("--------------------------------\n")
        // Items
        items.forEach { item ->
            val name = item.productName.take(20)
            val qtyPrice = "x${item.quantity} @ ${CurrencyFormatter.formatRupiah(item.price)}"
            val subStr = CurrencyFormatter.formatRupiah(item.subtotal)
            sb.append(padRight(name, PAPER_WIDTH_58 - qtyPrice.length) + qtyPrice + "\n")
            sb.append(padRight("", PAPER_WIDTH_58 - subStr.length) + subStr + "\n")
        }
        sb.append("--------------------------------\n")
        sb.append(padRight("SUBTOTAL", PAPER_WIDTH_58 - CurrencyFormatter.formatRupiah(subtotal).length) + CurrencyFormatter.formatRupiah(subtotal) + "\n")
        sb.append(padRight("PAJAK", PAPER_WIDTH_58 - CurrencyFormatter.formatRupiah(tax).length) + CurrencyFormatter.formatRupiah(tax) + "\n")
        sb.append(padRight("DISKON", PAPER_WIDTH_58 - CurrencyFormatter.formatRupiah(discount).length) + "-" + CurrencyFormatter.formatRupiah(discount) + "\n")
        sb.append(padRight("TOTAL", PAPER_WIDTH_58 - CurrencyFormatter.formatRupiah(total).length) + CurrencyFormatter.formatRupiah(total) + "\n")
        sb.append("--------------------------------\n")
        sb.append("Bayar: $paymentMethod\n")
        sb.append("--- TERIMA KASIH ---\n")
        sb.append("\n\n")
        return sb.toString()
    }

    private fun center(text: String): String {
        val pad = (PAPER_WIDTH_58 - text.length) / 2
        return " ".repeat(maxOf(0, pad)) + text
    }

    private fun padRight(str: String, totalWidth: Int): String {
        val w = maxOf(0, totalWidth)
        return if (str.length >= w) str.substring(0, w) else str + " ".repeat(w - str.length)
    }
}
