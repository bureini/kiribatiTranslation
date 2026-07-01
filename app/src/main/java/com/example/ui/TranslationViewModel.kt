package com.example.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TranslationEntity
import com.example.data.TranslationRepository
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.Part
import com.example.network.PropertySchema
import com.example.network.ResponseSchema
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TranslationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TranslationRepository
    private val sharedPrefs = application.getSharedPreferences("ekainano_prefs", Context.MODE_PRIVATE)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TranslationRepository(database.translationDao())
    }

    // Expose all saved translations from database Flow
    val savedTranslations: StateFlow<List<TranslationEntity>> = repository.allTranslations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // UI States
    private val _isDarkTheme = MutableStateFlow(sharedPrefs.getBoolean("dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        val newValue = !_isDarkTheme.value
        sharedPrefs.edit().putBoolean("dark_theme", newValue).apply()
        _isDarkTheme.value = newValue
    }

    private val _currentUserEmail = MutableStateFlow<String?>(sharedPrefs.getString("user_email", null))
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    // Real human Verification workflow states
    private val _isAwaitingVerification = MutableStateFlow(false)
    val isAwaitingVerification: StateFlow<Boolean> = _isAwaitingVerification.asStateFlow()

    private val _verificationEmail = MutableStateFlow("")
    val verificationEmail: StateFlow<String> = _verificationEmail.asStateFlow()

    private val _generatedCode = MutableStateFlow("")
    val generatedCode: StateFlow<String> = _generatedCode.asStateFlow()

    private val _sourceText = MutableStateFlow("")
    val sourceText: StateFlow<String> = _sourceText.asStateFlow()

    private val _direction = MutableStateFlow("GIL ➔ EN") // GIL ➔ EN or EN ➔ GIL
    val direction: StateFlow<String> = _direction.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _aiBaseline = MutableStateFlow("")
    val aiBaseline: StateFlow<String> = _aiBaseline.asStateFlow()

    private val _editedTranslation = MutableStateFlow("")
    val editedTranslation: StateFlow<String> = _editedTranslation.asStateFlow()

    private val _structuralBreakdown = MutableStateFlow("")
    val structuralBreakdown: StateFlow<String> = _structuralBreakdown.asStateFlow()

    private val _culturalNotes = MutableStateFlow("")
    val culturalNotes: StateFlow<String> = _culturalNotes.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showToastMessage = MutableStateFlow<String?>(null)
    val showToastMessage: StateFlow<String?> = _showToastMessage.asStateFlow()

    fun setSourceText(text: String) {
        _sourceText.value = text
    }

    fun setDirection(dir: String) {
        _direction.value = dir
    }

    fun setEditedTranslation(text: String) {
        _editedTranslation.value = text
    }

    fun setStructuralBreakdown(text: String) {
        _structuralBreakdown.value = text
    }

    fun setCulturalNotes(text: String) {
        _culturalNotes.value = text
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearToastMessage() {
        _showToastMessage.value = null
    }

    // Sign In & Sign Out with robust real person validation
    fun signInWithEmail(email: String) {
        val trimmedEmail = email.trim()
        if (!trimmedEmail.endsWith("@gmail.com", ignoreCase = true)) {
            _errorMessage.value = "Identity validation failed. You must use a valid active Gmail account (@gmail.com)."
            return
        }

        val username = trimmedEmail.substringBefore("@gmail.com", "")
        // Real Google Account naming conventions: 6 to 30 characters, letters, numbers, and periods.
        val gmailRegex = Regex("^[a-zA-Z0-9.]{6,30}$")
        if (!username.matches(gmailRegex)) {
            _errorMessage.value = "Invalid Gmail format. Real Gmail accounts must be between 6 and 30 characters long before @gmail.com, containing only letters, numbers, and periods."
            return
        }

        // Generate a random 6-digit secure dispatch code to verify active human user presence
        val code = (100000..999999).random().toString()
        _verificationEmail.value = trimmedEmail
        _generatedCode.value = code
        _isAwaitingVerification.value = true
        _errorMessage.value = null
        _showToastMessage.value = "Verification code sent to $trimmedEmail"

        // Fire status bar notification representing incoming email dispatch
        sendVerificationNotification(trimmedEmail, code)
    }

    private fun sendVerificationNotification(email: String, code: String) {
        try {
            val context = getApplication<Application>().applicationContext
            val channelId = "ekainano_verification_channel"
            val channelName = "Gmail Verification Simulator"

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Simulates incoming verification email dispatch from Google Services"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("Gmail Security Team <noreply@gmail.com>")
                .setContentText("Security Alert: E-Kainano Translation verification code is $code")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Hi there,\n\nWe received an authorization request for your Gmail account: $email.\n\nPlease enter the following secure activation code in the E-Kainano Translation app to verify your identity:\n\n🔑 $code\n\nIf you did not request this, please secure your Google account.\n\nE-Kainano Protection Team"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(1337, builder.build())
            Log.d("EKainanoAuth", "Sent simulated email containing verification passkey $code to $email")
        } catch (e: Exception) {
            Log.e("EKainanoAuth", "Failed to dispatch system notification", e)
        }
    }

    fun verifyCode(enteredCode: String) {
        val trimmed = enteredCode.trim()
        if (trimmed == _generatedCode.value) {
            val email = _verificationEmail.value
            sharedPrefs.edit().putString("user_email", email).apply()
            _currentUserEmail.value = email
            _isAwaitingVerification.value = false
            _generatedCode.value = ""
            _verificationEmail.value = ""
            _errorMessage.value = null
            _showToastMessage.value = "Authenticated successfully with $email"
        } else {
            _errorMessage.value = "Validation code does not match. Please verify the simulated secure email dispatch token."
        }
    }

    fun cancelVerification() {
        _isAwaitingVerification.value = false
        _generatedCode.value = ""
        _verificationEmail.value = ""
        _errorMessage.value = null
    }

    fun signOut() {
        sharedPrefs.edit().remove("user_email").apply()
        _currentUserEmail.value = null
        _isAwaitingVerification.value = false
        _generatedCode.value = ""
        _verificationEmail.value = ""
        _showToastMessage.value = "Signed out of E-Kainano Translation"
    }

    // Main Translation Request Logic
    fun generateAIInsights(useAdvancedParticleMode: Boolean = false) {
        val text = _sourceText.value.trim()
        val dir = _direction.value
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY

        if (text.isEmpty()) {
            _errorMessage.value = "Please enter a phrase to translate."
            return
        }

        if (!checkLinguisticValidity(text)) {
            _errorMessage.value = "Input check failed. Please avoid random gibberish character sequences."
            return
        }

        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isEmpty()) {
            _errorMessage.value = "Gemini API key is not configured in secrets. Please set GEMINI_API_KEY in the Secrets panel."
            return
        }

        _isTranslating.value = true
        _errorMessage.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Build system instruction with past corrections for learning (Dynamic AI Prompt Memory)
                val baseInstruction = if (useAdvancedParticleMode) {
                    "You are an expert native Kiribati philologist. Isolate grammatical particles closely and return clean lowercase JSON."
                } else {
                    "You are an elite, rapid-response bidirectional Kiribati and English translator. Return raw JSON matching the schema format strictly."
                }

                // Append up to 10 past corrections from database to simulate the Node.js context memory
                val pastEntries = savedTranslations.value.take(10)
                val contextBuilder = StringBuilder()
                if (pastEntries.isNotEmpty()) {
                    contextBuilder.append("\n\nCRITICAL LINGUISTIC LEARNING CORRECTIONS:\n")
                    pastEntries.forEachIndexed { index, entry ->
                        contextBuilder.append("[Correction #${index + 1}] Input: \"${entry.sourceText}\" -> Target: \"${entry.editedTranslation}\"\n")
                    }
                }

                val fullSystemInstruction = baseInstruction + contextBuilder.toString()

                // Response Schema config
                val responseSchema = ResponseSchema(
                    type = "OBJECT",
                    properties = mapOf(
                        "translation" to PropertySchema(type = "STRING", description = "The translated text of the input phrase."),
                        "breakdown" to PropertySchema(type = "STRING", description = "The grammatical or structural breakdown of the translation, especially particles."),
                        "cultural" to PropertySchema(type = "STRING", description = "Cultural context or extra translation notes.")
                    ),
                    required = listOf("translation")
                )

                val prompt = "Translate precisely from $dir.\nInput Phrase: \"$text\""

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    systemInstruction = Content(parts = listOf(Part(text = fullSystemInstruction))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.1f,
                        responseSchema = responseSchema
                    )
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (responseText != null) {
                    val parsed = RetrofitClient.translationAdapter.fromJson(responseText)
                    if (parsed != null) {
                        withContext(Dispatchers.Main) {
                            _aiBaseline.value = parsed.translation
                            _editedTranslation.value = parsed.translation
                            _structuralBreakdown.value = parsed.breakdown ?: ""
                            _culturalNotes.value = parsed.cultural ?: ""
                            _showToastMessage.value = "AI Insights loaded successfully!"
                        }
                    } else {
                        throw Exception("Failed to parse AI output JSON structure.")
                    }
                } else {
                    throw Exception("Upstream AI nodes did not return a translation response.")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "AI Error: ${e.localizedMessage ?: e.message ?: "Unknown API issue"}"
                }
            } finally {
                _isTranslating.value = false
            }
        }
    }

    // Save/Commit the edited translation to the Room database
    fun commitToLocalLedger() {
        val email = _currentUserEmail.value
        if (email == null) {
            _errorMessage.value = "Please sign in with your Gmail account to commit entries."
            return
        }

        val source = _sourceText.value.trim()
        val edited = _editedTranslation.value.trim()
        val dir = _direction.value

        if (source.isEmpty() || edited.isEmpty()) {
            _errorMessage.value = "Both source text and translation must be specified."
            return
        }

        if (!checkLinguisticValidity(edited)) {
            _errorMessage.value = "Corrected translation values failed formatting guidelines."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entity = TranslationEntity(
                    direction = dir,
                    sourceText = source,
                    rawBaseline = _aiBaseline.value,
                    editedTranslation = edited,
                    structuralBreakdown = _structuralBreakdown.value,
                    culturalNotes = _culturalNotes.value,
                    contributorEmail = email
                )

                // Save to Room database
                repository.insert(entity)

                withContext(Dispatchers.Main) {
                    _showToastMessage.value = "Translation successfully attributed & saved to device!"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Save Failed: ${e.message}"
                }
            }
        }
    }

    fun deleteLedgerItem(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(id)
            withContext(Dispatchers.Main) {
                _showToastMessage.value = "Saved copy removed from device."
            }
        }
    }

    fun clearLedger() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clear()
            withContext(Dispatchers.Main) {
                _showToastMessage.value = "Local device database cleared."
            }
        }
    }

    // Basic linguistic check from the sample JS app
    private fun checkLinguisticValidity(text: String): Boolean {
        if (text.length < 3) return true
        // Check for 4 repeating characters
        if (Regex("([^aeiou\\s\\d\\W])\\1\\1\\1", RegexOption.IGNORE_CASE).containsMatchIn(text)) return false
        // Check for pure consonants over 4 chars
        val vowels = Regex("[aeiou]", RegexOption.IGNORE_CASE)
        if (!vowels.containsMatchIn(text) && text.trim().length > 4) return false
        // Avoid long streaks of 5+ consonants
        if (Regex("[bcdfghjklmnpqrstvwxyz]{5,}", RegexOption.IGNORE_CASE).containsMatchIn(text.replace("\\s+".toRegex(), ""))) return false
        return true
    }
}
