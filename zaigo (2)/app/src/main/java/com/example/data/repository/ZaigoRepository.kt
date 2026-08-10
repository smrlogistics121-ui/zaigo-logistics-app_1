package com.example.data.repository

import com.example.data.db.BookingDao
import com.example.data.db.DriverDao
import com.example.data.db.WalletTransactionDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ZaigoRepository(
    private val driverDao: DriverDao,
    private val bookingDao: BookingDao,
    private val walletTransactionDao: WalletTransactionDao
) {
    val allDrivers: Flow<List<DriverEntity>> = driverDao.getAllDrivers()
    val allBookings: Flow<List<BookingEntity>> = bookingDao.getAllBookings()
    val searchingBookings: Flow<List<BookingEntity>> = bookingDao.getBookingsByStatus(BookingStatus.SEARCHING_DRIVER)
    val allTransactions: Flow<List<WalletTransactionEntity>> = walletTransactionDao.getAllTransactions()

    fun getDriverByIdFlow(driverId: Long): Flow<DriverEntity?> = driverDao.getDriverByIdFlow(driverId)
    fun getBookingsForDriver(driverId: Long): Flow<List<BookingEntity>> = bookingDao.getBookingsForDriver(driverId)
    fun getTransactionsForDriver(driverId: Long): Flow<List<WalletTransactionEntity>> = walletTransactionDao.getTransactionsForDriver(driverId)
    fun getBookingByIdFlow(bookingId: Long): Flow<BookingEntity?> = bookingDao.getBookingByIdFlow(bookingId)

    suspend fun seedInitialDataIfEmpty() {
        val drivers = driverDao.getAllDrivers().first()
        if (drivers.isEmpty()) {
            // Driver 1: Ramesh Kumar - Active, Approved, ₹150 wallet balance
            val driver1Id = driverDao.insertDriver(
                DriverEntity(
                    name = "Ramesh Kumar",
                    phone = "+91 98765 43210",
                    vehicleType = VehicleType.TATA_ACE,
                    vehicleNumber = "KA-04-MB-8821",
                    city = "Bangalore",
                    registrationFeePaid = true,
                    kycStatus = KYCStatus.APPROVED,
                    aadhaarNo = "5432-8890-1234",
                    dlNo = "KA-0420210098765",
                    rcNo = "RC-KA04MB8821",
                    walletBalance = 150.0,
                    isLocked = false,
                    totalTripsCompleted = 12,
                    totalCashCollected = 4850.0
                )
            )
            walletTransactionDao.insertTransaction(
                WalletTransactionEntity(
                    driverId = driver1Id,
                    amount = -49.0,
                    type = WalletTxType.REGISTRATION_FEE,
                    description = "One-time Driver Registration Fee Paid"
                )
            )
            walletTransactionDao.insertTransaction(
                WalletTransactionEntity(
                    driverId = driver1Id,
                    amount = 200.0,
                    type = WalletTxType.TOPUP,
                    description = "Initial Wallet Top-up"
                )
            )
            walletTransactionDao.insertTransaction(
                WalletTransactionEntity(
                    driverId = driver1Id,
                    amount = -20.0,
                    type = WalletTxType.DAILY_DEDUCTION,
                    description = "Daily Platform Deduction Fee"
                )
            )

            // Driver 2: Suresh Gowda - LOCKED! Balance ₹30 <= ₹40 threshold
            val driver2Id = driverDao.insertDriver(
                DriverEntity(
                    name = "Suresh Gowda",
                    phone = "+91 99801 12345",
                    vehicleType = VehicleType.THREE_WHEELER,
                    vehicleNumber = "KA-05-AB-4321",
                    city = "Bangalore",
                    registrationFeePaid = true,
                    kycStatus = KYCStatus.APPROVED,
                    aadhaarNo = "8890-1234-5678",
                    dlNo = "KA-0520200012345",
                    rcNo = "RC-KA05AB4321",
                    walletBalance = 30.0, // <= ₹40 lock threshold!
                    isLocked = true,
                    totalTripsCompleted = 8,
                    totalCashCollected = 2400.0
                )
            )
            walletTransactionDao.insertTransaction(
                WalletTransactionEntity(
                    driverId = driver2Id,
                    amount = -20.0,
                    type = WalletTxType.DAILY_DEDUCTION,
                    description = "Daily Deduction Fee (Account Locked: Balance ≤ ₹40)"
                )
            )

            // Driver 3: Venkatesh M - Pending KYC Verification
            val driver3Id = driverDao.insertDriver(
                DriverEntity(
                    name = "Venkatesh M",
                    phone = "+91 97400 99887",
                    vehicleType = VehicleType.PICKUP_8FT,
                    vehicleNumber = "KA-02-PE-9090",
                    city = "Bangalore",
                    registrationFeePaid = true,
                    kycStatus = KYCStatus.PENDING_APPROVAL,
                    aadhaarNo = "1122-3344-5566",
                    dlNo = "KA-0220220054321",
                    rcNo = "RC-KA02PE9090",
                    walletBalance = 100.0,
                    isLocked = false,
                    totalTripsCompleted = 0,
                    totalCashCollected = 0.0
                )
            )

            // Driver 4: Manjunath N - New Sign-up, Unpaid ₹49 Fee
            val driver4Id = driverDao.insertDriver(
                DriverEntity(
                    name = "Manjunath N",
                    phone = "+91 96112 33445",
                    vehicleType = VehicleType.TWO_WHEELER,
                    vehicleNumber = "KA-51-EX-7711",
                    city = "Bangalore",
                    registrationFeePaid = false,
                    kycStatus = KYCStatus.NOT_SUBMITTED,
                    walletBalance = 0.0,
                    isLocked = true,
                    totalTripsCompleted = 0,
                    totalCashCollected = 0.0
                )
            )

            // Seed Initial Bangalore Bookings
            bookingDao.insertBooking(
                BookingEntity(
                    customerName = "Anand Electricals",
                    customerPhone = "+91 98450 11223",
                    pickupLocation = "Peenya Industrial Area Phase 1, Bangalore",
                    dropLocation = "Koramangala 5th Block, Bangalore",
                    goodsCategory = GoodsCategory.ELECTRICAL_HARDWARE,
                    vehicleType = VehicleType.TATA_ACE,
                    distanceKm = 18.5,
                    fareAmount = 712.0,
                    paymentMode = "CASH_ON_DELIVERY",
                    status = BookingStatus.SEARCHING_DRIVER
                )
            )

            bookingDao.insertBooking(
                BookingEntity(
                    customerName = "Priya Furniture",
                    customerPhone = "+91 99000 55443",
                    pickupLocation = "Indiranagar 100ft Road, Bangalore",
                    dropLocation = "Whitefield ITPL Main Rd, Bangalore",
                    goodsCategory = GoodsCategory.HOUSEHOLD_FURNITURE,
                    vehicleType = VehicleType.THREE_WHEELER,
                    distanceKm = 14.2,
                    fareAmount = 375.0,
                    paymentMode = "CASH_ON_DELIVERY",
                    status = BookingStatus.SEARCHING_DRIVER
                )
            )

            bookingDao.insertBooking(
                BookingEntity(
                    customerName = "Kavitha Traders",
                    customerPhone = "+91 97311 88220",
                    pickupLocation = "HSR Layout Sector 1, Bangalore",
                    dropLocation = "Electronic City Phase 1, Bangalore",
                    goodsCategory = GoodsCategory.DOCUMENTS_PARCELS,
                    vehicleType = VehicleType.TWO_WHEELER,
                    distanceKm = 9.8,
                    fareAmount = 138.0,
                    paymentMode = "CASH_ON_DELIVERY",
                    status = BookingStatus.DELIVERED_CASH_COLLECTED,
                    driverId = driver1Id,
                    driverName = "Ramesh Kumar",
                    driverPhone = "+91 98765 43210",
                    driverVehicleNo = "KA-04-MB-8821",
                    completedAt = System.currentTimeMillis() - 3600000
                )
            )
        }
    }

    // Wallet & Driver Actions
    suspend fun payRegistrationFee(driverId: Long): Boolean {
        val driver = driverDao.getDriverById(driverId) ?: return false
        val updated = driver.copy(
            registrationFeePaid = true,
            walletBalance = driver.walletBalance,
            isLocked = if (driver.walletBalance > 40.0 && driver.kycStatus == KYCStatus.APPROVED) false else driver.isLocked
        )
        driverDao.updateDriver(updated)
        walletTransactionDao.insertTransaction(
            WalletTransactionEntity(
                driverId = driverId,
                amount = -49.0,
                type = WalletTxType.REGISTRATION_FEE,
                description = "₹49 One-time Registration Fee Paid"
            )
        )
        return true
    }

    suspend fun submitKYCDocuments(
        driverId: Long,
        aadhaar: String,
        dl: String,
        rc: String
    ): Boolean {
        val driver = driverDao.getDriverById(driverId) ?: return false
        val updated = driver.copy(
            aadhaarNo = aadhaar,
            dlNo = dl,
            rcNo = rc,
            kycStatus = KYCStatus.PENDING_APPROVAL
        )
        driverDao.updateDriver(updated)
        return true
    }

    suspend fun approveDriverKYC(driverId: Long): Boolean {
        val driver = driverDao.getDriverById(driverId) ?: return false
        val isLockedNow = driver.walletBalance <= 40.0 || !driver.registrationFeePaid
        val updated = driver.copy(
            kycStatus = KYCStatus.APPROVED,
            isLocked = isLockedNow
        )
        driverDao.updateDriver(updated)
        return true
    }

    suspend fun rejectDriverKYC(driverId: Long): Boolean {
        val driver = driverDao.getDriverById(driverId) ?: return false
        val updated = driver.copy(
            kycStatus = KYCStatus.REJECTED,
            isLocked = true
        )
        driverDao.updateDriver(updated)
        return true
    }

    suspend fun deductDailyFee(driverId: Long): Double {
        val driver = driverDao.getDriverById(driverId) ?: return 0.0
        val newBalance = driver.walletBalance - 20.0
        val lockedNow = newBalance <= 40.0
        val updated = driver.copy(
            walletBalance = newBalance,
            isLocked = lockedNow
        )
        driverDao.updateDriver(updated)
        walletTransactionDao.insertTransaction(
            WalletTransactionEntity(
                driverId = driverId,
                amount = -20.0,
                type = WalletTxType.DAILY_DEDUCTION,
                description = "Daily ₹20 Platform Fee Deduction ${if (lockedNow) "(Account LOCKED: Balance ≤ ₹40)" else ""}"
            )
        )
        return newBalance
    }

    suspend fun deductDailyFeeAllActiveDrivers(): Int {
        val list = driverDao.getAllDrivers().first()
        var count = 0
        for (driver in list) {
            if (driver.registrationFeePaid && driver.kycStatus == KYCStatus.APPROVED) {
                deductDailyFee(driver.id)
                count++
            }
        }
        return count
    }

    suspend fun topUpWallet(driverId: Long, amount: Double, note: String = "Wallet Recharge"): Double {
        val driver = driverDao.getDriverById(driverId) ?: return 0.0
        val newBalance = driver.walletBalance + amount
        val isUnlocked = newBalance > 40.0 && driver.registrationFeePaid && driver.kycStatus == KYCStatus.APPROVED
        val updated = driver.copy(
            walletBalance = newBalance,
            isLocked = !isUnlocked
        )
        driverDao.updateDriver(updated)
        walletTransactionDao.insertTransaction(
            WalletTransactionEntity(
                driverId = driverId,
                amount = amount,
                type = WalletTxType.TOPUP,
                description = note
            )
        )
        return newBalance
    }

    suspend fun createNewDriver(
        name: String,
        phone: String,
        vehicleType: VehicleType,
        vehicleNumber: String
    ): Long {
        val newDriver = DriverEntity(
            name = name,
            phone = phone,
            vehicleType = vehicleType,
            vehicleNumber = vehicleNumber,
            city = "Bangalore",
            registrationFeePaid = false,
            kycStatus = KYCStatus.NOT_SUBMITTED,
            walletBalance = 0.0,
            isLocked = true
        )
        return driverDao.insertDriver(newDriver)
    }

    suspend fun createBooking(
        customerName: String,
        customerPhone: String,
        pickupLocation: String,
        dropLocation: String,
        goodsCategory: GoodsCategory,
        vehicleType: VehicleType,
        distanceKm: Double,
        fareAmount: Double
    ): Long {
        val newBooking = BookingEntity(
            customerName = customerName,
            customerPhone = customerPhone,
            pickupLocation = pickupLocation,
            dropLocation = dropLocation,
            goodsCategory = goodsCategory,
            vehicleType = vehicleType,
            distanceKm = distanceKm,
            fareAmount = fareAmount,
            paymentMode = "CASH_ON_DELIVERY",
            status = BookingStatus.SEARCHING_DRIVER
        )
        return bookingDao.insertBooking(newBooking)
    }

    suspend fun acceptBooking(bookingId: Long, driverId: Long): Result<Unit> {
        val driver = driverDao.getDriverById(driverId)
            ?: return Result.failure(Exception("Driver not found."))

        if (!driver.registrationFeePaid) {
            return Result.failure(Exception("Cannot accept trip: ₹49 Registration Fee is pending."))
        }
        if (driver.kycStatus != KYCStatus.APPROVED) {
            return Result.failure(Exception("Cannot accept trip: Driver KYC document verification is pending approval."))
        }
        if (driver.walletBalance <= 40.0 || driver.isLocked) {
            return Result.failure(Exception("ACCOUNT LOCKED! Wallet balance ₹${driver.walletBalance.toInt()} is ≤ ₹40 threshold. Please top up wallet to accept trips."))
        }

        val booking = bookingDao.getBookingById(bookingId)
            ?: return Result.failure(Exception("Booking not found."))

        if (booking.status != BookingStatus.SEARCHING_DRIVER) {
            return Result.failure(Exception("Booking is no longer available."))
        }

        val updatedBooking = booking.copy(
            status = BookingStatus.ACCEPTED,
            driverId = driver.id,
            driverName = driver.name,
            driverPhone = driver.phone,
            driverVehicleNo = driver.vehicleNumber
        )
        bookingDao.updateBooking(updatedBooking)
        return Result.success(Unit)
    }

    suspend fun updateBookingStatus(bookingId: Long, newStatus: BookingStatus): Boolean {
        val booking = bookingDao.getBookingById(bookingId) ?: return false
        val updatedBooking = booking.copy(
            status = newStatus,
            completedAt = if (newStatus == BookingStatus.DELIVERED_CASH_COLLECTED) System.currentTimeMillis() else booking.completedAt
        )
        bookingDao.updateBooking(updatedBooking)

        if (newStatus == BookingStatus.DELIVERED_CASH_COLLECTED && booking.driverId != null) {
            val driver = driverDao.getDriverById(booking.driverId)
            if (driver != null) {
                driverDao.updateDriver(
                    driver.copy(
                        totalTripsCompleted = driver.totalTripsCompleted + 1,
                        totalCashCollected = driver.totalCashCollected + booking.fareAmount
                    )
                )
            }
        }
        return true
    }
}
