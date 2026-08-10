package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.CashGreen
import com.example.ui.theme.LockRed
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    drivers: List<DriverEntity>,
    currentDriver: DriverEntity?,
    availableBookings: List<BookingEntity>,
    driverBookings: List<BookingEntity>,
    transactions: List<WalletTransactionEntity>,
    onSelectDriver: (Long) -> Unit,
    onPayRegistrationFee: () -> Unit,
    onSubmitKYC: (aadhaar: String, dl: String, rc: String) -> Unit,
    onTopUpWallet: (Double) -> Unit,
    onAcceptTrip: (Long) -> Unit,
    onUpdateTripStatus: (Long, BookingStatus) -> Unit,
    onCreateDriver: (name: String, phone: String, vehicleType: VehicleType, vehicleNumber: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDriverTab by remember { mutableStateOf(0) } // 0 = Dashboard & Wallet, 1 = Available Orders, 2 = My Trips & Cash, 3 = KYC & Registration
    var showNewDriverDialog by remember { mutableStateOf(false) }
    var cashfreePaymentTarget by remember { mutableStateOf<Pair<String, Double>?>(null) } // Purpose to Amount

    // KYC Form states
    var aadhaarInput by remember { mutableStateOf(currentDriver?.aadhaarNo ?: "") }
    var dlInput by remember { mutableStateOf(currentDriver?.dlNo ?: "") }
    var rcInput by remember { mutableStateOf(currentDriver?.rcNo ?: "") }

    LaunchedEffect(currentDriver) {
        if (currentDriver != null) {
            aadhaarInput = currentDriver.aadhaarNo
            dlInput = currentDriver.dlNo
            rcInput = currentDriver.rcNo
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Driver Selector Dropdown Bar
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Driver Profile:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(drivers) { drv ->
                            val isSel = currentDriver?.id == drv.id
                            FilterChip(
                                selected = isSel,
                                onClick = { onSelectDriver(drv.id) },
                                label = {
                                    Text(
                                        text = "${drv.name} ${if (drv.isLocked) "🔒" else "✓"}",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showNewDriverDialog = true },
                        modifier = Modifier.testTag("add_new_driver_button")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add New Driver Partner", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (currentDriver == null) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
            return
        }

        // Lock Banner Warning if Wallet <= ₹40
        if (currentDriver.isLocked || currentDriver.walletBalance <= 40.0) {
            WalletLockBanner(
                walletBalance = currentDriver.walletBalance,
                onTopUpClick = { onTopUpWallet(100.0) },
                modifier = Modifier.padding(12.dp)
            )
        }

        // Driver Section Tabs
        TabRow(
            selectedTabIndex = selectedDriverTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedDriverTab == 0,
                onClick = { selectedDriverTab = 0 },
                text = { Text("Wallet & Rules", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("driver_tab_wallet")
            )
            Tab(
                selected = selectedDriverTab == 1,
                onClick = { selectedDriverTab = 1 },
                text = { Text("Available Orders (${availableBookings.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("driver_tab_orders")
            )
            Tab(
                selected = selectedDriverTab == 2,
                onClick = { selectedDriverTab = 2 },
                text = { Text("My Trips (${driverBookings.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("driver_tab_my_trips")
            )
            Tab(
                selected = selectedDriverTab == 3,
                onClick = { selectedDriverTab = 3 },
                text = { Text("KYC & ₹49 Fee", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("driver_tab_kyc")
            )
        }

        when (selectedDriverTab) {
            0 -> DriverWalletTab(
                driver = currentDriver,
                transactions = transactions,
                onTopUpWallet = { amount ->
                    cashfreePaymentTarget = "Wallet Recharge (₹${amount.toInt()})" to amount
                }
            )
            1 -> DriverAvailableOrdersTab(
                driver = currentDriver,
                availableBookings = availableBookings,
                onAcceptTrip = onAcceptTrip
            )
            2 -> DriverMyTripsTab(
                driverBookings = driverBookings,
                onUpdateStatus = onUpdateTripStatus
            )
            3 -> DriverKYCTab(
                driver = currentDriver,
                aadhaar = aadhaarInput,
                dl = dlInput,
                rc = rcInput,
                onAadhaarChange = { aadhaarInput = it },
                onDlChange = { dlInput = it },
                onRcChange = { rcInput = it },
                onPayRegFee = {
                    cashfreePaymentTarget = "Zaigo ₹49 Registration Fee" to 49.0
                },
                onSubmitKYC = { onSubmitKYC(aadhaarInput, dlInput, rcInput) }
            )
        }
    }

    if (showNewDriverDialog) {
        NewDriverDialog(
            onDismiss = { showNewDriverDialog = false },
            onCreate = { name, phone, vType, vNo ->
                onCreateDriver(name, phone, vType, vNo)
                showNewDriverDialog = false
            }
        )
    }

    if (cashfreePaymentTarget != null) {
        val (purpose, amount) = cashfreePaymentTarget!!
        CashfreePaymentDialog(
            purpose = purpose,
            amount = amount,
            onDismiss = { cashfreePaymentTarget = null },
            onPaymentSuccess = { txnId ->
                cashfreePaymentTarget = null
                if (amount == 49.0) {
                    onPayRegistrationFee()
                } else {
                    onTopUpWallet(amount)
                }
            }
        )
    }
}

@Composable
fun DriverWalletTab(
    driver: DriverEntity,
    transactions: List<WalletTransactionEntity>,
    onTopUpWallet: (Double) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Wallet Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (driver.isLocked) Color(0xFF263238) else MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "DRIVER WALLET BALANCE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (driver.isLocked) Color.LightGray else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Text(
                                text = "₹${driver.walletBalance.toInt()}",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (driver.isLocked) LockRed else CashGreen
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = CircleShape,
                                color = if (driver.isLocked) LockRed else CashGreen
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (driver.isLocked) Icons.Default.Lock else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (driver.isLocked) "LOCKED" else "ACTIVE",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = Color.Gray.copy(alpha = 0.3f))

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rules Summary
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Rule, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Registration Fee: ₹49 (One-time) • ${if (driver.registrationFeePaid) "PAID ✓" else "PENDING ✗"}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (driver.isLocked) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = LockRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Daily Deduction: ₹20/day auto-deducted from wallet",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (driver.isLocked) Color.LightGray else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LockClock, contentDescription = null, tint = LockRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lock Threshold: Below ₹40 account is locked automatically",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = LockRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Top Up Quick Actions
                    Text(
                        text = "Quick Wallet Recharge:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (driver.isLocked) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onTopUpWallet(50.0) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("recharge_50_button")
                        ) {
                            Text("+ ₹50", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onTopUpWallet(100.0) },
                            colors = ButtonDefaults.buttonColors(containerColor = CashGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("recharge_100_button")
                        ) {
                            Text("+ ₹100", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onTopUpWallet(200.0) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("recharge_200_button")
                        ) {
                            Text("+ ₹200", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Total Cash Earnings Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Completed Trips", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${driver.totalTripsCompleted}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Total Cash Collected", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "₹${driver.totalCashCollected.toInt()}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CashGreen
                            )
                        )
                    }
                }
            }
        }

        // Cashfree Gateway Integration Banner
        item {
            CashfreeIntegrationGuideCard(
                onSimulateCashfreeClick = { onTopUpWallet(100.0) }
            )
        }

        // Transactions History
        item {
            Text(
                text = "Wallet Transaction Log",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (transactions.isEmpty()) {
            item {
                Text(text = "No transactions recorded yet.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.description,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = tx.type.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${if (tx.amount > 0) "+" else ""}₹${tx.amount.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (tx.amount > 0) CashGreen else LockRed
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DriverAvailableOrdersTab(
    driver: DriverEntity,
    availableBookings: List<BookingEntity>,
    onAcceptTrip: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (driver.isLocked) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = LockRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Your account is LOCKED (Balance ≤ ₹40). You cannot accept new bookings until wallet is recharged.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = LockRed)
                        )
                    }
                }
            }
        }

        if (availableBookings.isEmpty()) {
            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                    Text(text = "No open customer requests in Bangalore right now.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            items(availableBookings, key = { it.id }) { booking ->
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
                            Text(
                                text = "Order #${booking.id} • ${booking.vehicleType.title}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "₹${booking.fareAmount.toInt()} CASH",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CashGreen
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Pickup: ${booking.pickupLocation}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Drop: ${booking.dropLocation}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Goods: ${booking.goodsCategory.label} (${booking.distanceKm} km)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onAcceptTrip(booking.id) },
                            enabled = !driver.isLocked && driver.registrationFeePaid && driver.kycStatus == KYCStatus.APPROVED,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("accept_order_${booking.id}_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (driver.isLocked) "ACCOUNT LOCKED (Recharge to Accept)" else "Accept Trip (Collect ₹${booking.fareAmount.toInt()} Cash)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverMyTripsTab(
    driverBookings: List<BookingEntity>,
    onUpdateStatus: (Long, BookingStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (driverBookings.isEmpty()) {
            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                    Text(text = "You haven't accepted any trips yet.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            items(driverBookings, key = { it.id }) { booking ->
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
                            Text(
                                text = "Trip #${booking.id}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            BookingStatusChip(status = booking.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Customer: ${booking.customerName} (${booking.customerPhone})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = "Pickup: ${booking.pickupLocation}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Drop: ${booking.dropLocation}", style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Text(text = "Collect Cash from Customer:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "₹${booking.fareAmount.toInt()}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = CashGreen)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Progression Buttons
                        when (booking.status) {
                            BookingStatus.ACCEPTED -> {
                                Button(
                                    onClick = { onUpdateStatus(booking.id, BookingStatus.ARRIVED_PICKUP) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().testTag("status_arrived_button")
                                ) {
                                    Text("Mark Arrived at Pickup Point")
                                }
                            }
                            BookingStatus.ARRIVED_PICKUP -> {
                                Button(
                                    onClick = { onUpdateStatus(booking.id, BookingStatus.GOODS_LOADED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().testTag("status_loaded_button")
                                ) {
                                    Text("Goods Loaded & Start Journey")
                                }
                            }
                            BookingStatus.GOODS_LOADED -> {
                                Button(
                                    onClick = { onUpdateStatus(booking.id, BookingStatus.DELIVERED_CASH_COLLECTED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CashGreen),
                                    modifier = Modifier.fillMaxWidth().testTag("status_delivered_button")
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mark Delivered & Cash Collected (₹${booking.fareAmount.toInt()})", fontWeight = FontWeight.Bold)
                                }
                            }
                            BookingStatus.DELIVERED_CASH_COLLECTED -> {
                                Text(
                                    text = "✓ Trip Completed! Cash collected from customer.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = CashGreen)
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverKYCTab(
    driver: DriverEntity,
    aadhaar: String,
    dl: String,
    rc: String,
    onAadhaarChange: (String) -> Unit,
    onDlChange: (String) -> Unit,
    onRcChange: (String) -> Unit,
    onPayRegFee: () -> Unit,
    onSubmitKYC: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 1: ₹49 Registration Fee Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "1. Registration Fee (₹49)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (driver.registrationFeePaid) {
                            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp)) {
                                Text(
                                    text = "PAID ✓",
                                    color = CashGreen,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Mandatory ₹49 one-time driver onboarding fee to activate Zaigo Driver account.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!driver.registrationFeePaid) {
                        Button(
                            onClick = onPayRegFee,
                            colors = ButtonDefaults.buttonColors(containerColor = CashGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("pay_reg_fee_button")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pay ₹49 Registration Fee", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Step 2: KYC Verification Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "2. Verification Documents",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        KYCStatusChip(status = driver.kycStatus)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = aadhaar,
                        onValueChange = onAadhaarChange,
                        label = { Text("Aadhaar Card Number") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_aadhaar")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dl,
                        onValueChange = onDlChange,
                        label = { Text("Driving License Number") },
                        leadingIcon = { Icon(Icons.Default.CardMembership, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_dl")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = rc,
                        onValueChange = onRcChange,
                        label = { Text("Vehicle RC Number") },
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().testTag("input_rc")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onSubmitKYC,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("submit_kyc_button")
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Documents for Admin Review", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NewDriverDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, phone: String, vehicleType: VehicleType, vehicleNumber: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+91 ") }
    var vehicleNumber by remember { mutableStateOf("KA-01-") }
    var vehicleType by remember { mutableStateOf(VehicleType.TATA_ACE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Zaigo Driver Partner", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Driver Full Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = vehicleNumber,
                    onValueChange = { vehicleNumber = it },
                    label = { Text("Vehicle Plate No. (e.g. KA-01-AB-1234)") },
                    singleLine = true
                )

                Text("Vehicle Type:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(VehicleType.entries) { vt ->
                        FilterChip(
                            selected = vehicleType == vt,
                            onClick = { vehicleType = vt },
                            label = { Text(vt.title, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, phone, vehicleType, vehicleNumber) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Create Partner Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
