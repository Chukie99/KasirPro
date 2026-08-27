package com.kasirpro.printer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import java.io.IOException
import java.io.OutputStream
import java.util.*

/**
 * BluetoothManager — handles discovery & connection to a Bluetooth
 * thermal printer (ESC/POS compatible, 58mm & 80mm).
 *
 * The MAC address of a successfully paired printer is saved to
 * SharedPreferences via SettingsViewModel.
 */
object BluetoothManager {

    private val BLUETOOTH_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    /** Returns true if Bluetooth is enabled. */
    fun isBluetoothEnabled(): Boolean = BluetoothAdapter.getDefaultAdapter()?.isEnabled == true

    /** Enables Bluetooth (needs runtime permission). */
    fun enableBluetooth(activity: androidx.activity.ComponentActivity) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null && !adapter.isEnabled) {
            val enableBt = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBt, 7654)
        }
    }

    /** Pairs & connects to a printer by MAC address. Returns the socket. */
    fun connectPrinter(macAddress: String): BluetoothSocket? {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            val device = adapter?.getRemoteDevice(macAddress)
            val socket = device?.createRfcommSocketToServiceRecord(BLUETOOTH_UUID)
            socket?.connect()
            socket
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /** Gets a list of paired Bluetooth devices (for device picker). */
    fun getPairedDevices(): List<BluetoothDevice> {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        return adapter?.bondedDevices?.toList() ?: emptyList()
    }

    /** Sends raw bytes to the connected socket. */
    fun sendData(socket: BluetoothSocket, data: ByteArray): Boolean {
        return try {
            socket.outputStream?.write(data)?.run { }
            socket.outputStream?.flush()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /** Disconnects & closes the socket. */
    fun disconnect(socket: BluetoothSocket?) {
        try { socket?.close() } catch (e: IOException) { e.printStackTrace() }
    }
}
