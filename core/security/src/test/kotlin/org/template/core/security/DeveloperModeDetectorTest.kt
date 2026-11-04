package org.template.core.security

import android.content.Context
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Pruebas unitarias para [DeveloperModeDetector] usando Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
class DeveloperModeDetectorTest {

    private lateinit var context: Context
    private lateinit var detector: DeveloperModeDetector

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        detector = DeveloperModeDetector(context)
    }

    @Test
    fun `isDeveloperModeEnabled retorna false por defecto en entorno limpio`() {
        Settings.Secure.putInt(context.contentResolver, Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0)
        assertFalse(detector.isDeveloperModeEnabled())
    }

    @Test
    fun `isAdbEnabled retorna false por defecto en entorno limpio`() {
        Settings.Secure.putInt(context.contentResolver, Settings.Secure.ADB_ENABLED, 0)
        assertFalse(detector.isAdbEnabled())
    }
}
