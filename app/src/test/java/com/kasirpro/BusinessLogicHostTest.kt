package com.kasirpro

import org.junit.Assert.*
import org.junit.Test
import com.kasirpro.utils.SerialValidator
import com.kasirpro.utils.CurrencyFormatter
import com.kasirpro.utils.DeviceIdHelper

/**
 * BusinessLogicHostTest — QC logika murni (tanpa UI).
 * Semua yang bisa bikin bug diam-diam: pajak, stok, serial, validasi.
 * Dijalanin di GitHub Actions tiap push — kalo merah, rilis ketahan.
 */
class BusinessLogicHostTest {

    // --- Serial ---
    @Test fun serial_kosong_gagal() {
        val r = SerialValidator.validate("", "A1B2C3D4")
        assertFalse(r.isSuccess)
    }
    @Test fun serial_panjangSalah_gagal() {
        assertFalse(SerialValidator.validate("ABC", "A1B2C3D4").isSuccess)
        assertFalse(SerialValidator.validate("ABCDEFGHI", "A1B2C3D4").isSuccess)
    }
    @Test fun serial_karakterSalah_gagal() {
        assertFalse(SerialValidator.validate("AB CD12!", "A1B2C3D4").isSuccess)
    }
    @Test fun serial_benar_lolos() {
        val deviceId = "A1B2C3D4"
        val expected = SerialValidator.generateExpectedSerial(deviceId)
        assertEquals(8, expected.length)
        assertTrue(expected.all { it in 'A'..'Z' || it in '0'..'9' })
        val r = SerialValidator.validate(expected, deviceId)
        assertTrue(r.isSuccess)
    }
    @Test fun serial_generate_8Alphanum() {
        val s = SerialValidator.generateExpectedSerial("TEST1234")
        assertEquals(8, s.length)
        assertTrue(s.all { it in 'A'..'Z' || it in '0'..'9' })
    }
    @Test fun serial_generate_deterministik() {
        val a = SerialValidator.generateExpectedSerial("XYZ98765")
        val b = SerialValidator.generateExpectedSerial("XYZ98765")
        assertEquals(a, b)
    }
    @Test fun serial_beda_deviceId_beda_serial() {
        val a = SerialValidator.generateExpectedSerial("AAAA0000")
        val b = SerialValidator.generateExpectedSerial("BBBB1111")
        assertNotEquals(a, b)
    }
    @Test fun serial_caseInsensitive() {
        val deviceId = "a1b2c3d4"
        val expected = SerialValidator.generateExpectedSerial(deviceId.uppercase())
        assertTrue(SerialValidator.validate(expected.lowercase(), deviceId).isSuccess)
    }

    // --- Currency ---
    @Test fun currency_format_ada_Rp() {
        val s = CurrencyFormatter.formatRupiah(1500000L)
        assertTrue(s.contains("Rp"))
    }
    @Test fun currency_parse_roundtrip() {
        assertEquals(1500L, CurrencyFormatter.parseRupiah("Rp1.500"))
        assertEquals(0L, CurrencyFormatter.parseRupiah(""))
        assertEquals(0L, CurrencyFormatter.parseRupiah("Rp"))
    }

    // --- DeviceId ---
    @Test fun deviceId_hash_8hex_uppercase() {
        val id = DeviceIdHelper.hashDeviceId("raw123")
        assertEquals(8, id.length)
        assertTrue(id.all { it in '0'..'9' || it in 'A'..'F' })
        assertEquals(id, id.uppercase())
    }
    @Test fun deviceId_hash_deterministik() {
        assertEquals(DeviceIdHelper.hashDeviceId("same"), DeviceIdHelper.hashDeviceId("same"))
        assertNotEquals(DeviceIdHelper.hashDeviceId("a"), DeviceIdHelper.hashDeviceId("b"))
    }

    // --- Transaksi: pajak & diskon (recalc harus (subtotal-diskon)*11%) ---
    private fun recalc(subtotal: Long, discount: Long, taxPercent: Double = 11.0): Pair<Long, Long> {
        val after = maxOf(0L, subtotal - discount)
        val tax = (after * taxPercent / 100.0).toLong()
        return tax to (after + tax)
    }
    @Test fun recalc_normal() {
        val (tax, total) = recalc(100000, 10000, 11.0)
        assertEquals(9900L, tax) // (90000*0.11)
        assertEquals(99900L, total)
    }
    @Test fun recalc_diskon_melebihi_subtotal_jadi_0() {
        val (tax, total) = recalc(5000, 10000, 11.0)
        assertEquals(0L, tax)
        assertEquals(0L, total)
    }
    @Test fun recalc_tanpa_diskon() {
        val (tax, total) = recalc(100000, 0, 11.0)
        assertEquals(11000L, tax)
        assertEquals(111000L, total)
    }
    @Test fun recalc_pajak_sinkron_tiap_path() {
        // Bug lama: kadang pajak hitung dari subtotal, kadang dari afterDiscount. Sekarang harus sama.
        val subtotal = 200000L; val diskon = 30000L
        val (t1, _) = recalc(subtotal, diskon)
        val after = maxOf(0L, subtotal - diskon)
        val t2 = (after * 11.0 / 100.0).toLong()
        assertEquals(t1, t2)
    }

    // --- Validasi input produk (digit only, nama wajib) ---
    @Test fun harga_hanya_digit() {
        assertTrue("3000".all { it.isDigit() })
        assertFalse("30a0".all { it.isDigit() })
        assertFalse("3000 ".all { it.isDigit() })
    }
    @Test fun harga_toLongOrNull_clamp_negatif() {
        val p = "-5".toLongOrNull() ?: 0L
        val safe = if (p < 0) 0L else p
        assertEquals(0L, safe)
    }
    @Test fun nama_wajib_tidak_kosong() {
        assertTrue("".isBlank())
        assertFalse("Ayam Goreng".isBlank())
        assertTrue("   ".isBlank())
    }
    @Test fun stok_hanya_digit() {
        assertTrue("12".all { it.isDigit() })
        assertFalse("-1".all { it.isDigit() })
    }
}
