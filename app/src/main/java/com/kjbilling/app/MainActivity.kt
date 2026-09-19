package com.kjbilling.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kjbilling.app.navigation.AppNavigation
import com.kjbilling.app.ui.theme.KJInvoiceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KJInvoiceTheme {
                AppNavigation()
            }
        }
    }
}
