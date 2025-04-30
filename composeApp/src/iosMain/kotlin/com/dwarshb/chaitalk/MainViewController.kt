package com.dwarshb.chaitalk

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController {
    val driverFactory = DriverFactory()
    App(driverFactory.createDriver())
}