package com.kjbilling.app.ui.upi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.ui.components.ActionButton
import com.kjbilling.app.ui.components.AmountDisplay
import com.kjbilling.app.ui.components.AmountKeypad
import com.kjbilling.app.ui.components.EmptyState
import com.kjbilling.app.ui.components.LoadingState
import com.kjbilling.app.ui.components.SecondaryButton
import com.kjbilling.app.ui.components.UpiQrCard
import com.kjbilling.app.ui.theme.Dimens

/** Type any amount and show a "Scan to pay" QR — for walk-in payments without a bill. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiQrScreen(
    onNavigateBack: () -> Unit,
    onOpenBusinessProfile: () -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: UpiQrViewModel = viewModel(factory = UpiQrViewModel.factory(app.container))
    val state by viewModel.state.collectAsState()
    val canShow by viewModel.canShow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UPI QR") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val request = state.request
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Lg)
        ) {
            when {
                state.isLoading -> LoadingState()
                !state.hasUpiId -> EmptyState(
                    icon = Icons.Outlined.QrCode2,
                    title = "Add your UPI ID first",
                    subtitle = "Save it once in Business Profile. Then any customer can scan to pay you.",
                    actionText = "Open Business Profile",
                    onAction = onOpenBusinessProfile
                )
                request != null -> {
                    UpiQrCard(request)
                    SecondaryButton(text = "New amount", onClick = viewModel::newAmount)
                }
                else -> {
                    Text("Enter amount to collect")
                    AmountDisplay(state.amountText)
                    AmountKeypad(text = state.amountText, onTextChange = viewModel::onAmountChange)
                    ActionButton(text = "Show QR", onClick = viewModel::showQr, enabled = canShow)
                }
            }
        }
    }
}
