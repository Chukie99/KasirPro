package com.kasirpro.printer

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager as SystemBluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * BluetoothManager — scan, pair and connect to ESC/POS thermal printers
 * (typically 58mm or 80mm Bluetooth SPP printers).
 *
 * Scope:
 *  - Permission helpers for Android 6 (location for discovery) and Android 12+
 *    (BLUETOOTH_CONNECT / BLUETOOTH_SCAN).
 *  - Synchronous access to already-paired devices.
 *  - Classic Bluetooth discovery (the SPP profile used by thermal printers is
 *    Classic, not BLE) with a callback-based live scan.
 *  - Pairing via createBond() (the system UI handles PIN / passkey entry).
 *  - Opening an RFCOMM (SPP) BluetoothSocket to a given MAC address. A null
 *    result means the printer is **not reachable / not found** — this is the
 *    single place the "printer not connected" state is detected for the
 *    ReceiptPrinter.
 *
 * It is a plain class (not Hilt-injected) created from a Context, matching the
 * style of the other utils in this project. Pass the application context to
 * avoid leaking an Activity.
 */
@SuppressLint("MissingPermission") // permissions are checked at runtime via hasPermissions()
class BluetoothManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as SystemBluetoothManager).adapter

    /** Single cached thread pool so socket connect() is never blocking UI. */
    private val executor = Executors.newCachedThreadPool()

    /** SPP (Serial Port Profile) UUID used by virtually every ESC/POS printer. */
    private val sppUuid: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    // ── Permission helpers ────────────────────────────────────────────────

    /** Runtime permissions required before any Bluetooth I/O can happen. */
    val requiredPermissions: Array<String>
        get() = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            } else {
                // Android 6 – 11: location is required to discover Bluetooth devices.
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }.toTypedArray()

    /** True when every permission required by [requiredPermissions] has been granted. */
    fun hasPermissions(): Boolean {
        if (bluetoothAdapter == null) return false
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Request the runtime Bluetooth permissions synchronously from an Activity.
     * On Android 12+ this asks for BLUETOOTH_CONNECT + BLUETOOTH_SCAN;
     * below that it asks for ACCESS_FINE_LOCATION (needed for discovery).
     *
     * The caller receives the result via onRequestPermissionsResult. This is a
     * thin wrapper around ActivityCompat.requestPermissions.
     */
    fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity, requiredPermissions, REQUEST_BLUETOOTH_PERMISSIONS
        )
    }

    companion object {
        const val REQUEST_BLUETOOTH_PERMISSIONS = 0xC0FFEE
        const val REQUEST_ENABLE_BLUETOOTH = 0xBEFA  // Int requestCode for startActivityForResult
        private const val DISCOVERY_TIMEOUT_MS = 12_000L
        private const val PREFS_NAME = "kasirpro_bluetooth"
        private const val KEY_PRINTER_MAC = "printer_mac"
    }

    // ── Bluetooth adapter state ───────────────────────────────────────────

    /** False when the device has no Bluetooth radio at all. */
    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    /** True when Bluetooth is currently enabled. */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    /** Prompt the user to enable Bluetooth via the system dialog. */
    fun enableBluetooth(activity: Activity) {
        if (bluetoothAdapter?.isEnabled == false) {
            @Suppress("DEPRECATION")
            activity.startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BLUETOOTH
            )
        }
    }

    // ── Paired devices ────────────────────────────────────────────────────

    /**
     * Returns the set of already-bonded (paired) Bluetooth devices immediately.
     * No scanning / discovery delay. Use this for the common flow where the
     * printer was paired through Android Settings first.
     */
    fun getPairedPrinters(): List<BluetoothDevice> {
        val adapter = bluetoothAdapter ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()
        return adapter.bondedDevices
            .filter { isLikelyPrinter(it) }
            .sortedBy { it.name ?: "" }
    }

    /**
     * Resolve a BluetoothDevice straight from its MAC address, without scanning.
     * Works for any already-known address (paired or previously recorded).
     */
    fun getDevice(mac: String): BluetoothDevice? = try {
        bluetoothAdapter?.getRemoteDevice(mac)
    } catch (e: IllegalArgumentException) {
        null
    }

    /**
     * Loose heuristic to recognise an ESC/POS printer from its advertised name.
     * Thermal printers tend to carry these keywords; returns true as a fallback
     * when the name is unknown so a valid printer is never hidden by a bad name.
     */
    private fun isLikelyPrinter(device: BluetoothDevice): Boolean {
        val name = device.name ?: return true
        val lower = name.lowercase()
        return lower.contains("printer") ||
            lower.contains("pos") ||
            lower.contains("mrp") ||       // common thermal printer prefixes
            lower.contains("xprinter") ||
            lower.contains("epson") ||
            lower.contains("bixolon") ||
            lower.contains("star") ||
            lower.contains("g printer")
    }

    // ── Live discovery (Classic Bluetooth) ────────────────────────────────

    /** Listener invoked while a live scan is running. */
    interface DiscoveryListener {
        fun onDeviceFound(device: BluetoothDevice)
        fun onDiscoveryFinished()
    }

    private var discoveryReceiver: BroadcastReceiver? = null

    /**
     * Start a Classic Bluetooth discovery scan. Each discovered printer is
     * delivered to [listener]; [DiscoveryListener.onDiscoveryFinished] fires on
     * completion or after the internal timeout, whichever is first.
     *
     * Caller MUST [DiscoveryHandle.stop] the scan before connecting, so the scan
     * does not interfere with the SPP socket handshake.
     */
    fun startDiscovery(listener: DiscoveryListener): DiscoveryHandle {
        stopDiscovery() // only one scan at a time

        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            listener.onDiscoveryFinished()
            return DiscoveryHandle { }
        }

        val found = mutableSetOf<String>()
        val handler = Handler(Looper.getMainLooper())

        discoveryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        if (device != null && device.name != null &&
                            !found.contains(device.address) &&
                            isLikelyPrinter(device)
                        ) {
                            found.add(device.address)
                            listener.onDeviceFound(device)
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        tearDownReceiver()
                        listener.onDiscoveryFinished()
                        handler.removeCallbacksAndMessages(null)
                    }
                }
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND).apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(discoveryReceiver, filter)

        val timeout = handler.postDelayed({
            tearDownReceiver()
            listener.onDiscoveryFinished()
        }, DISCOVERY_TIMEOUT_MS)

        // startDiscovery() is safe to call off the UI thread.
        executor.execute { adapter.startDiscovery() }

        return DiscoveryHandle {
            tearDownReceiver()
            handler.removeCallbacksAndMessages(null)
        }
    }

    /** Stop the current discovery scan (safe to call repeatedly). */
    fun stopDiscovery() {
        val adapter = bluetoothAdapter ?: return
        if (adapter.isDiscovering) {
            adapter.cancelDiscovery()
        }
        tearDownReceiver()
    }

    private fun tearDownReceiver() {
        discoveryReceiver?.let {
            try { context.unregisterReceiver(it) } catch (e: IllegalArgumentException) { /* already unregistered */ }
        }
        discoveryReceiver = null
    }

    /** Opaque handle to stop an in-flight [startDiscovery] scan. */
    fun interface DiscoveryHandle {
        fun stop()
    }

    // ── Pairing / bonding ─────────────────────────────────────────────────

    /**
     * Start the pairing (bonding) flow for a device. The system UI handles PIN /
     * passkey entry; for printers with a fixed PIN pair through Android Settings
     * first.
     *
     * @return true if bonding was initiated or already complete, false if it
     *         could not be started.
     */
    fun pairDevice(device: BluetoothDevice): Boolean {
        val state = device.bondState
        if (state == BluetoothDevice.BOND_BONDED || state == BluetoothDevice.BOND_BONDING) {
            return true
        }
        return device.createBond()
    }

    /** Whether a device is fully paired (bonded). */
    fun isPaired(device: BluetoothDevice): Boolean =
        device.bondState == BluetoothDevice.BOND_BONDED

    // ── Saved-printer MAC (SharedPreferences-backed) ────────────────────────

    /**
     * Persist the MAC address of the chosen printer so printReceipt() can find
     * it later without forcing the user to re-select on every transaction.
     */
    fun savePrinterMac(mac: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_PRINTER_MAC, mac).apply()
    }

    /** Last MAC saved via [savePrinterMac], or null when none was chosen. */
    fun getSavedPrinterMac(): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PRINTER_MAC, null)

    // ── Socket connection ─────────────────────────────────────────────────

    /**
     * Open an RFCOMM (SPP) socket to [device] and block until it connects.
     * This is the canonical handshake for ESC/POS thermal printers.
     *
     * Returns the connected socket on success, or **null** when the printer is
     * not found / not reachable / refuses the connection. Returning null
     * (rather than throwing) is intentional: ReceiptPrinter treats a null socket
     * as "printer not connected" and exits gracefully instead of crashing.
     *
     * Discovery is cancelled first so the scan does not starve the connection.
     */
    fun connectSocket(device: BluetoothDevice, timeoutMs: Int = 5_000): BluetoothSocket? {
        cancelDiscoveryIfNeeded()
        val socket = try {
            device.createRfcommSocketToServiceRecord(sppUuid)
        } catch (e: IOException) {
            return null
        }

        return try {
            val future = executor.submit { socket.connect() }
            future.get(timeoutMs.toLong(), TimeUnit.MILLISECONDS)
            socket
        } catch (e: TimeoutException) {
            closeQuietly(socket)
            null
        } catch (e: Exception) {
            // IOException (not found / not connected) or rejected execution.
            closeQuietly(socket)
            null
        }
    }

    /**
     * Open an RFCOMM (SPP) socket to the printer at [mac]. Resolves the device
     * first; returns null if the address is unknown or the socket fails.
     */
    fun connectSocket(mac: String, timeoutMs: Int = 5_000): BluetoothSocket? {
        val device = getDevice(mac) ?: return null
        return connectSocket(device, timeoutMs)
    }

    /** Close a previously opened socket quietly. */
    fun disconnect(socket: BluetoothSocket?) {
        closeQuietly(socket)
    }

    private fun cancelDiscoveryIfNeeded() {
        val adapter = bluetoothAdapter ?: return
        if (adapter.isDiscovering) adapter.cancelDiscovery()
    }

    private fun closeQuietly(socket: BluetoothSocket?) {
        try { socket?.close() } catch (e: IOException) { /* ignore */ }
    }
}
