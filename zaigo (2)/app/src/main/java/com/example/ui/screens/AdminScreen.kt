package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.data.model.DriverEntity
import com.example.data.model.KYCStatus
import com.example.ui.theme.CashGreen
import com.example.ui.theme.LockRed
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    drivers: List<DriverEntity>,
    bookings: List<BookingEntity>,
    onApproveKYC: (Long) -> Unit,
    onRejectKYC: (Long) -> Unit,
    onDeductDailyFee: (Long) -> Unit,
    onDeductAllDailyFees: () -> Unit,
    onTopUpDriverWallet: (Long, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var adminTab by remember { mutableStateOf(0) } // 0 = Driver Approvals, 1 = Wallet Manager, 2 = Live Trips
    var showTopUpDialogForDriver by remember { mutableStateOf<DriverEntity?>(null) }

    val lockedCount = drivers.count { it.isLocked || it.walletBalance <= 40.0 }
    val pendingKYCCount = drivers.count { it.kycStatus == KYCStatus.PENDING_APPROVAL }
    val approvedCount = drivers.count { it.kycStatus == KYCStatus.APPROVED }

    Column(modifier = modifier.fillMaxSize()) {
        // Admin Overview Header Banner
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Zaigo Admin Operations Panel",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Manage Bangalore driver partners, ₹49 registration approvals & daily ₹20 deductions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Metric Cards Grid
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(
                        title = "Approved",
                        value = "$approvedCount",
                        color = CashGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Pending KYC",
                        value = "$pendingKYCCount",
                        color = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Locked (≤₹40)",
                        value = "$lockedCount",
                        color = LockRed,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total Trips",
                        value = "${bookings.size}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        TabRow(selectedTabIndex = adminTab) {
            Tab(
                selected = adminTab == 0,
                onClick = { adminTab = 0 },
                text = { Text("KYC Approvals ($pendingKYCCount)") },
                icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("admin_tab_kyc")
            )
            Tab(
                selected = adminTab == 1,
                onClick = { adminTab = 1 },
                text = { Text("Driver Wallet Manager") },
                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("admin_tab_wallets")
            )
            Tab(
                selected = adminTab == 2,
                onClick = { adminTab = 2 },
                text = { Text("Live Trips (${bookings.size})") },
                icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("admin_tab_trips")
            )
        }

        when (adminTab) {
            0 -> AdminKYCApprovalsTab(
                drivers = drivers,
                onApprove = onApproveKYC,
                onReject = onRejectKYC
            )
            1 -> AdminWalletManagerTab(
                drivers = drivers,
                onDeductDailyFee = onDeductDailyFee,
                onDeductAllDailyFees = onDeductAllDailyFees,
                onOpenTopUp = { showTopUpDialogForDriver = it }
            )
            2 -> AdminLiveTripsTab(bookings = bookings)
        }
    }

    if (showTopUpDialogForDriver != null) {
        val targetDriver = showTopUpDialogForDriver!!
        AdminTopUpDialog(
            driver = targetDriver,
            onDismiss = { showTopUpDialogForDriver = null },
            onTopUp = { amount ->
                onTopUpDriverWallet(targetDriver.id, amount)
                showTopUpDialogForDriver = null
            }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = color))
            Text(text = title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
fun AdminKYCApprovalsTab(
    drivers: List<DriverEntity>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Driver Document & Registration Verification Queue",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(drivers, key = { it.id }) { driver ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "${driver.name} (${driver.vehicleType.title})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Phone: ${driver.phone} • Plate: ${driver.vehicleNumber}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        KYCStatusChip(status = driver.kycStatus)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Registration Fee (₹49): ${if (driver.registrationFeePaid) "PAID ✓" else "UNPAID ✗"}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(text = "Aadhaar: ${driver.aadhaarNo.ifBlank { "Not provided" }}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "License: ${driver.dlNo.ifBlank { "Not provided" }}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "RC No: ${driver.rcNo.ifBlank { "Not provided" }}", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onApprove(driver.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CashGreen),
                            shape = RoundedCornerShape(8.dp),
                            enabled = driver.kycStatus != KYCStatus.APPROVED,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_approve_kyc_${driver.id}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve KYC")
                        }

                        OutlinedButton(
                            onClick = { onReject(driver.id) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LockRed),
                            shape = RoundedCornerShape(8.dp),
                            enabled = driver.kycStatus != KYCStatus.REJECTED,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_reject_kyc_${driver.id}")
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWalletManagerTab(
    drivers: List<DriverEntity>,
    onDeductDailyFee: (Long) -> Unit,
    onDeductAllDailyFees: () -> Unit,
    onOpenTopUp: (DriverEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Automated Daily Platform Deduction",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Auto-deducts ₹20 daily platform fee from active driver wallets. Locks accounts if balance drops to ≤ ₹40.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(
                        onClick = onDeductAllDailyFees,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("admin_deduct_all_button")
                    ) {
                        Text("Deduct ₹20 All")
                    }
                }
            }
        }

        item {
            Text(
                text = "Driver Wallets & Lock Status",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(drivers, key = { it.id }) { driver ->
            val isBelowThreshold = driver.walletBalance <= 40.0
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isBelowThreshold || driver.isLocked) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = driver.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${driver.vehicleType.title} • ${driver.vehicleNumber}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${driver.walletBalance.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isBelowThreshold || driver.isLocked) LockRed else CashGreen
                                )
                            )
                            Text(
                                text = if (isBelowThreshold || driver.isLocked) "LOCKED (≤₹40)" else "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isBelowThreshold || driver.isLocked) LockRed else CashGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onDeductDailyFee(driver.id) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_deduct_driver_${driver.id}")
                        ) {
                            Text("Deduct ₹20")
                        }

                        Button(
                            onClick = { onOpenTopUp(driver) },
                            colors = ButtonDefaults.buttonColors(containerColor = CashGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_topup_driver_${driver.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Credit")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLiveTripsTab(bookings: List<BookingEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Live Bangalore Logistics Order Feed",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(bookings, key = { it.id }) { booking ->
            CustomerBookingCard(booking = booking)
        }
    }
}

@Composable
fun AdminTopUpDialog(
    driver: DriverEntity,
    onDismiss: () -> Unit,
    onTopUp: (Double) -> Unit
) {
    var amountInput by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Credit Driver Wallet (${driver.name})", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Current Balance: ₹${driver.walletBalance.toInt()}")
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Credit Amount in ₹") },
                    singleLine = true
                )
                Text(
                    text = "Adding ₹${amountInput.toDoubleOrNull() ?: 0.0} will bring wallet balance to ₹${(driver.walletBalance + (amountInput.toDoubleOrNull() ?: 0.0)).toInt()}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CashGreen
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onTopUp(amt)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Approve Wallet Credit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
