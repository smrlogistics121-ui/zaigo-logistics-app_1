package com.example.data.db

import androidx.room.*
import com.example.data.model.BookingEntity
import com.example.data.model.BookingStatus
import com.example.data.model.DriverEntity
import com.example.data.model.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverDao {
    @Query("SELECT * FROM drivers ORDER BY id DESC")
    fun getAllDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE id = :driverId")
    fun getDriverByIdFlow(driverId: Long): Flow<DriverEntity?>

    @Query("SELECT * FROM drivers WHERE id = :driverId")
    suspend fun getDriverById(driverId: Long): DriverEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity): Long

    @Update
    suspend fun updateDriver(driver: DriverEntity)

    @Query("DELETE FROM drivers WHERE id = :driverId")
    suspend fun deleteDriver(driverId: Long)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE status = :status ORDER BY createdAt DESC")
    fun getBookingsByStatus(status: BookingStatus): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE driverId = :driverId ORDER BY createdAt DESC")
    fun getBookingsForDriver(driverId: Long): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    fun getBookingByIdFlow(bookingId: Long): Flow<BookingEntity?>

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    suspend fun getBookingById(bookingId: Long): BookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Update
    suspend fun updateBooking(booking: BookingEntity)
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions WHERE driverId = :driverId ORDER BY timestamp DESC")
    fun getTransactionsForDriver(driverId: Long): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity): Long
}
