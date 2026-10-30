package com.example.tic_tac_toe

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun usaElContextoDeLaAplicacion() {
        val contextoAplicacion = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.tic_tac_toe", contextoAplicacion.packageName)
    }
}
