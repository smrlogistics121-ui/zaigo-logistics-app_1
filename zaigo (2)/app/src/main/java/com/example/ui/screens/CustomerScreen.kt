package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.BookingEntity
import com.example.data.model.GoodsCategory
import com.example.data.model.VehicleType
import com.example.ui.theme.CashGreen

val bangaloreHubs = listOf(
    "Peenya Industrial Area Phase 1",
    "Koramangala 5th Block",
    "Indiranagar 100ft Road",
    "Whitefield ITPL Main Rd",
    "HSR Layout Sector 1",
    "Electronic City Phase 1",
    "Rajajinagar Industrial Area",
    "Peenya Phase 2 Peenya",
    "Majestic City Railway Station",
    "Yelahanka New Town"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    bookings: List<BookingEntity>,
    onBookVehicle: (
        customerName: String,
        customerPhone: String,
        pickup: String,
        drop: String,
        goodsCat: GoodsCategory,
        vehicleType: VehicleType,
        distanceKm: Double
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Book Trip, 1 = My Orders

    // Form states
    var customerName by remember { mutableStateOf("Anand Traders") }
    var customerPhone by remember { mutableStateOf("+91 98450 11223") }
    var pickupLocation by remember { mutableStateOf(bangaloreHubs[0]) }
    var dropLocation by remember { mutableStateOf(bangaloreHubs[1]) }
    var selectedGoodsCat by remember { mutableStateOf(GoodsCategory.ELECTRICAL_HARDWARE) }
    var selectedVehicle by remember { mutableStateOf(VehicleType.TATA_ACE) }
    var distanceKm by remember { mutableFloatStateOf(16.0f) }

    val currentFare = selectedVehicle.baseFare + (distanceKm * selectedVehicle.perKmRate)

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Book Goods Transport", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                modifier = Modifier.testTag("customer_tab_book")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("My Trips (${bookings.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                modifier = Modifier.testTag("customer_tab_orders")
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CashPaymentNoticeCard()
                }

                // Customer Details Header Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Customer Information",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = customerName,
                                    onValueChange = { customerName = it },
                                    label = { Text("Name / Company") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_customer_name"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customerPhone,
                                    onValueChange = { customerPhone = it },
                                    label = { Text("Phone Number") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_customer_phone"),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                // Pickup & Drop Locations in Bangalore
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bangalore Pickup & Drop Locations",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = pickupLocation,
                                onValueChange = { pickupLocation = it },
                                label = { Text("Pickup Address (Bengaluru)") },
                                leadingIcon = {
                                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = CashGreen)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_pickup_location")
                            )

                            // Quick Bangalore Hub Chips for Pickup
                            Text(
                                text = "Popular Hubs:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(bangaloreHubs) { hub ->
                                    FilterChip(
                                        selected = pickupLocation == hub,
                                        onClick = { pickupLocation = hub },
                                        label = { Text(hub, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = dropLocation,
                                onValueChange = { dropLocation = it },
                                label = { Text("Drop Address (Bengaluru)") },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_drop_location")
                            )

                            // Quick Bangalore Hub Chips for Drop
                            Text(
                                text = "Popular Destinations:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(bangaloreHubs.reversed()) { hub ->
                                    FilterChip(
                                        selected = dropLocation == hub,
                                        onClick = { dropLocation = hub },
                                        label = { Text(hub, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Distance Slider
                            Text(
                                text = "Estimated Distance: ${distanceKm.toInt()} km",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Slider(
                                value = distanceKm,
                                onValueChange = { distanceKm = it },
                                valueRange = 3.0f..40.0f,
                                steps = 37,
                                modifier = Modifier.testTag("slider_distance")
                            )
                        }
                    }
                }

                // Goods Category Selection
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Select Goods Type",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(GoodsCategory.entries) { cat ->
                                    FilterChip(
                                        selected = selectedGoodsCat == cat,
                                        onClick = { selectedGoodsCat = cat },
                                        label = { Text(cat.label) },
                                        leadingIcon = {
                                            if (selectedGoodsCat == cat) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Select Vehicle Type
                item {
                    Text(
                        text = "Select Vehicle",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(VehicleType.entries) { vehicle ->
                    val estFare = vehicle.baseFare + (distanceKm * vehicle.perKmRate)
                    VehicleSelectionCard(
                        vehicleType = vehicle,
                        isSelected = selectedVehicle == vehicle,
                        estimatedFare = estFare,
                        onSelect = { selectedVehicle = vehicle }
                    )
                }

                // Book Action Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onBookVehicle(
                                customerName,
                                customerPhone,
                                pickupLocation,
                                dropLocation,
                                selectedGoodsCat,
                                selectedVehicle,
                                distanceKm.toDouble()
                            )
                            selectedTab = 1 // Switch to orders tab
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("book_trip_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Book ${selectedVehicle.title} • ₹${currentFare.toInt()} (CASH)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        } else {
            // Customer Orders List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (bookings.isEmpty()) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        ) {
                            Text(
                                text = "No active bookings found. Book your first vehicle above!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(bookings, key = { it.id }) { booking ->
                        CustomerBookingCard(booking = booking)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerBookingCard(booking: BookingEntity) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Booking #${booking.id}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${booking.vehicleType.title})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BookingStatusChip(status = booking.status)
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            // Route Details
            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CashGreen))
                    Box(modifier = Modifier.width(2.dp).height(24.dp).background(Color.Gray))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "FROM: ${booking.pickupLocation}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "TO: ${booking.dropLocation}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fare & Payment Mode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "Goods: ${booking.goodsCategory.label}", style = MaterialTheme.typography.labelSmall)
                    Text(text = "Distance: ${booking.distanceKm} km", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${booking.fareAmount.toInt()}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = CashGreen)
                    )
                    Text(
                        text = "Pay Cash to Driver",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CashGreen)
                    )
                }
            }

            // Driver Information if assigned
            if (booking.driverName != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Driver: ${booking.driverName} (${booking.driverVehicleNo})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Phone: ${booking.driverPhone}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
