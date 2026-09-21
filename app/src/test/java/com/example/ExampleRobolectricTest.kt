package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.CalculatorEngine
import com.example.data.model.MilkCalculations
import com.example.traceability.TraceabilityEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Milk Collection", appName)
    }

    @Test
    fun `test milk calculation formulas`() {
        // Liters = 100, Fat = 6.5%, LR = 28.0, Rate = 200 Rs/L
        val calc = MilkCalculations.compute(100.0, 6.5, 28.0, 200.0)
        assertEquals(9.15, calc.snf, 0.01)
        assertEquals(15.65, calc.ts, 0.01)
        assertEquals(1.028, calc.spGravity, 0.001)
        assertEquals(102.8, calc.milkKg, 0.01)
        assertEquals(6.682, calc.fatKg, 0.01)
        assertEquals(9.406, calc.snfKg, 0.01)
        assertEquals(120.135, calc.tsMilk, 0.05)
        assertEquals(24027.02, calc.payment, 1.0)
    }

    @Test
    fun `test calculator engine basic operations`() {
        val engine = CalculatorEngine()
        // 50 + 25 = 75
        engine.onDigit("5")
        engine.onDigit("0")
        assertEquals("50", engine.getState().displayValue)

        engine.onOperator("+")
        assertEquals("50 +", engine.getState().expression)

        engine.onDigit("2")
        engine.onDigit("5")
        assertEquals("25", engine.getState().displayValue)

        engine.onEquals()
        assertEquals("75", engine.getState().displayValue)
    }

    @Test
    fun `test traceability engine name generation and uniqueness`() {
        val names = TraceabilityEngine.makeUrduNames(70, "محمد علی")
        assertEquals(70, names.size)
        assertEquals("محمد علی", names[0])

        // Verify values within constraints
        val generated = TraceabilityEngine.generateUniqueValues(25, 40.0)
        assertEquals(25, generated.size)
        generated.forEach { v ->
            assertTrue("Value $v must be between 1.0 and 40.0", v in 1.0..40.0)
        }
    }

    @Test
    fun `test record computation integrity`() {
        val dummyRecord = com.example.data.model.MilkRecord(
            id = 1L,
            farmerId = "F-101",
            farmerName = "Rashid Khan",
            date = "2026-09-19",
            liters = 50.0,
            fat = 6.0,
            lr = 28.0,
            rate = 180.0,
            snf = 8.84,
            ts = 14.84,
            spGravity = 1.028,
            milkKg = 51.4,
            fatKg = 3.084,
            snfKg = 4.544,
            tsKg = 7.628,
            tsMilk = 56.96,
            payment = 10252.8
        )
        assertEquals("F-101", dummyRecord.farmerId)
        assertEquals(10252.8, dummyRecord.payment, 0.01)
        assertEquals(56.96, dummyRecord.tsMilk, 0.01)
    }

    @Test
    fun `test auth manager google login and persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val authManager = com.example.data.auth.AuthManager.getInstance(context)
        authManager.signInWithGoogle("operator@gmail.com", "Dairy Operator")

        val profile = authManager.userProfile.value
        assertEquals("operator@gmail.com", profile.email)
        assertEquals("Dairy Operator", profile.name)
        assertTrue(profile.isLoggedIn)
        assertTrue(profile.isGoogleAccount)
        assertTrue(profile.isProTrialActive)
    }
}
