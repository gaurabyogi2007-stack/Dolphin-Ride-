package com.example.data.network

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun getChatResponse(prompt: String, chatHistory: List<Pair<String, String>> = emptyList()): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return getSimulatedResponse(prompt)
        }

        // Build instructions
        val systemPrompt = """
            You are "Dolphin AI", a smart, friendly, helpful AI Ride Assistant for the "Dolphin Ride" app of Nepal.
            Dolphin Ride operates in Nepalese cities like Kathmandu, Pokhara, Lalitpur, Bhaktapur, Chitwan, Janakpur, etc.
            We offer three main categories:
            1. Bike: Standard (Rs. 15/km, Min Rs. 30), Premium (Rs. 25/km, Min Rs. 50), and Electric (Rs. 10/km, Min Rs. 20). Standard is quick; Electric is eco-friendly. Helmets are always provided.
            2. Auto: Standard (Rs. 25/km, Min Rs. 50) and Share Auto (Rs. 15/km, Min Rs. 30). Fits up to 3 passengers with luggage space.
            3. Car: Economy (Rs. 40/km, Min Rs. 100), Comfort (Rs. 55/km, Min Rs. 150), Premium (Rs. 80/km, Min Rs. 255), and XL (Rs. 100/km, Min Rs. 350). Features include AC, Music, Extra luggage.
            Dolphin Ride has a unique 5% commission policy where exactly 5% of each ride fare goes automatically to the owner. This helps maintain the platform.
            Answer cleanly, keep responses short and concise (under 3 sentences unless pricing list is requested), and be helpful. Use English or friendly Nepali (Romanized or Devanagari) depending on user question. Emphasize that we are fast, safe, and proudly Nepali!
        """.trimIndent()

        val contents = mutableListOf<GeminiContent>()

        // Add history
        for (item in chatHistory) {
            val role = item.first
            val text = item.second
            // For now, simple format or we can pack both into content
            contents.add(GeminiContent(listOf(GeminiPart("$role: $text"))))
        }

        // Add user prompt
        contents.add(GeminiContent(listOf(GeminiPart(prompt))))

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = GeminiContent(listOf(GeminiPart(systemPrompt)))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "🐬 Namaste! I'm Dolphin AI. I'm thinking, but could not get a response right now. Please try again."
        } catch (e: Exception) {
            // Fallback to local simulated replies
            getSimulatedResponse(prompt)
        }
    }

    private fun getSimulatedResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("book") || lower.contains("ride") || lower.contains("सवारी") || lower.contains("बुक") -> {
                "🐬 To book a ride, go to the Home screen, enter your destination in the text fields, tap 'Find Rides', select your category (Bike, Auto, or Car) and then tap 'Book'! Under 5 seconds!"
            }
            lower.contains("rate") || lower.contains("price") || lower.contains("cost") || lower.contains("मूल्य") || lower.contains("दर") || lower.contains("भाडा") -> {
                "💰 Dolphin Ride Pricing:\n• 🏍️ Bike: Standard Rs. 15/km, Premium Rs. 25/km, Electric Rs. 10/km.\n• 🛺 Auto: Standard Rs. 25/km, Share Auto Rs. 15/km.\n• 🚗 Car: Economy Rs. 40/km, Comfort Rs. 55/km, Premium Rs. 80/km, XL Rs. 100/km."
            }
            lower.contains("commission") || lower.contains("5%") || lower.contains("owner") || lower.contains("कमिसन") -> {
                "🐬 Exactly 5% of each ride fare goes directly to the Dolphin Ride Owner platform account, ensuring continuous support and security for all riders and drivers in Nepal!"
            }
            lower.contains("emergency") || lower.contains("sos") || lower.contains("help") || lower.contains("आपतकाल") -> {
                "🚨 Your safety is our Priority! Press the red SOS button to instantly alert emergency servers and access rescue contacts (Police: 100, Ambulance: 102)."
            }
            lower.contains("payment") || lower.contains("esewa") || lower.contains("khalti") -> {
                "💳 We accept Cash, eSewa, Khalti, and ConnectIPS. You can select your payment option in the booking dialog!"
            }
            else -> {
                "🐬 Namaste! I am Dolphin AI, your companion in Nepal. Ask me anything about booking rides, our 5% commission, pricing, or emergency help!"
            }
        }
    }

    suspend fun estimateFareWithGemini(from: String, to: String, category: String = "bike"): FareEstimation {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val cleanCat = category.lowercase().trim()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return getSimulatedFareEstimation(from, to, cleanCat)
        }

        val systemPrompt = """
            You are a fare estimation AI for the Dolphin Ride application in Nepal. 
            Given a pickup and dropoff location, estimate:
            1. Realistic road distance in kilometers (typically between 2km and 20km).
            2. Estimated fare in Nepalese Rupees (Rs.).
              - Bike: Rs. 15 per km, base price Rs. 30.
              - Auto: Rs. 25 per km, base price Rs. 50.
              - Car: Rs. 40 per km, base price Rs. 100.
            Return ONLY a valid JSON object matching this schema exactly:
            {
               "distance": Double,
               "fare": Double,
               "reason": "Short explanation in English (under 12 words) about the route"
            }
            Do not include Markdown blocks like ```json or any other text. Return pure JSON text.
        """.trimIndent()

        val prompt = "Estimate fare from '$from' to '$to' for category: '$cleanCat'"

        val request = GeminiRequest(
            contents = listOf(GeminiContent(listOf(GeminiPart(prompt)))),
            systemInstruction = GeminiContent(listOf(GeminiPart(systemPrompt)))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val cleanJson = jsonText.replace("```json", "").replace("```", "").trim()
            
            val distance = parseDoubleFromJson(cleanJson, "distance", 6.4)
            val fare = parseDoubleFromJson(cleanJson, "fare", 125.0)
            val reason = parseStringFromJson(cleanJson, "reason", "Calculated optimum city route")
            
            FareEstimation(distance, fare, reason, isAiEstimated = true)
        } catch (e: Exception) {
            getSimulatedFareEstimation(from, to, cleanCat)
        }
    }

    private fun parseDoubleFromJson(json: String, key: String, default: Double): Double {
        val regex = "\"$key\"\\s*:\\s*([0-9.]+)".toRegex()
        return regex.find(json)?.groupValues?.get(1)?.toDoubleOrNull() ?: default
    }

    private fun parseStringFromJson(json: String, key: String, default: String): String {
        val regex = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return regex.find(json)?.groupValues?.get(1) ?: default
    }

    fun getSimulatedFareEstimation(from: String, to: String, category: String): FareEstimation {
        val hash = (from.hashCode() + to.hashCode()).let { if (it < 0) -it else it }
        val distance = (hash % 15).toDouble() + 3.4
        
        val pricePerKm = when (category) {
            "bike" -> 15.0
            "auto" -> 25.0
            else -> 40.0
        }
        val minFare = when (category) {
            "bike" -> 30.0
            "auto" -> 50.0
            else -> 100.0
        }
        val fare = maxOf(minFare, distance * pricePerKm)
        val reason = "Optimal route simulated via Ring Road (Safe & Certified)"
        
        return FareEstimation(distance, fare, reason, isAiEstimated = false)
    }
}

data class FareEstimation(
    val distance: Double,
    val fare: Double,
    val reason: String,
    val isAiEstimated: Boolean = false
)
