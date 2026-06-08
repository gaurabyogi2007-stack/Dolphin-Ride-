package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.model.Ride
import com.example.data.model.WalletTransaction
import com.example.data.network.GeminiApiClient
import com.example.data.repository.DolphinRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    Splash, Login, FaceVerify, Home, Rides, Wallet, Owner, Settings
}

data class ChatMessage(
    val id: Long = System.currentTimeMillis() + (0..1000).random(),
    val text: String,
    val isUser: Boolean
)

data class Driver(
    val name: String,
    val vehicle: String,
    val phone: String,
    val rating: Float
)

class DolphinViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "dolphin_database"
    ).fallbackToDestructiveMigration().build()

    private val repository = DolphinRepository(db.dolphinDao())

    // Language State
    private val _currentLang = MutableStateFlow("ne") // Default is ne
    val currentLang: StateFlow<String> = _currentLang.asStateFlow()

    // Screen Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.Splash)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // User State
    private val _userPhone = MutableStateFlow("")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userName = MutableStateFlow("Ramesh Sharma")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _walletBalance = MutableStateFlow(1250.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    // Owner Auth
    private val _isOwnerAuthenticated = MutableStateFlow(false)
    val isOwnerAuthenticated: StateFlow<Boolean> = _isOwnerAuthenticated.asStateFlow()

    // Database Flows
    val ridesList: StateFlow<List<Ride>> = repository.allRides.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactionsList: StateFlow<List<WalletTransaction>> = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Calculated Owner Stats from Room items
    val ownerTodayEarnings: StateFlow<Double> = transactionsList.map { txList ->
        val earnings = txList.filter { it.type == "earning" || it.type == "commission" }.sumOf { it.commission }
        // Base seed owner earnings
        1820.0 + earnings
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1820.0)

    val ownerTotalEarnings: StateFlow<Double> = transactionsList.map { txList ->
        val earnings = txList.filter { it.type == "earning" || it.type == "commission" }.sumOf { it.commission }
        45890.0 + earnings
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 45890.0)

    val ownerTodayRides: StateFlow<Int> = ridesList.map { rList ->
        18 + rList.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 18)

    val ownerTotalRides: StateFlow<Int> = ridesList.map { rList ->
        1247 + rList.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1247)

    // Active Ride flows
    private val _fromLocation = MutableStateFlow("")
    val fromLocation: StateFlow<String> = _fromLocation.asStateFlow()

    private val _toLocation = MutableStateFlow("")
    val toLocation: StateFlow<String> = _toLocation.asStateFlow()

    private val _isSearchingRides = MutableStateFlow(false)
    val isSearchingRides: StateFlow<Boolean> = _isSearchingRides.asStateFlow()

    private val _activeSubtypeSelected = MutableStateFlow<String?>(null) // standard, electric, etc.
    val activeSubtypeSelected: StateFlow<String?> = _activeSubtypeSelected.asStateFlow()

    private val _showSubtypeDialogFor = MutableStateFlow<String?>(null) // "bike", "auto", "car" or null
    val showSubtypeDialogFor: StateFlow<String?> = _showSubtypeDialogFor.asStateFlow()

    private val _activeRideTracking = MutableStateFlow<Ride?>(null)
    val activeRideTracking: StateFlow<Ride?> = _activeRideTracking.asStateFlow()

    private val _activeTrackingStep = MutableStateFlow(0) // 0: Confirmed, 1: Arriving, 2: Started, 3: Completed
    val activeTrackingStep: StateFlow<Int> = _activeTrackingStep.asStateFlow()

    private val _activeDriver = MutableStateFlow<Driver?>(null)
    val activeDriver: StateFlow<Driver?> = _activeDriver.asStateFlow()

    private val _activeOtp = MutableStateFlow("")
    val activeOtp: StateFlow<String> = _activeOtp.asStateFlow()

    private val _activePrice = MutableStateFlow(0.0)
    val activePrice: StateFlow<Double> = _activePrice.asStateFlow()

    private val _activeDistance = MutableStateFlow(0.0)
    val activeDistance: StateFlow<Double> = _activeDistance.asStateFlow()

    // Gemini fare estimation states
    private val _isEstimatingFare = MutableStateFlow(false)
    val isEstimatingFare: StateFlow<Boolean> = _isEstimatingFare.asStateFlow()

    private val _geminiFareEstimation = MutableStateFlow<com.example.data.network.FareEstimation?>(null)
    val geminiFareEstimation: StateFlow<com.example.data.network.FareEstimation?> = _geminiFareEstimation.asStateFlow()

    fun estimateFare(from: String, to: String, category: String) {
        if (from.isBlank() || to.isBlank()) return
        viewModelScope.launch {
            _isEstimatingFare.value = true
            _geminiFareEstimation.value = null
            try {
                val estimation = com.example.data.network.GeminiApiClient.estimateFareWithGemini(from, to, category)
                _geminiFareEstimation.value = estimation
                _activePrice.value = estimation.fare
                _activeDistance.value = estimation.distance
            } catch (e: java.lang.Exception) {
                // Handled gracefully below or inside API helper
            } finally {
                _isEstimatingFare.value = false
            }
        }
    }

    fun clearFareEstimation() {
        _geminiFareEstimation.value = null
    }

    private val _showRatingDialog = MutableStateFlow(false)
    val showRatingDialog: StateFlow<Boolean> = _showRatingDialog.asStateFlow()

    private val _currentRatingSelected = MutableStateFlow(5)
    val currentRatingSelected: StateFlow<Int> = _currentRatingSelected.asStateFlow()

    // AI Chat Assistant State
    private val _aiChatOpen = MutableStateFlow(false)
    val aiChatOpen: StateFlow<Boolean> = _aiChatOpen.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    // Sample drivers
    val drivers = listOf(
        Driver("Sagar Thapa", "BA 1 PA 1234", "9841230001", 4.8f),
        Driver("Anita Gurung", "BA 1 PA 5678", "9841230002", 4.6f),
        Driver("Rajesh Hamal", "BA 1 PA 9012", "9841230003", 4.9f),
        Driver("Prakash Rai", "BA 1 PA 3456", "9841230004", 4.7f),
        Driver("Sita Poudel", "BA 1 PA 7890", "9841230005", 4.5f)
    )

    init {
        // Prepopulate local storage
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // Toggle language
    fun toggleLang() {
        _currentLang.value = if (_currentLang.value == "ne") "en" else "ne"
    }

    fun setLang(lang: String) {
        _currentLang.value = lang
    }

    // Navigation helper
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen == AppScreen.Home) {
            _showSubtypeDialogFor.value = null
        }
    }

    // Login Action
    fun login(phone: String) {
        _userPhone.value = phone
        _userName.value = if (phone.endsWith("1234")) "Ramesh Sharma" else "Dolphin Rider"
        navigateTo(AppScreen.FaceVerify)
    }

    // Logout Action
    fun logout() {
        _userPhone.value = ""
        _userName.value = "Ramesh Sharma"
        _isOwnerAuthenticated.value = false
        navigateTo(AppScreen.Login)
    }

    // Owner Login
    fun authenticateOwner(phone: String) {
        _isOwnerAuthenticated.value = true
        navigateTo(AppScreen.Owner)
    }

    // Quick address selection
    fun setLocations(from: String, to: String) {
        _fromLocation.value = from
        _toLocation.value = to
    }

    fun updateFromLocation(from: String) {
        _fromLocation.value = from
    }

    fun updateToLocation(to: String) {
        _toLocation.value = to
    }

    // Trigger Search
    fun searchRides() {
        if (_toLocation.value.trim().isEmpty()) {
            return
        }
        viewModelScope.launch {
            _isSearchingRides.value = true
            delay(1500) // simulated network lag
            _isSearchingRides.value = false
        }
    }

    // Show/Close category subtype sheet
    fun selectCategory(category: String?) {
        _showSubtypeDialogFor.value = category
    }

    // Handle ride subtype confirm
    fun initiateBooking(type: String, subtype: String) {
        _activeSubtypeSelected.value = subtype
        _showSubtypeDialogFor.value = null

        val currentEstimation = _geminiFareEstimation.value
        if (currentEstimation != null) {
            _activeDistance.value = currentEstimation.distance
            _activePrice.value = currentEstimation.fare
        } else {
            val randDist = (2..14).random().toDouble()
            _activeDistance.value = randDist

            val pricePerKm = when (type) {
                "bike" -> when (subtype) {
                    "standard" -> 15.0
                    "premium" -> 25.0
                    "electric" -> 10.0
                    else -> 15.0
                }
                "auto" -> when (subtype) {
                    "standard" -> 25.0
                    "share" -> 15.0
                    else -> 25.0
                }
                else -> when (subtype) {
                    "economy" -> 40.0
                    "comfort" -> 55.0
                    "premium" -> 80.0
                    "xl" -> 100.0
                    else -> 40.0
                }
            }

            val minFare = when (type) {
                "bike" -> when (subtype) {
                    "standard" -> 30.0
                    "premium" -> 50.0
                    "electric" -> 20.0
                    else -> 30.0
                }
                "auto" -> when (subtype) {
                    "standard" -> 50.0
                    "share" -> 30.0
                    else -> 50.0
                }
                else -> when (subtype) {
                    "economy" -> 100.0
                    "comfort" -> 150.0
                    "premium" -> 250.0
                    "xl" -> 350.0
                    else -> 100.0
                }
            }

            _activePrice.value = maxOf(minFare, randDist * pricePerKm)
        }
        _activeDriver.value = drivers.random()
        _activeOtp.value = (1000..9999).random().toString()
    }

    // Confirm booking to initiate ride simulation and trace
    fun confirmRideBooking(type: String, subtype: String) {
        val curTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val curDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val ride = Ride(
            fromLocation = _fromLocation.value.ifEmpty { "Kathmandu, Nepal" },
            toLocation = _toLocation.value,
            type = type,
            subtype = subtype,
            price = _activePrice.value,
            dateStr = "Today",
            timeStr = curTime,
            status = "active",
            driverName = _activeDriver.value?.name ?: "Sagar Thapa",
            rating = 0
        )

        _activeRideTracking.value = ride
        _activeTrackingStep.value = 0 // Confirmed

        viewModelScope.launch {
            // Simulated steps
            delay(3000)
            _activeTrackingStep.value = 1 // Arriving
            delay(3000)
            _activeTrackingStep.value = 2 // Started
            delay(3000)
            _activeTrackingStep.value = 3 // Completed

            // Deduct from wallet balance or log as transaction
            _walletBalance.value = maxOf(0.0, _walletBalance.value - _activePrice.value)

            _showRatingDialog.value = true
        }
    }

    // Active Ride Completed and rated
    fun submitRideRating(rating: Int) {
        _currentRatingSelected.value = rating
        val ride = _activeRideTracking.value ?: return

        viewModelScope.launch {
            // Save to database
            val updatedRide = ride.copy(
                status = "completed",
                rating = rating
            )
            repository.insertRide(updatedRide)

            // Save transaction
            val curTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val curDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val commission = _activePrice.value * 0.05

            val walletTx = WalletTransaction(
                type = "earning",
                desc = "${ride.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} - ${ride.fromLocation} to ${ride.toLocation}",
                amount = _activePrice.value,
                timeStr = curTime,
                dateStr = curDate,
                status = "completed",
                commission = commission
            )
            repository.insertTransaction(walletTx)

            // Also add commission credit transaction
            val commissionTx = WalletTransaction(
                type = "commission",
                desc = "Commission Credit (5%)",
                amount = commission,
                timeStr = curTime,
                dateStr = curDate,
                status = "completed"
            )
            repository.insertTransaction(commissionTx)

            // Clear state
            _activeRideTracking.value = null
            _showRatingDialog.value = false
            _toLocation.value = ""
        }
    }

    fun dismissRatingDialog() {
        _showRatingDialog.value = false
        _activeRideTracking.value = null
        _toLocation.value = ""
    }

    // Dynamic Wallet transactions
    fun addWalletMoney(amount: Double) {
        _walletBalance.value += amount
        viewModelScope.launch {
            val curTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val curDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val tx = WalletTransaction(
                type = "load",
                desc = "Add Cash (eSewa/Khalti)",
                amount = amount,
                timeStr = curTime,
                dateStr = curDate,
                status = "completed"
            )
            repository.insertTransaction(tx)
        }
    }

    fun withdrawWalletMoney(amount: Double) {
        if (_walletBalance.value >= amount) {
            _walletBalance.value -= amount
            viewModelScope.launch {
                val curTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                val curDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                val tx = WalletTransaction(
                    type = "withdraw",
                    desc = "Cash Withdrawal",
                    amount = amount,
                    timeStr = curTime,
                    dateStr = curDate,
                    status = "completed"
                )
                repository.insertTransaction(tx)
            }
        }
    }

    // AI Chat Assistant
    fun toggleAIChat() {
        _aiChatOpen.value = !_aiChatOpen.value
        if (_aiChatOpen.value && _chatMessages.value.isEmpty()) {
            _chatMessages.value = listOf(
                ChatMessage(text = "🐬 Namaste! I'm Dolphin AI. How can I help you ride today?", isUser = false)
            )
        }
    }

    fun closeAIChat() {
        _aiChatOpen.value = false
    }

    fun sendChatMessage(msg: String) {
        if (msg.trim().isEmpty()) return

        val userMessage = ChatMessage(text = msg, isUser = true)
        _chatMessages.value = _chatMessages.value + userMessage
        _isAiGenerating.value = true

        viewModelScope.launch {
            // Gather history for context
            val history = _chatMessages.value.takeLast(10).map {
                Pair(if (it.isUser) "user" else "model", it.text)
            }

            val replyText = GeminiApiClient.getChatResponse(msg, history)
            _isAiGenerating.value = false
            _chatMessages.value = _chatMessages.value + ChatMessage(text = replyText, isUser = false)
        }
    }
}
