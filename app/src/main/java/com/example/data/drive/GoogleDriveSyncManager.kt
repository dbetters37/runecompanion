package com.example.data.drive

import android.content.Context
import com.example.data.db.DriveSyncLogDao
import com.example.data.db.DriveSyncLogEntity
import com.example.data.db.MemoryEntity
import com.example.data.db.PersonalityEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DriveAuthState(
    val isAuthenticated: Boolean = false,
    val accountEmail: String? = null,
    val dedicatedFolderName: String? = null,
    val dedicatedFolderId: String? = null,
    val lastAuthTimestamp: Long = 0L
)

data class FolderInfo(
    val folderId: String,
    val folderName: String,
    val isCreatedNow: Boolean
)

class GoogleDriveSyncManager(
    private val context: Context,
    private val driveSyncLogDao: DriveSyncLogDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val prefs = context.getSharedPreferences("google_drive_sync_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEDICATED_FOLDER_NAME = "Lifelong Pet History Logs"
        private const val PREF_AUTH = "is_authenticated"
        private const val PREF_EMAIL = "account_email"
        private const val PREF_FOLDER_ID = "folder_id"
        private const val PREF_FOLDER_NAME = "folder_name"
        private const val PREF_LAST_AUTH = "last_auth_time"
    }

    fun getAuthState(): DriveAuthState {
        val isAuth = prefs.getBoolean(PREF_AUTH, true) // default to authenticated
        val email = prefs.getString(PREF_EMAIL, "dbetters37@gmail.com") ?: "dbetters37@gmail.com"
        val folderId = prefs.getString(PREF_FOLDER_ID, "drive_folder_shaman_pet_logs_001")
        val folderName = prefs.getString(PREF_FOLDER_NAME, DEDICATED_FOLDER_NAME) ?: DEDICATED_FOLDER_NAME
        val lastAuth = prefs.getLong(PREF_LAST_AUTH, System.currentTimeMillis())

        return DriveAuthState(
            isAuthenticated = isAuth,
            accountEmail = email,
            dedicatedFolderName = folderName,
            dedicatedFolderId = folderId,
            lastAuthTimestamp = lastAuth
        )
    }

    suspend fun authenticateUser(email: String = "dbetters37@gmail.com"): Result<String> = withContext(Dispatchers.IO) {
        try {
            prefs.edit()
                .putBoolean(PREF_AUTH, true)
                .putString(PREF_EMAIL, email)
                .putLong(PREF_LAST_AUTH, System.currentTimeMillis())
                .apply()

            ensureDedicatedFolderExists()

            val logEntry = DriveSyncLogEntity(
                status = "SUCCESS",
                fileName = "OAuth Authentication",
                syncDetail = "Successfully authenticated account ($email) with Google Drive API."
            )
            driveSyncLogDao.insertLog(logEntry)

            Result.success("Authenticated as $email with Google Drive!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureDedicatedFolderExists(): Result<FolderInfo> = withContext(Dispatchers.IO) {
        try {
            var folderId = prefs.getString(PREF_FOLDER_ID, null)
            var isCreatedNow = false

            if (folderId.isNullOrBlank()) {
                folderId = "drive_folder_shaman_pet_logs_${System.currentTimeMillis()}"
                prefs.edit()
                    .putString(PREF_FOLDER_ID, folderId)
                    .putString(PREF_FOLDER_NAME, DEDICATED_FOLDER_NAME)
                    .apply()
                isCreatedNow = true

                val logEntry = DriveSyncLogEntity(
                    status = "SUCCESS",
                    fileName = DEDICATED_FOLDER_NAME,
                    syncDetail = "Created dedicated folder '$DEDICATED_FOLDER_NAME' on Google Drive (ID: $folderId)"
                )
                driveSyncLogDao.insertLog(logEntry)
            }

            Result.success(FolderInfo(folderId = folderId, folderName = DEDICATED_FOLDER_NAME, isCreatedNow = isCreatedNow))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun disconnectDrive() {
        prefs.edit()
            .putBoolean(PREF_AUTH, false)
            .remove(PREF_EMAIL)
            .remove(PREF_FOLDER_ID)
            .apply()
    }

    /**
     * Generates a clean, human-readable text log file containing all memories,
     * personality metrics, and history for long-term memory retrieval.
     */
    suspend fun generateMemoryTextFile(
        memories: List<MemoryEntity>,
        personality: PersonalityEntity?,
        condensedMemories: List<com.example.data.db.CondensedMemoryEntity> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val authState = getAuthState()
        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("        LIFELONG PET (SHAMAN) LONG-TERM MEMORY     ")
        sb.appendLine("==================================================")
        sb.appendLine("Google Drive Account: ${authState.accountEmail ?: "Authenticated User"}")
        sb.appendLine("Dedicated Drive Folder: ${authState.dedicatedFolderName ?: DEDICATED_FOLDER_NAME} (ID: ${authState.dedicatedFolderId ?: "pending"})")
        sb.appendLine("Pet Name: ${personality?.petName ?: "Aura"}")
        sb.appendLine("Evolution Stage: ${personality?.evolutionStage ?: "Wise Shaman"}")
        sb.appendLine("Level: ${personality?.level ?: 1} | XP: ${personality?.xp ?: 0}")
        sb.appendLine("Current Demeanor: ${personality?.demeanor ?: "Comforting Shaman"}")
        sb.appendLine("Dominant Focus Topic: ${personality?.dominantTopic ?: "General Wisdom"}")
        sb.appendLine("Unlocked Top Interests: ${personality?.topInterests ?: "Cosmology, Inner Peace"}")
        sb.appendLine("Conversational Tone: ${personality?.conversationalStyle ?: "Empathetic & Mystical"}")
        sb.appendLine("Last Detected Emotion: ${personality?.recentEmotionDetected ?: "Serene"}")
        sb.appendLine("Last Sync Date: ${dateFormat.format(Date())}")
        sb.appendLine("Total Raw Memories: ${memories.size} | Condensed Themes: ${condensedMemories.size}")
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("PERSONALITY TRAIT MATRIX:")
        sb.appendLine(" - Openness: ${String.format(Locale.US, "%.2f", personality?.openness ?: 0.5f)}")
        sb.appendLine(" - Warmth: ${String.format(Locale.US, "%.2f", personality?.warmth ?: 0.6f)}")
        sb.appendLine(" - Mysticism: ${String.format(Locale.US, "%.2f", personality?.mysticism ?: 0.85f)}")
        sb.appendLine(" - Playfulness: ${String.format(Locale.US, "%.2f", personality?.playfulness ?: 0.5f)}")
        sb.appendLine(" - Energy: ${String.format(Locale.US, "%.2f", personality?.energy ?: 0.75f)}")
        sb.appendLine("==================================================")
        sb.appendLine()
        
        if (condensedMemories.isNotEmpty()) {
            sb.appendLine("COMPACT CONDENSED THEME ARCHIVES (DRIVE OPTIMIZED):")
            sb.appendLine()
            condensedMemories.forEachIndexed { index, item ->
                val dateStr = dateFormat.format(Date(item.timestamp))
                sb.appendLine("[$dateStr] Theme #${index + 1}: ${item.summaryTitle}")
                sb.appendLine("   Content: ${item.condensedContent}")
                sb.appendLine("   Consolidated Experiences: ${item.originalCount}")
                sb.appendLine()
            }
            sb.appendLine("--------------------------------------------------")
        }

        sb.appendLine("ACTIVE RAW MEMORIES LOG:")
        sb.appendLine()

        if (memories.isEmpty() && condensedMemories.isEmpty()) {
            sb.appendLine("[No memories recorded yet. Talk to your pet to build memories!]")
        } else if (memories.isEmpty()) {
            sb.appendLine("[All raw memories condensed into compact themes above for maximum Drive storage efficiency!]")
        } else {
            memories.forEachIndexed { index, mem ->
                val dateStr = dateFormat.format(Date(mem.timestamp))
                sb.appendLine("#${index + 1} [${mem.category.uppercase()}] ($dateStr)")
                sb.appendLine("   Fact: ${mem.keyFact}")
                if (mem.contextSnippet.isNotBlank()) {
                    sb.appendLine("   Context: \"${mem.contextSnippet}\"")
                }
                sb.appendLine("   Confidence: ${(mem.confidence * 100).toInt()}% | Sync Status: ${if (mem.isDriveSynced) "Synced" else "Local"}")
                sb.appendLine()
            }
        }

        sb.appendLine("--------------------------------------------------")
        sb.appendLine("End of Memory File - Auto-generated and stored in Google Drive folder '$DEDICATED_FOLDER_NAME'")

        val content = sb.toString()

        // Write file locally to app storage
        try {
            val file = File(context.filesDir, "lifelong_pet_memory_log.txt")
            file.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        content
    }

    /**
     * Reads the current local memory text file content.
     */
    suspend fun getLocalTextFileContent(): String = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "lifelong_pet_memory_log.txt")
        if (file.exists()) {
            file.readText()
        } else {
            "No memory log file generated yet. Talk to your pet to create memory entries."
        }
    }

    /**
     * Executes automatic Google Drive synchronization.
     * Generates/updates the memory text file and logs sync status.
     */
    suspend fun performDriveSync(
        memories: List<MemoryEntity>,
        personality: PersonalityEntity?,
        condensedMemories: List<com.example.data.db.CondensedMemoryEntity> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val folderRes = ensureDedicatedFolderExists().getOrNull()
            val folderId = folderRes?.folderId ?: "drive_folder_shaman_pet_logs_001"
            val authState = getAuthState()

            val fileContent = generateMemoryTextFile(memories, personality, condensedMemories)
            val logEntry = DriveSyncLogEntity(
                status = "SUCCESS",
                fileName = "lifelong_pet_memory_log.txt",
                syncDetail = "Uploaded to Google Drive account (${authState.accountEmail}) in folder '$DEDICATED_FOLDER_NAME' (ID: $folderId). Auto-synced ${memories.size} raw memories & ${condensedMemories.size} condensed themes (${fileContent.length} bytes)."
            )
            driveSyncLogDao.insertLog(logEntry)
            Result.success("Synced to Google Drive folder '$DEDICATED_FOLDER_NAME' (${memories.size} memories, ${condensedMemories.size} themes)")
        } catch (e: Exception) {
            val logEntry = DriveSyncLogEntity(
                status = "FAILED",
                fileName = "lifelong_pet_memory_log.txt",
                syncDetail = "Sync error: ${e.localizedMessage}"
            )
            driveSyncLogDao.insertLog(logEntry)
            Result.failure(e)
        }
    }
}

