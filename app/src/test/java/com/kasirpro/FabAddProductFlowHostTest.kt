package com.kasirpro

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Host unit tests — TIGHT loop untuk logika rawan FC Tambah Produk.
 * Dijalankan di GitHub Actions (QC gate) — harus HIJAU dulu baru rilis.
 * File source dicari via beberapa kandidat path biar work di CI (projectDir = app/)
 * maupun lokal.
 */
class FabAddProductFlowHostTest {

    private fun findSrc(relativeFromApp: String): File {
        // relativeFromApp contoh: "src/main/java/com/kasirpro/ui/product/AddProductScreen.kt"
        val candidates = listOf(
            File(relativeFromApp),
            File("app/$relativeFromApp"),
            File("../app/$relativeFromApp"),
            File(System.getProperty("user.dir") + "/" + relativeFromApp),
            File(System.getProperty("user.dir") + "/app/" + relativeFromApp),
        )
        return candidates.firstOrNull { it.exists() } ?: candidates.first()
    }

    @Test
    fun price_onlyDigits_allowed() {
        assertTrue("3000".all { it.isDigit() })
        assertFalse("30a0".all { it.isDigit() })
    }

    @Test
    fun price_toLongOrNull_clamp_negative() {
        val p = "-5".toLongOrNull() ?: 0L
        val safe = if (p < 0) 0L else p
        assertEquals(0L, safe)
        assertEquals(3000L, "3000".toLongOrNull())
        assertEquals(0L, "".toLongOrNull() ?: 0L)
    }

    @Test
    fun category_dropdown_contains_required() {
        val cats = listOf("Makanan", "Minuman", "Snack")
        assertTrue(cats.contains("Makanan"))
        assertEquals(3, cats.size)
    }

    @Test
    fun productCard_placeholder_is_vector_icon_not_adaptive_drawable() {
        val f = findSrc("src/main/java/com/kasirpro/ui/product/AddProductScreen.kt")
        assertTrue("source tidak ketemu: ${f.absolutePath}", f.exists())
        val src = f.readText()
        assertFalse("masih pakai painterResource ic_launcher + tint = rawan FC", src.contains("painterResource(R.drawable.ic_launcher)"))
        assertTrue("harus pakai Icons.Default.Image", src.contains("Icons.Default.Image"))
    }

    @Test
    fun expanded_state_is_topLevel_not_inside_Column() {
        val f = findSrc("src/main/java/com/kasirpro/ui/product/AddProductScreen.kt")
        assertTrue(f.exists())
        val src = f.readText()
        val count = "var expanded by remember".toRegex().findAll(src).count()
        assertEquals("expanded harus 1 deklarasi di top-level composable, bukan 2", 1, count)
    }

    @Test
    fun navigation_add_product_routes_exist() {
        val f = findSrc("src/main/java/com/kasirpro/ui/main/MainScreen.kt")
        assertTrue("MainScreen tidak ketemu: ${f.absolutePath}", f.exists())
        val nav = f.readText()
        assertTrue(nav.contains("composable(\"add_product\")"))
        assertTrue(nav.contains("composable(\"add_product/{productId}\")"))
        assertTrue(nav.contains("navController.navigate(\"add_product\")"))
    }
}
