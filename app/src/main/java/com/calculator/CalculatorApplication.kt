package com.calculator

import android.app.Application

/**
 * CalculatorApplication
 *
 * Root Application class for the Native Android Multi-Mode Calculator.
 * Operates under strict privacy constraints: zero network capabilities,
 * zero third-party telemetry, and zero analytics SDK initialization.
 */
class CalculatorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Application-wide local initialization (DataStore, audio, haptics) occurs here.
    }
}
