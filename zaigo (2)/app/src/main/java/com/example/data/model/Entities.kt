package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val vehicleType: VehicleType,
    val vehicleNumber: String,
    val city: String = "Bangalore",
    val registrationFeePaid: Boolean = false, // ₹49
    val kycStatus: KYCStatus = KYCStatus.NOT_SUBMITTED,
    val aadhaarNo: String = "",
    val dlNo: String = "",
    val rcNo: String = "",
    val walletBalance: Double = 100.0,
    val isLocked: Boolean = false, // Locked if wallet <= 40.0
    val totalTripsCompleted: Int = 0,
    val totalCashCollected: Double = 0.0,
    val registeredTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val customerPhone: String,
    val pickupLocation: String,
    val dropLocation: String,
    val goodsCategory: GoodsCategory,
    val vehicleType: VehicleType,
    val distanceKm: Double,
    val fareAmount: Double, // Customer pays this exact amount in CASH to driver
    val paymentMode: String = "CASH_ON_DELIVERY",
    val status: BookingStatus = BookingStatus.SEARCHING_DRIVER,
    val driverId: Long? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val driverVehicleNo: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val driverId: Long,
    val amount: Double, // Positive for credit/topup, negative for deduction
    val type: WalletTxType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
