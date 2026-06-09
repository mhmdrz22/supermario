package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ResistanceRepository
import com.example.data.ResistanceTask
import com.example.data.TransmissionLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class ResistanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ResistanceRepository
    val uiTasks: StateFlow<List<ResistanceTask>>
    val uiTransmissions: StateFlow<List<TransmissionLog>>

    // Dynamic environmental network states
    private val _syndicateInterference = MutableStateFlow(24) // Dynamic load index %
    val syndicateInterference: StateFlow<Int> = _syndicateInterference.asStateFlow()

    private val _networkLatency = MutableStateFlow(42) // ms
    val networkLatency: StateFlow<Int> = _networkLatency.asStateFlow()

    private val _activeDecryptions = MutableStateFlow<Set<Int>>(emptySet())
    val activeDecryptions: StateFlow<Set<Int>> = _activeDecryptions.asStateFlow()

    // Centralized system telemetry logs
    private val _systemLogs = MutableStateFlow<List<String>>(emptyList())
    val systemLogs: StateFlow<List<String>> = _systemLogs.asStateFlow()

    // Random terminal glitching system
    private val _glitchActive = MutableStateFlow(false)
    val glitchActive: StateFlow<Boolean> = _glitchActive.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).resistanceDao()
        repository = ResistanceRepository(dao)

        uiTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        uiTransmissions = repository.allTransmissions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial system logs
        _systemLogs.value = listOf(
            "[INF] Systems initialized securely.",
            "[SYS] Local database synced with Room cluster.",
            "[NET] Bypassing Syndicate firewall handshake...",
            "[SYS] Thermal masking protocol active."
        )

        // Seed data if empty
        viewModelScope.launch {
            if (repository.allTasks.first().isEmpty()) {
                seedInitialTasks()
            }
            if (repository.allTransmissions.first().isEmpty()) {
                seedInitialTransmissions()
            }
        }

        // Simulate realistic terminal fluctuation
        viewModelScope.launch {
            while (true) {
                delay(4000)
                _syndicateInterference.value = Random.nextInt(15, 38)
                _networkLatency.value = Random.nextInt(28, 65)
            }
        }

        // Loop to append telemetry log periodically (moved from UI to comply with clean architecture)
        viewModelScope.launch {
            val pool = listOf(
                "[SYS] Rotating security handshakes ports... [SUCCESS]",
                "[ALRT] Syndicate load scans blocked securely.",
                "[NET] Siphoning decrypted packet sequences.",
                "[SYS] Memory fragmentation optimized to 0.02%.",
                "[INF] Rotating encryption keys on node cluster...",
                "[SYS] Inbound socket handshake validated.",
                "[ALRT] Heavy load simulation checked under limits.",
                "[NET] Handshake routing latency updated."
            )
            while (true) {
                delay(3500)
                val randomLog = pool.random()
                val logText = if (randomLog.contains("latency")) {
                    "[NET] Handshake routing latency: ${_networkLatency.value}ms stable."
                } else {
                    randomLog
                }
                val current = _systemLogs.value.toMutableList()
                current.add(logText)
                if (current.size > 8) {
                    current.removeAt(0)
                }
                _systemLogs.value = current
            }
        }

        // Occasional screen interference glitches (every 22-38s for 200ms)
        viewModelScope.launch {
            while (true) {
                delay(Random.nextLong(20000, 38000))
                _glitchActive.value = true
                delay(200)
                _glitchActive.value = false
            }
        }
    }

    private suspend fun seedInitialTasks() {
        val initial = listOf(
            ResistanceTask(
                title = "Infiltrate Sector 4 Data Vault",
                description = "Bypass the biometric scanner of the Syndicate central server to siphon the encryption database.",
                threatLevel = "CRITICAL",
                status = "ACTIVE",
                targetSector = "SECTOR_04_CORE",
                decryptionKey = "AES_GCM_256_S4"
            ),
            ResistanceTask(
                title = "Intercept Syndicate Cargo Feed",
                description = "Reroute local logistics hub cameras to map the incoming weapon delivery route.",
                threatLevel = "HIGH",
                status = "PENDING",
                targetSector = "SECTOR_09_DISTRIB",
                decryptionKey = "SHA_512_FEED_B"
            ),
            ResistanceTask(
                title = "Node-07 Thermal Stabilization",
                description = "Deploy software patches to throttle background compilation engines and mask thermal footprints.",
                threatLevel = "LOW",
                status = "COMPLETED",
                targetSector = "UNDERGROUND_SUB",
                decryptionKey = "NONE"
            )
        )
        for (task in initial) {
            repository.insertTask(task)
        }
    }

    private suspend fun seedInitialTransmissions() {
        val initial = listOf(
            TransmissionLog(
                sender = "ORACLE",
                encryptedContent = "U2VuZCBpbmZvcm1hdGlvbiBub3c7IHN5bmRpY2F0ZSBpcyBtb3Zpbmcgb24gc2VjdG9yIDUu",
                decryptedContent = "URGENT: Syndicate strike unit is re-routing towards Sector 5. Disperse local node immediately.",
                isDecrypted = false,
                signalStrength = 92
            ),
            TransmissionLog(
                sender = "NODE_03",
                encryptedContent = "U3lzdGVtcyBzdGFibGUsIG5vIGFjdGl2aXR5IGRldGVjdGVk",
                decryptedContent = "All thermal footprints masked. Sub-grid 3 operational.",
                isDecrypted = true,
                signalStrength = 84
            ),
            TransmissionLog(
                sender = "UNKNOWN_SOURCE",
                encryptedContent = "V2UgaGF2ZSB0aGUgZW5jcnlwdGlvbiBrZXlzLiBBd2FpdCBzaWduYWwu",
                decryptedContent = "COURIER LOCATED: Encryption keys retrieved. Keep visual terminal open for downlink instructions.",
                isDecrypted = false,
                signalStrength = 41
            )
        )
        for (log in initial) {
            repository.insertTransmission(log)
        }
    }

    fun submitTask(title: String, description: String, threatLevel: String, targetSector: String, decryptionKey: String) {
        viewModelScope.launch {
            val task = ResistanceTask(
                title = title,
                description = description,
                threatLevel = threatLevel,
                status = "PENDING",
                targetSector = targetSector,
                decryptionKey = decryptionKey.ifBlank { "TACTICAL_SECURE" }
            )
            repository.insertTask(task)
        }
    }

    fun updateTaskStatus(task: ResistanceTask, newStatus: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = newStatus))
        }
    }

    fun removeTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun transmitLog(sender: String, messageText: String) {
        viewModelScope.launch {
            // Emulate secure encrypt matching
            val senderLabel = sender.uppercase().ifBlank { "NODE_LOCAL" }
            val base64Encoded = android.util.Base64.encodeToString(messageText.toByteArray(), android.util.Base64.NO_WRAP)
            val log = TransmissionLog(
                sender = senderLabel,
                encryptedContent = base64Encoded,
                decryptedContent = messageText,
                isDecrypted = true, // Local messages are composed unencrypted then locked, shown as open to sender
                signalStrength = 100
            )
            repository.insertTransmission(log)

            // Trigger real-time intelligent response from Oracle in background using Gemini
            generateOracleResponse(senderLabel, messageText)
        }
    }

    private fun generateOracleResponse(userSender: String, userMessage: String) {
        viewModelScope.launch {
            // Wait slightly for dramatic effect
            delay(1500)

            val apiKey = com.example.BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                // Return highly thematic default responses if key is not configured
                val fallbackAnswers = listOf(
                    "INTELLIGENCE BRIEFING: Security breach detected in Sector 4. Seal all ports immediately.",
                    "TACTICAL OVERRIDE: Oracle channels are operational. Log downlink received. Stay dark.",
                    "SIGNAL CONFIRMED: Bypassing Syndicate firewalls. Target instructions will follow. Standby, Operator."
                )
                addOracleTransmission(fallbackAnswers.random())
                return@launch
            }

            try {
                val systemPromptText = "تو یک هوش مصنوعی مخفی به نام Oracle در یک پادآرمانشهر (Dystopia) هستی که به نیروهای مقاومت کمک می‌کنی. جواب‌ها را رمزآلود، کوتاه و با لحن ترمینالی بده. Respond in the same language as the user (Persian if user types Persian, English if English). Limit response to 1-2 extremely concise sentences with retro hacking flavor."
                val response = GeminiClient.service.generateContent(
                    apiKey = apiKey,
                    request = GeminiRequest(
                        contents = listOf(
                            GeminiContent(
                                parts = listOf(
                                    GeminiPart(text = "Transmission from Operator [$userSender]: $userMessage")
                                )
                            )
                        ),
                        systemInstruction = GeminiContent(
                            parts = listOf(
                                GeminiPart(text = systemPromptText)
                            )
                        )
                    )
                )

                val answer = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!answer.isNullOrBlank()) {
                    addOracleTransmission(answer.trim())
                } else {
                    addOracleTransmission("WARNING: Intermittent noise detected. Oracle signal weak but alive.")
                }
            } catch (e: Exception) {
                addOracleTransmission("UPLINK INTERRUPTED: Direct signal blocked by Syndicate cluster jamming. Retry transmission.")
            }
        }
    }

    private suspend fun addOracleTransmission(messageText: String) {
        val base64Encoded = android.util.Base64.encodeToString(messageText.toByteArray(), android.util.Base64.NO_WRAP)
        val log = TransmissionLog(
            sender = "ORACLE",
            encryptedContent = base64Encoded,
            decryptedContent = messageText,
            isDecrypted = false, // Cipher needs to be cracked!
            signalStrength = Random.nextInt(78, 97)
        )
        repository.insertTransmission(log)
    }

    fun decryptTransmission(log: TransmissionLog) {
        if (log.isDecrypted || _activeDecryptions.value.contains(log.id)) return

        viewModelScope.launch {
            _activeDecryptions.value = _activeDecryptions.value + log.id
            // Simulate immersive crunching of decryption keys
            delay(1500)
            repository.updateTransmission(log.copy(isDecrypted = true))
            _activeDecryptions.value = _activeDecryptions.value - log.id
        }
    }

    fun purgeTransmissions() {
        viewModelScope.launch {
            repository.clearAllTransmissions()
            seedInitialTransmissions()
        }
    }
}
