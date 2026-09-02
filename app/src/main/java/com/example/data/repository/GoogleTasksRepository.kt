package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.GoogleTaskItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class GoogleTasksRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("google_tasks_prefs", Context.MODE_PRIVATE)
    private val okHttpClient = OkHttpClient()

    var googleOAuthToken: String?
        get() = prefs.getString("google_oauth_token", null)
        set(value) = prefs.edit().putString("google_oauth_token", value).apply()

    init {
        // Initialize sample tasks if empty
        if (!prefs.contains("has_initialized_tasks")) {
            val now = System.currentTimeMillis()
            val dayMs = 86_400_000L
            val defaultTasks = listOf(
                GoogleTaskItem(
                    id = "gt_sample_1",
                    title = "🧹 Deep clean garage & organize tools",
                    notes = "Ancient task left pending for over a week!",
                    status = "needsAction",
                    createdTimestampMs = now - (8 * dayMs)
                ),
                GoogleTaskItem(
                    id = "gt_sample_2",
                    title = "📚 Finish reading Chapter 4 & write summary",
                    notes = "Aged task pending for 4 days",
                    status = "needsAction",
                    createdTimestampMs = now - (4 * dayMs)
                ),
                GoogleTaskItem(
                    id = "gt_sample_3",
                    title = "🦷 Call dentist & schedule teeth cleaning",
                    notes = "Mature task pending for 2 days",
                    status = "needsAction",
                    createdTimestampMs = now - (2 * dayMs)
                ),
                GoogleTaskItem(
                    id = "gt_sample_4",
                    title = "🪴 Water living room indoor plants",
                    notes = "Fresh daily task",
                    status = "needsAction",
                    createdTimestampMs = now - (4 * 3_600_000L)
                )
            )
            saveLocalTasks(defaultTasks)
            prefs.edit().putBoolean("has_initialized_tasks", true).apply()
        }
    }

    fun getLocalTasks(): List<GoogleTaskItem> {
        val jsonStr = prefs.getString("local_google_tasks_json", null) ?: return emptyList()
        val list = mutableListOf<GoogleTaskItem>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    GoogleTaskItem(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        notes = obj.optString("notes", null),
                        status = obj.optString("status", "needsAction"),
                        createdTimestampMs = obj.optLong("createdTimestampMs", System.currentTimeMillis()),
                        completedTimestampMs = if (obj.has("completedTimestampMs")) obj.optLong("completedTimestampMs") else null,
                        due = obj.optString("due", null),
                        listId = obj.optString("listId", "@default")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveLocalTasks(tasks: List<GoogleTaskItem>) {
        val jsonArray = JSONArray()
        for (task in tasks) {
            val obj = JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("notes", task.notes)
                put("status", task.status)
                put("createdTimestampMs", task.createdTimestampMs)
                task.completedTimestampMs?.let { put("completedTimestampMs", it) }
                put("due", task.due)
                put("listId", task.listId)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("local_google_tasks_json", jsonArray.toString()).apply()
    }

    /**
     * Sync tasks with official Google Tasks REST API endpoint if token exists.
     */
    suspend fun syncWithGoogleTasksApi(): Result<List<GoogleTaskItem>> = withContext(Dispatchers.IO) {
        val token = googleOAuthToken
        if (token.isNullOrEmpty()) {
            return@withContext Result.success(getLocalTasks())
        }

        try {
            val request = Request.Builder()
                .url("https://tasks.googleapis.com/tasks/v1/lists/@default/tasks?showCompleted=true&showHidden=true")
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Google Tasks API HTTP ${response.code}: ${response.message}"))
            }

            val bodyStr = response.body?.string() ?: ""
            val jsonObj = JSONObject(bodyStr)
            val itemsArray = jsonObj.optJSONArray("items") ?: JSONArray()
            val fetchedList = mutableListOf<GoogleTaskItem>()

            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val localTasks = getLocalTasks().associateBy { it.id }

            for (i in 0 until itemsArray.length()) {
                val item = itemsArray.getJSONObject(i)
                val id = item.optString("id")
                val title = item.optString("title", "Untitled Task")
                val notes = item.optString("notes", null)
                val status = item.optString("status", "needsAction")
                val createdIso = item.optString("updated") ?: item.optString("created")
                
                val createdMs = try {
                    if (!createdIso.isNullOrEmpty()) isoFormat.parse(createdIso)?.time ?: System.currentTimeMillis()
                    else System.currentTimeMillis()
                } catch (e: Exception) {
                    localTasks[id]?.createdTimestampMs ?: System.currentTimeMillis()
                }

                val completedIso = item.optString("completed")
                val completedMs = try {
                    if (!completedIso.isNullOrEmpty()) isoFormat.parse(completedIso)?.time else null
                } catch (e: Exception) {
                    null
                }

                fetchedList.add(
                    GoogleTaskItem(
                        id = id,
                        title = title,
                        notes = notes,
                        status = status,
                        createdTimestampMs = createdMs,
                        completedTimestampMs = completedMs,
                        due = item.optString("due", null)
                    )
                )
            }

            saveLocalTasks(fetchedList)
            Result.success(fetchedList)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to local tasks
            Result.success(getLocalTasks())
        }
    }

    suspend fun createGoogleTaskApi(title: String, notes: String? = null): Result<GoogleTaskItem> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val localId = "gt_local_${now}_${(100..999).random()}"
        val newTask = GoogleTaskItem(
            id = localId,
            title = title,
            notes = notes,
            status = "needsAction",
            createdTimestampMs = now
        )

        val token = googleOAuthToken
        if (!token.isNullOrEmpty()) {
            try {
                val jsonPayload = JSONObject().apply {
                    put("title", title)
                    notes?.let { put("notes", it) }
                }
                val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://tasks.googleapis.com/tasks/v1/lists/@default/tasks")
                    .header("Authorization", "Bearer $token")
                    .post(body)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val respObj = JSONObject(response.body?.string() ?: "{}")
                    val apiId = respObj.optString("id", localId)
                    val createdTask = newTask.copy(id = apiId)
                    val list = getLocalTasks().toMutableList()
                    list.add(0, createdTask)
                    saveLocalTasks(list)
                    return@withContext Result.success(createdTask)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Save locally
        val list = getLocalTasks().toMutableList()
        list.add(0, newTask)
        saveLocalTasks(list)
        Result.success(newTask)
    }

    suspend fun completeTaskApi(taskId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val token = googleOAuthToken
        if (!token.isNullOrEmpty() && !taskId.startsWith("gt_local_") && !taskId.startsWith("gt_sample_")) {
            try {
                val jsonPayload = JSONObject().apply {
                    put("status", "completed")
                }
                val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://tasks.googleapis.com/tasks/v1/lists/@default/tasks/$taskId")
                    .header("Authorization", "Bearer $token")
                    .patch(body)
                    .build()

                okHttpClient.newCall(request).execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Update local status
        val current = getLocalTasks().toMutableList()
        val index = current.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val updated = current[index].copy(
                status = "completed",
                completedTimestampMs = System.currentTimeMillis()
            )
            current[index] = updated
            saveLocalTasks(current)
        }
        Result.success(true)
    }

    fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
}
