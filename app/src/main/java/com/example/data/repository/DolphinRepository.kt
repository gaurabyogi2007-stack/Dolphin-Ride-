package com.example.data.repository

import com.example.data.db.DolphinDao
import com.example.data.model.Ride
import com.example.data.model.WalletTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class DolphinRepository(private val dao: DolphinDao) {
    val allRides: Flow<List<Ride>> = dao.getAllRides()
    val allTransactions: Flow<List<WalletTransaction>> = dao.getAllTransactions()

    suspend fun insertRide(ride: Ride) {
        dao.insertRide(ride)
    }

    suspend fun insertTransaction(transaction: WalletTransaction) {
        dao.insertTransaction(transaction)
    }

    suspend fun prepopulateIfEmpty() {
        // Check if there are existing rides
        val currentRides = dao.getAllRides().firstOrNull()
        if (currentRides.isNullOrEmpty()) {
            // Seed rides
            val sampleRides = listOf(
                Ride(
                    fromLocation = "Thamel",
                    toLocation = "New Road",
                    type = "bike",
                    subtype = "standard",
                    price = 75.0,
                    dateStr = "Today",
                    timeStr = "10:30 AM",
                    status = "completed",
                    driverName = "Sagar Thapa",
                    rating = 5
                ),
                Ride(
                    fromLocation = "Pokhara Airport",
                    toLocation = "Lakeside",
                    type = "auto",
                    subtype = "standard",
                    price = 350.0,
                    dateStr = "Today",
                    timeStr = "11:45 AM",
                    status = "completed",
                    driverName = "Anita Gurung",
                    rating = 4
                ),
                Ride(
                    fromLocation = "Bhaktapur",
                    toLocation = "Kathmandu",
                    type = "car",
                    subtype = "comfort",
                    price = 820.0,
                    dateStr = "Yesterday",
                    timeStr = "2:15 PM",
                    status = "completed",
                    driverName = "Rajesh Hamal",
                    rating = 5
                ),
                Ride(
                    fromLocation = "Durbar Square",
                    toLocation = "Boudha",
                    type = "bike",
                    subtype = "electric",
                    price = 45.0,
                    dateStr = "Yesterday",
                    timeStr = "5:30 PM",
                    status = "completed",
                    driverName = "Prakash Rai",
                    rating = 4
                )
            )
            for (ride in sampleRides) {
                dao.insertRide(ride)
            }
        }

        // Seed wallet transactions
        val currentTxs = dao.getAllTransactions().firstOrNull()
        if (currentTxs.isNullOrEmpty()) {
            val sampleTxs = listOf(
                WalletTransaction(
                    type = "earning",
                    desc = "Bike - New Road to Thamel",
                    amount = 75.0,
                    timeStr = "10:30 AM",
                    dateStr = "2026-06-08",
                    status = "completed",
                    commission = 3.75
                ),
                WalletTransaction(
                    type = "earning",
                    desc = "Auto - Pokhara Airport",
                    amount = 350.0,
                    timeStr = "11:45 AM",
                    dateStr = "2026-06-08",
                    status = "completed",
                    commission = 17.50
                ),
                WalletTransaction(
                    type = "commission",
                    desc = "Commission Credit (5%)",
                    amount = 21.25,
                    timeStr = "12:00 PM",
                    dateStr = "2026-06-08",
                    status = "completed"
                ),
                WalletTransaction(
                    type = "earning",
                    desc = "Car - Bhaktapur to KTM",
                    amount = 820.0,
                    timeStr = "02:15 PM",
                    dateStr = "2026-06-07",
                    status = "completed",
                    commission = 41.0
                ),
                WalletTransaction(
                    type = "earning",
                    desc = "Bike - Durbar Square",
                    amount = 60.0,
                    timeStr = "05:30 PM",
                    dateStr = "2026-06-07",
                    status = "completed",
                    commission = 3.0
                ),
                WalletTransaction(
                    type = "earning",
                    desc = "Premium Car - KTM Airport",
                    amount = 1200.0,
                    timeStr = "08:00 PM",
                    dateStr = "2026-06-06",
                    status = "completed",
                    commission = 60.0
                )
            )
            for (tx in sampleTxs) {
                dao.insertTransaction(tx)
            }
        }
    }
}
