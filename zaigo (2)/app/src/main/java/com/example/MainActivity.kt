package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppRole
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.CustomerScreen
import com.example.ui.screens.DriverScreen
import com.example.ui.screens.RoleSelectorBar
import com.example.ui.theme.ZaigoTheme
import com.example.ui.viewmodel.ZaigoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZaigoTheme {
                ZaigoMainApp()
            }
        }
    }
}

@Composable
fun ZaigoMainApp(viewModel: ZaigoViewModel = viewModel()) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val drivers by viewModel.allDrivers.collectAsStateWithLifecycle()
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val searchingBookings by viewModel.searchingBookings.collectAsStateWithLifecycle()

    val currentDriver by viewModel.currentDriver.collectAsStateWithLifecycle()
    val driverTransactions by viewModel.currentDriverTransactions.collectAsStateWithLifecycle()
    val driverBookings by viewModel.currentDriverBookings.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            RoleSelectorBar(
                currentRole = currentRole,
                onRoleSelected = { role -> viewModel.setRole(role) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                AppRole.CUSTOMER -> {
                    CustomerScreen(
                        bookings = bookings,
                        onBookVehicle = { name, phone, pickup, drop, goodsCat, vType, dist ->
                            viewModel.bookGoodsVehicle(name, phone, pickup, drop, goodsCat, vType, dist)
                        }
                    )
                }

                AppRole.DRIVER -> {
                    DriverScreen(
                        drivers = drivers,
                        currentDriver = currentDriver,
                        availableBookings = searchingBookings,
                        driverBookings = driverBookings,
                        transactions = driverTransactions,
                        onSelectDriver = { driverId -> viewModel.selectDriver(driverId) },
                        onPayRegistrationFee = { viewModel.payDriverRegistrationFee() },
                        onSubmitKYC = { aadhaar, dl, rc -> viewModel.submitDriverKYC(aadhaar, dl, rc) },
                        onTopUpWallet = { amount -> viewModel.driverTopUpWallet(amount) },
                        onAcceptTrip = { bookingId -> viewModel.driverAcceptTrip(bookingId) },
                        onUpdateTripStatus = { bookingId, status -> viewModel.updateTripStatus(bookingId, status) },
                        onCreateDriver = { name, phone, vType, vNo -> viewModel.createNewDriver(name, phone, vType, vNo) }
                    )
                }

                AppRole.ADMIN -> {
                    AdminScreen(
                        drivers = drivers,
                        bookings = bookings,
                        onApproveKYC = { driverId -> viewModel.approveDriverKYC(driverId) },
                        onRejectKYC = { driverId -> viewModel.rejectDriverKYC(driverId) },
                        onDeductDailyFee = { driverId -> viewModel.adminDeductDailyFee(driverId) },
                        onDeductAllDailyFees = { viewModel.adminDeductAllActiveDrivers() },
                        onTopUpDriverWallet = { driverId, amount -> viewModel.adminTopUpDriverWallet(driverId, amount) }
                    )
                }
            }
        }
    }
}
