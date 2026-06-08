package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.data.model.Ride
import com.example.data.model.WalletTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DolphinDao {
    @Query("SELECT * FROM rides ORDER BY id DESC")
    fun getAllRides(): Flow<List<Ride>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: Ride)

    @Query("SELECT * FROM wallet_transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction)

    @Query("DELETE FROM rides")
    suspend fun clearRides()

    @Query("DELETE FROM wallet_transactions")
    suspend fun clearTransactions()
}

@Database(entities = [Ride::class, WalletTransaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dolphinDao(): DolphinDao
}
