package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ZaigoDatabase
import com.example.data.model.*
import com.example.data.repository.ZaigoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ZaigoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ZaigoDatabase.getDatabase(application)
    val repository = ZaigoRepository(db.driverDao(), db.bookingDao(), db.walletTransactionDao())

    // Active Role State
    private val _currentRole = MutableStateFlow(AppRole.CUSTOMER)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    // Selected Driver State for Driver View
    private val _selectedDriverId = MutableStateFlow<Long>(1L)
    val selectedDriverId: StateFlow<Long> = _selectedDriverId.asStateFlow()

    // Alert / Toast Messages
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val allDrivers: StateFlow<List<DriverEntity>> = repository.allDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookings: StateFlow<List<BookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchingBookings: StateFlow<List<BookingEntity>> = repository.searchingBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<WalletTransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentDriver: StateFlow<DriverEntity?> = _selectedDriverId.flatMapLatest { id ->
        repository.getDriverByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentDriverTransactions: StateFlow<List<WalletTransactionEntity>> = _selectedDriverId.flatMapLatest { id ->
        repository.getTransactionsForDriver(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentDriverBookings: StateFlow<List<BookingEntity>> = _selectedDriverId.flatMapLatest { id ->
        repository.getBookingsForDriver(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setRole(role: AppRole) {
        _currentRole.value = role
    }

    fun selectDriver(driverId: Long) {
        _selectedDriverId.value = driverId
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // Customer Actions
    fun bookGoodsVehicle(
        customerName: String,
        customerPhone: String,
        pickupLocation: String,
        dropLocation: String,
        goodsCategory: GoodsCategory,
        vehicleType: VehicleType,
        distanceKm: Double
    ) {
        viewModelScope.launch {
            val fare = vehicleType.baseFare + (distanceKm * vehicleType.perKmRate)
            val id = repository.createBooking(
                customerName = if (customerName.isBlank()) "Bangalore Customer" else customerName,
                customerPhone = if (customerPhone.isBlank()) "+91 98765 00000" else customerPhone,
                pickupLocation = pickupLocation,
                dropLocation = dropLocation,
                goodsCategory = goodsCategory,
                vehicleType = vehicleType,
                distanceKm = distanceKm,
                fareAmount = fare
            )
            _userMessage.value = "Booking #$id created! Customer will pay ₹${fare.toInt()} Cash to driver."
        }
    }

    // Driver Actions
    fun payDriverRegistrationFee() {
        viewModelScope.launch {
            val driverId = _selectedDriverId.value
            val success = repository.payRegistrationFee(driverId)
            if (success) {
                _userMessage.value = "₹49 Registration Fee paid successfully!"
            }
        }
    }

    fun submitDriverKYC(aadhaar: String, dl: String, rc: String) {
        viewModelScope.launch {
            val driverId = _selectedDriverId.value
            if (aadhaar.isBlank() || dl.isBlank() || rc.isBlank()) {
                _userMessage.value = "Please fill Aadhaar, Driving License, and RC details."
                return@launch
            }
            val success = repository.submitKYCDocuments(driverId, aadhaar, dl, rc)
            if (success) {
                _userMessage.value = "Documents submitted! Pending Admin verification."
            }
        }
    }

    fun driverTopUpWallet(amount: Double) {
        viewModelScope.launch {
            val driverId = _selectedDriverId.value
            val newBal = repository.topUpWallet(driverId, amount, "Driver Self Wallet Recharge")
            if (newBal > 40.0) {
                _userMessage.value = "Wallet recharged by ₹${amount.toInt()}! Balance: ₹${newBal.toInt()}. Account Unlocked!"
            } else {
                _userMessage.value = "Recharged ₹${amount.toInt()}. Balance: ₹${newBal.toInt()} (Still ≤ ₹40 limit)."
            }
        }
    }

    fun driverAcceptTrip(bookingId: Long) {
        viewModelScope.launch {
            val driverId = _selectedDriverId.value
            val result = repository.acceptBooking(bookingId, driverId)
            result.onSuccess {
                _userMessage.value = "Trip accepted! Navigate to pickup point."
            }.onFailure { ex ->
                _userMessage.value = ex.message ?: "Failed to accept trip."
            }
        }
    }

    fun updateTripStatus(bookingId: Long, newStatus: BookingStatus) {
        viewModelScope.launch {
            val success = repository.updateBookingStatus(bookingId, newStatus)
            if (success) {
                val label = when (newStatus) {
                    BookingStatus.ARRIVED_PICKUP -> "Arrived at pickup point"
                    BookingStatus.GOODS_LOADED -> "Goods loaded & in transit"
                    BookingStatus.DELIVERED_CASH_COLLECTED -> "Delivered! Cash payment collected from customer."
                    else -> newStatus.displayLabel
                }
                _userMessage.value = "Trip status: $label"
            }
        }
    }

    fun createNewDriver(name: String, phone: String, vehicleType: VehicleType, vehicleNumber: String) {
        viewModelScope.launch {
            if (name.isBlank() || vehicleNumber.isBlank()) {
                _userMessage.value = "Please enter driver name and vehicle number."
                return@launch
            }
            val id = repository.createNewDriver(name, phone, vehicleType, vehicleNumber)
            _selectedDriverId.value = id
            _userMessage.value = "New driver profile created! Pay ₹49 fee and upload KYC to activate."
        }
    }

    // Admin Actions
    fun approveDriverKYC(driverId: Long) {
        viewModelScope.launch {
            repository.approveDriverKYC(driverId)
            _userMessage.value = "Driver KYC Approved!"
        }
    }

    fun rejectDriverKYC(driverId: Long) {
        viewModelScope.launch {
            repository.rejectDriverKYC(driverId)
            _userMessage.value = "Driver KYC Rejected."
        }
    }

    fun adminDeductDailyFee(driverId: Long) {
        viewModelScope.launch {
            val newBal = repository.deductDailyFee(driverId)
            _userMessage.value = "Daily ₹20 fee deducted. New balance: ₹${newBal.toInt()}"
        }
    }

    fun adminDeductAllActiveDrivers() {
        viewModelScope.launch {
            val count = repository.deductDailyFeeAllActiveDrivers()
            _userMessage.value = "Daily ₹20 fee deducted for $count active drivers."
        }
    }

    fun adminTopUpDriverWallet(driverId: Long, amount: Double) {
        viewModelScope.launch {
            val newBal = repository.topUpWallet(driverId, amount, "Admin Credit / Wallet Topup")
            _userMessage.value = "Admin credited ₹${amount.toInt()} to driver. New Balance: ₹${newBal.toInt()}"
        }
    }
}
