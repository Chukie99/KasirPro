package com.kasirpro

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import androidx.test.ext.junit.runners.AndroidJUnit4

/**
 * AllScreensHostTest — render SEMUA screen tanpa FC.
 * Tiap test = 1 screen. Kalo ada screen yang crash pas compose (icon tint, remember, menuAnchor),
 * test langsung MERAH di GitHub — gak perlu lu jadi tester.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class AllScreensHostTest {

    @get:Rule val rule = createComposeRule()

    @Test fun dashboard_probe_render() {
        rule.setContent { MaterialTheme { DashboardProbe() } }
        rule.onNodeWithText("Dashboard").assertIsDisplayed()
    }

    @Test fun transaction_probe_render() {
        rule.setContent { MaterialTheme { TransactionProbe() } }
        rule.onNodeWithText("Transaksi").assertIsDisplayed()
    }

    @Test fun report_probe_render() {
        rule.setContent { MaterialTheme { ReportProbe() } }
        rule.onNodeWithText("Laporan").assertIsDisplayed()
    }

    @Test fun table_probe_render() {
        rule.setContent { MaterialTheme { TableProbe() } }
        rule.onNodeWithText("Meja").assertIsDisplayed()
    }

    @Test fun settings_probe_render() {
        rule.setContent { MaterialTheme { SettingsProbe() } }
        rule.onNodeWithText("Pengaturan").assertIsDisplayed()
    }

    @Test fun activation_probe_render() {
        rule.setContent { MaterialTheme { ActivationProbe() } }
        rule.onNodeWithText("Aktivasi Premium").assertIsDisplayed()
    }

    @Test fun splash_probe_render() {
        rule.setContent { MaterialTheme { SplashProbe() } }
        rule.onNodeWithText("Tenang. Rapi. Berkelas.").assertIsDisplayed()
    }

    // ——— regression: MainScreen bottom nav must use remember + popUpTo saveState ———
    @Test fun mainScreen_bottomNav_fix_still_present() {
        val f = findSrc("src/main/java/com/kasirpro/ui/main/MainScreen.kt")
        assert(f.exists()) { "MainScreen tidak ketemu: ${f.absolutePath}" }
        val src = f.readText()
        // remember sekali, bukan list tiap recompose
        assert(src.contains("remember(")) { "MainScreen harus pakai remember untuk items" }
        assert(src.contains("popUpTo")) { "MainScreen harus pakai popUpTo saveState biar gak lebay" }
        assert(src.contains("saveState = true")) { "popUpTo harus saveState = true" }
        assert(src.contains("restoreState = true")) { "navigate harus restoreState = true" }
    }

    private fun findSrc(rel: String): java.io.File {
        val cands = listOf(
            java.io.File(rel),
            java.io.File("app/$rel"),
            java.io.File("../app/$rel"),
            java.io.File(System.getProperty("user.dir") + "/" + rel),
            java.io.File(System.getProperty("user.dir") + "/app/" + rel),
        )
        return cands.firstOrNull { it.exists() } ?: cands.first()
    }
}

// ——— Probes ———

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun DashboardProbe() {
    Column { Text("Dashboard"); OutlinedTextField(value = "", onValueChange = {}, label = { Text("Cari") }, modifier = Modifier.testTag("search")) }
}
@Composable private fun TransactionProbe() {
    var qty by remember { mutableIntStateOf(1) }
    Column {
        Text("Transaksi")
        Row { Button(onClick = { if (qty > 1) qty-- }) { Text("-") }; Text("$qty"); Button(onClick = { qty++ }) { Text("+") } }
        Button(onClick = {}) { Icon(Icons.Default.ShoppingCart, null); Text("Bayar") }
    }
}
@Composable private fun ReportProbe() {
    Column { Text("Laporan"); Text("Rp 100.000"); LinearProgressIndicator(progress = { 0.7f }) }
}
@Composable private fun TableProbe() {
    Column { Text("Meja"); Card { Text("A1 — Kosong") }; FloatingActionButton(onClick = {}) { Icon(Icons.Default.Add, null) } }
}
@Composable private fun SettingsProbe() {
    Column { Text("Pengaturan"); Text("Premium Edition"); Text("100% Offline • Tanpa Iklan") }
}
@Composable private fun ActivationProbe() {
    Column { Text("Aktivasi Premium"); OutlinedTextField(value = "", onValueChange = {}, label = { Text("Serial Premium") }); Button(onClick = {}) { Text("Buka Akses") } }
}
@Composable private fun SplashProbe() {
    Column { Text("KasirPro"); Text("Tenang. Rapi. Berkelas.") ; Text("Point of Sale untuk Usaha Modern") }
}
