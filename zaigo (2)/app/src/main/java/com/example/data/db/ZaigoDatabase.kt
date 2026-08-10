package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.BookingEntity
import com.example.data.model.DriverEntity
import com.example.data.model.WalletTransactionEntity

@Database(
    entities = [DriverEntity::class, BookingEntity::class, WalletTransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ZaigoDatabase : RoomDatabase() {
    abstract fun driverDao(): DriverDao
    abstract fun bookingDao(): BookingDao
    abstract fun walletTransactionDao(): WalletTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: ZaigoDatabase? = null

        fun getDatabase(context: Context): ZaigoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZaigoDatabase::class.java,
                    "zaigo_logistics_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
