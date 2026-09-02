package com.example.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.PetRepository
import com.example.data.ai.GeminiPetService
import com.example.data.ai.PersonalityEngine
import com.example.data.db.AppDatabase
import com.example.data.db.ConversationEntity
import com.example.data.db.PersonalityEntity
import com.example.data.drive.GoogleDriveSyncManager
import com.example.ui.components.PetExpression
import com.example.ui.components.ShamanPetCanvas
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.cos
import kotlin.math.sin

class PetOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null

    private lateinit var repository: PetRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var wanderingJob: Job? = null

    // Learned movement memory ratios and perch zone
    private var favXRatio = 0.50f
    private var favYRatio = 0.18f
    private var favoriteZone = "Header Overlook"
    private var isChatExpanded = false
    private var isScreenInteractive = true

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenInteractive = false
                    wanderingJob?.cancel()
                }
                Intent.ACTION_SCREEN_ON,
                Intent.ACTION_USER_PRESENT -> {
                    isScreenInteractive = true
                    if (!isChatExpanded && (wanderingJob == null || wanderingJob?.isActive != true)) {
                        startLifelikeWanderingLoop()
                    }
                    ensureOverlayAttachedAndVisible()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val db = AppDatabase.getDatabase(applicationContext)
        val driveSyncManager = GoogleDriveSyncManager(applicationContext, db.driveSyncLogDao())
        val geminiService = GeminiPetService()
        repository = PetRepository(
            conversationDao = db.conversationDao(),
            memoryDao = db.memoryDao(),
            personalityDao = db.personalityDao(),
            personalityLogDao = db.personalityLogDao(),
            driveSyncLogDao = db.driveSyncLogDao(),
            telemetryDao = db.telemetryDao(),
            petOpinionDao = db.petOpinionDao(),
            condensedMemoryDao = db.condensedMemoryDao(),
            movementBehaviorDao = db.movementBehaviorDao(),
            petDailyJournalDao = db.petDailyJournalDao(),
            petDreamJournalDao = db.petDreamJournalDao(),
            googleSearchLogDao = db.googleSearchLogDao(),
            personalityStateTrackerDao = db.personalityStateTrackerDao(),
            brainLobeStateDao = db.brainLobeStateDao(),
            brainNeuralLogDao = db.brainNeuralLogDao(),
            subjectiveWorldModelDao = db.subjectiveWorldModelDao(),
            autonomousGoalDao = db.autonomousGoalDao(),
            persistentMemoryLoopDao = db.persistentMemoryLoopDao(),
            driveSyncManager = driveSyncManager,
            geminiPetService = geminiService
        )

        isOverlayRunning = true

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, true).apply()

        startForegroundServiceNotification()
        setupFloatingWindow()
        loadMovementMemory()
        registerScreenStateReceiver()
        startHeartbeatVerifier()

        // Also ensure today's journals are populated
        serviceScope.launch(Dispatchers.IO) {
            try {
                repository.ensureDailyJournalsFilledForToday()
            } catch (_: Exception) { }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_OVERLAY) {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, false).apply()
            stopSelf()
            return START_NOT_STICKY
        } else if (action == ACTION_RECENTER_PET) {
            recenterPetPosition()
        }

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, true).apply()

        isOverlayRunning = true
        startForegroundServiceNotification()

        ensureOverlayAttachedAndVisible()

        if (!isChatExpanded && (wanderingJob == null || wanderingJob?.isActive != true)) {
            startLifelikeWanderingLoop()
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_OVERLAY_ENABLED, true)) {
            val restartServiceIntent = Intent(applicationContext, PetOverlayService::class.java).apply {
                setPackage(packageName)
            }
            try {
                val restartPendingIntent = PendingIntent.getService(
                    applicationContext,
                    101,
                    restartServiceIntent,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
                alarmManager?.set(
                    android.app.AlarmManager.ELAPSED_REALTIME,
                    android.os.SystemClock.elapsedRealtime() + 1500,
                    restartPendingIntent
                )
            } catch (_: Exception) { }
        }
    }

    private fun registerScreenStateReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(screenStateReceiver, filter)
            }
        } catch (_: Exception) { }
    }

    private fun startHeartbeatVerifier() {
        serviceScope.launch {
            while (isActive) {
                delay(15000L)
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                isScreenInteractive = powerManager?.isInteractive ?: true
                if (isScreenInteractive) {
                    ensureOverlayAttachedAndVisible()
                }
            }
        }
    }

    private fun ensureOverlayAttachedAndVisible() {
        try {
            if (overlayView == null || windowManager == null) {
                setupFloatingWindow()
            } else {
                if (overlayView?.parent == null) {
                    windowManager?.addView(overlayView, windowLayoutParams)
                }
            }
        } catch (_: Exception) { }
    }

    fun recenterPetPosition() {
        val metrics = resources.displayMetrics
        val targetX = (metrics.widthPixels * 0.75f).toInt().coerceIn(12, (metrics.widthPixels - 120).coerceAtLeast(12))
        val targetY = (metrics.heightPixels * 0.20f).toInt().coerceIn(50, (metrics.heightPixels - 140).coerceAtLeast(50))

        windowLayoutParams?.let { params ->
            params.x = targetX
            params.y = targetY
            try {
                windowManager?.updateViewLayout(overlayView, params)
            } catch (_: Exception) { }
        }
    }

    private fun loadMovementMemory() {
        serviceScope.launch {
            repository.movementBehavior.collect { movement ->
                if (movement != null) {
                    favXRatio = movement.favoriteXRatio
                    favYRatio = movement.favoriteYRatio
                    favoriteZone = movement.favoriteZone

                    val targetXRatio = movement.currentXRatio
                    val targetYRatio = movement.currentYRatio

                    val metrics = resources.displayMetrics
                    val targetX = (metrics.widthPixels * targetXRatio).toInt().coerceIn(12, (metrics.widthPixels - 120).coerceAtLeast(12))
                    val targetY = (metrics.heightPixels * targetYRatio).toInt().coerceIn(50, (metrics.heightPixels - 140).coerceAtLeast(50))

                    if (!isChatExpanded) {
                        smoothGlideTo(targetX, targetY)
                    }
                }
            }
        }
    }

    private fun smoothGlideTo(targetX: Int, targetY: Int) {
        val params = windowLayoutParams ?: return
        val startX = params.x
        val startY = params.y
        val dx = targetX - startX
        val dy = targetY - startY
        if (kotlin.math.abs(dx) < 5 && kotlin.math.abs(dy) < 5) return

        serviceScope.launch {
            val steps = 16
            for (step in 1..steps) {
                val t = step / steps.toFloat()
                val ease = 1f - (1f - t) * (1f - t)
                params.x = (startX + dx * ease).toInt()
                params.y = (startY + dy * ease).toInt()
                try {
                    windowManager?.updateViewLayout(overlayView, params)
                } catch (_: Exception) { }
                delay(16)
            }
        }
    }

    private fun startForegroundServiceNotification() {
        val channelId = "pet_overlay_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Floating Pet Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps your floating AI pet active on screen"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val recenterIntent = Intent(this, PetOverlayService::class.java).apply {
            action = ACTION_RECENTER_PET
        }
        val recenterPendingIntent = PendingIntent.getService(
            this, 1, recenterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, PetOverlayService::class.java).apply {
            action = ACTION_STOP_OVERLAY
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Shaman Companion Floating")
            .setContentText("Your AI companion is wandering peacefully on your screen")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_rotate, "Recenter Pet", recenterPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Hide", stopPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(2001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(2001, notification)
        }
    }

    private fun updateWindowFocusState(chatOpen: Boolean) {
        isChatExpanded = chatOpen
        val params = windowLayoutParams ?: return
        val metrics = resources.displayMetrics
        val density = metrics.density

        if (chatOpen) {
            // Remove FLAG_NOT_FOCUSABLE so soft keyboard can open for text input
            params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE

            // Clamp position so chat card fits within screen
            val chatW = (310 * density).toInt()
            val chatH = (390 * density).toInt()
            val maxX = (metrics.widthPixels - chatW - 12).coerceAtLeast(12)
            val maxY = (metrics.heightPixels - chatH - 40).coerceAtLeast(50)

            params.x = params.x.coerceIn(12, maxX)
            params.y = params.y.coerceIn(50, maxY)

            // Pause wandering while interacting with chat
            wanderingJob?.cancel()
        } else {
            // Restore FLAG_NOT_FOCUSABLE so touches pass through to underlying apps
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED

            // Resume lifelike gentle wandering
            startLifelikeWanderingLoop()
        }

        try {
            windowManager?.updateViewLayout(overlayView, params)
        } catch (_: Exception) { }
    }

    private fun setupFloatingWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val metrics = resources.displayMetrics
        val screenW = metrics.widthPixels
        val screenH = metrics.heightPixels

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        windowLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (screenW * favXRatio).toInt().coerceIn(12, (screenW - 120).coerceAtLeast(12))
            y = (screenH * favYRatio).toInt().coerceIn(50, (screenH - 140).coerceAtLeast(50))
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@PetOverlayService)
            setViewTreeSavedStateRegistryOwner(this@PetOverlayService)

            setContent {
                var isChatOpen by remember { mutableStateOf(false) }
                var speechText by remember { mutableStateOf("Perched & listening ✨") }
                var showSpeechBubble by remember { mutableStateOf(true) }
                var expression by remember { mutableStateOf(PetExpression.HAPPY) }
                var isAiGenerating by remember { mutableStateOf(false) }

                val conversations by repository.allConversations.collectAsState(initial = emptyList())
                val personality by repository.personality.collectAsState(initial = null)

                // Hide initial speech bubble after delay
                LaunchedEffect(Unit) {
                    delay(4000)
                    showSpeechBubble = false
                }

                // Sync window focus whenever chat expands or collapses
                LaunchedEffect(isChatOpen) {
                    updateWindowFocusState(isChatOpen)
                }

                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(4.dp)
                ) {
                    if (!isChatOpen) {
                        // ==========================================
                        // COMPACT FLOATING PET AVATAR MODE
                        // ==========================================
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Compact Speech Bubble
                            AnimatedVisibility(
                                visible = showSpeechBubble,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xEE0A1F13),
                                    border = BorderStroke(1.dp, Color(0xFF00F5D4)),
                                    modifier = Modifier.padding(bottom = 2.dp)
                                ) {
                                    Text(
                                        text = speechText,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Draggable Pet Avatar Container - Clean Pet Only
                            Box(
                                modifier = Modifier
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = {
                                                wanderingJob?.cancel()
                                            },
                                            onDragEnd = {
                                                recordMovementAndResumeWander()
                                            },
                                            onDragCancel = {
                                                recordMovementAndResumeWander()
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val params = this@PetOverlayService.windowLayoutParams ?: return@detectDragGestures
                                                val curMetrics = resources.displayMetrics
                                                val maxX = (curMetrics.widthPixels - 90).coerceAtLeast(12)
                                                val maxY = (curMetrics.heightPixels - 110).coerceAtLeast(50)

                                                params.x = (params.x + dragAmount.x.toInt()).coerceIn(12, maxX)
                                                params.y = (params.y + dragAmount.y.toInt()).coerceIn(50, maxY)
                                                try {
                                                    windowManager?.updateViewLayout(overlayView, params)
                                                } catch (_: Exception) { }
                                            }
                                        )
                                    }
                            ) {
                                ShamanPetCanvas(
                                    expression = expression,
                                    skin = personality?.activeSkin ?: "SHAMAN_DEFAULT",
                                    size = 61.dp, // 10% smaller compact companion
                                    showFpsBadge = false,
                                    onClick = {
                                        // Tap opens the interactive AI mini chat!
                                        isChatOpen = true
                                    },
                                    onTactileTouch = {
                                        expression = PetExpression.PLAYFUL
                                        speechText = "Ooh! Warm headpat! 💖"
                                        showSpeechBubble = true
                                    }
                                )
                            }
                        }
                    } else {
                        // ==========================================
                        // EXPANDED FLOATING AI CHAT LOG WINDOW
                        // ==========================================
                        OverlayMiniChatCard(
                            conversations = conversations,
                            personality = personality,
                            expression = expression,
                            favoriteZone = favoriteZone,
                            isAiGenerating = isAiGenerating,
                            onCloseChat = { isChatOpen = false },
                            onOpenFullApp = {
                                val intent = Intent(this@PetOverlayService, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                startActivity(intent)
                            },
                            onStopOverlay = { stopOverlay(this@PetOverlayService) },
                            onSendMessage = { userText ->
                                if (userText.isNotBlank() && !isAiGenerating) {
                                    isAiGenerating = true
                                    expression = PetExpression.THINKING
                                    serviceScope.launch {
                                        try {
                                            val result = repository.sendMessageAndReceiveResponse(userText)
                                            speechText = result.petReplyText.take(60)
                                            val parsedExpression = try {
                                                PetExpression.valueOf(result.expression.uppercase())
                                            } catch (_: Exception) {
                                                PetExpression.HAPPY
                                            }
                                            expression = parsedExpression
                                        } catch (_: Exception) {
                                            expression = PetExpression.HAPPY
                                        } finally {
                                            isAiGenerating = false
                                        }
                                    }
                                }
                            },
                            onHeaderDrag = { dragOffset ->
                                val params = this@PetOverlayService.windowLayoutParams ?: return@OverlayMiniChatCard
                                val curMetrics = resources.displayMetrics
                                val density = curMetrics.density
                                val chatW = (310 * density).toInt()
                                val chatH = (390 * density).toInt()
                                val maxX = (curMetrics.widthPixels - chatW - 12).coerceAtLeast(12)
                                val maxY = (curMetrics.heightPixels - chatH - 40).coerceAtLeast(50)

                                params.x = (params.x + dragOffset.x.toInt()).coerceIn(12, maxX)
                                params.y = (params.y + dragOffset.y.toInt()).coerceIn(50, maxY)
                                try {
                                    windowManager?.updateViewLayout(overlayView, params)
                                } catch (_: Exception) { }
                            },
                            onHeaderDragEnd = {
                                recordMovementAndResumeWander()
                            }
                        )
                    }
                }
            }
        }

        overlayView = composeView
        try {
            windowManager?.addView(overlayView, windowLayoutParams)
            startLifelikeWanderingLoop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun recordMovementAndResumeWander() {
        val params = windowLayoutParams ?: return
        serviceScope.launch {
            val curMetrics = resources.displayMetrics
            val updatedBehavior = repository.recordPetMovement(
                params.x.toFloat(),
                params.y.toFloat(),
                curMetrics.widthPixels.toFloat(),
                curMetrics.heightPixels.toFloat()
            )
            favXRatio = updatedBehavior.favoriteXRatio
            favYRatio = updatedBehavior.favoriteYRatio
            favoriteZone = updatedBehavior.favoriteZone
        }
        if (!isChatExpanded) {
            startLifelikeWanderingLoop()
        }
    }

    /**
     * Organic, lifelike wandering engine:
     * - Stays resting peacefully at favorite perch with gentle, subtle breathing hover.
     * - Periodically performs a smooth, graceful glide (cosine easing at 30 FPS) to observe nearby.
     * - Strictly clamps within screen boundaries.
     */
    private fun startLifelikeWanderingLoop() {
        wanderingJob?.cancel()
        wanderingJob = serviceScope.launch {
            val metrics = resources.displayMetrics

            while (isActive && !isChatExpanded) {
                // 1. Rest at perch / current spot with subtle life-like breathing hover for 8-14 seconds
                val restDurationMs = (8000..14000).random()
                val restSteps = restDurationMs / 60
                var breathTime = 0f

                val startParams = windowLayoutParams ?: break
                val anchorX = startParams.x
                val anchorY = startParams.y

                for (i in 0 until restSteps) {
                    if (!isActive || isChatExpanded) break
                    delay(60)
                    breathTime += 0.05f

                    // Very gentle floating breathing oscillation (±1.5 pixels)
                    val floatY = (sin(breathTime) * 1.5f).toInt()
                    val curParams = windowLayoutParams ?: break
                    curParams.y = (anchorY + floatY).coerceIn(50, (metrics.heightPixels - 120).coerceAtLeast(50))
                    try {
                        windowManager?.updateViewLayout(overlayView, curParams)
                    } catch (_: Exception) {
                        break
                    }
                }

                if (!isActive || isChatExpanded) break

                // 2. Decide on a natural target waypoint near favorite perch or gentle exploration point
                val targetFavX = (metrics.widthPixels * favXRatio).toInt().coerceIn(12, (metrics.widthPixels - 100).coerceAtLeast(12))
                val targetFavY = (metrics.heightPixels * favYRatio).toInt().coerceIn(50, (metrics.heightPixels - 130).coerceAtLeast(50))

                // Small wandering offset (±40 px)
                val wanderOffsetX = (-40..40).random()
                val wanderOffsetY = (-30..30).random()

                val goalX = (targetFavX + wanderOffsetX).coerceIn(12, (metrics.widthPixels - 100).coerceAtLeast(12))
                val goalY = (targetFavY + wanderOffsetY).coerceIn(50, (metrics.heightPixels - 130).coerceAtLeast(50))

                val curParams = windowLayoutParams ?: break
                val fromX = curParams.x
                val fromY = curParams.y

                // Smooth Glide along ease-in-out curve over 2.5 seconds (75 frames @ 33ms)
                val totalFrames = 75
                for (frame in 1..totalFrames) {
                    if (!isActive || isChatExpanded) break
                    delay(33)
                    val t = frame.toFloat() / totalFrames.toFloat()
                    // Smooth cosine ease-in-out curve
                    val ease = (1f - cos(t * Math.PI.toFloat())) / 2f

                    val interpX = (fromX + (goalX - fromX) * ease).toInt()
                    val interpY = (fromY + (goalY - fromY) * ease).toInt()

                    curParams.x = interpX.coerceIn(12, (metrics.widthPixels - 100).coerceAtLeast(12))
                    curParams.y = interpY.coerceIn(50, (metrics.heightPixels - 130).coerceAtLeast(50))

                    try {
                        windowManager?.updateViewLayout(overlayView, curParams)
                    } catch (_: Exception) {
                        break
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isOverlayRunning = false
        wanderingJob?.cancel()
        serviceScope.cancel()

        try {
            unregisterReceiver(screenStateReceiver)
        } catch (_: Exception) { }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        try {
            if (overlayView != null) {
                windowManager?.removeView(overlayView)
                overlayView = null
            }
        } catch (_: Exception) { }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val PREFS_NAME = "shaman_overlay_prefs"
        const val KEY_OVERLAY_ENABLED = "overlay_enabled"
        const val ACTION_STOP_OVERLAY = "com.example.ACTION_STOP_OVERLAY"
        const val ACTION_RECENTER_PET = "com.example.ACTION_RECENTER_PET"

        var isOverlayRunning = false
            private set

        fun startOverlay(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, true).apply()

            val intent = Intent(context, PetOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopOverlay(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, false).apply()

            val intent = Intent(context, PetOverlayService::class.java).apply {
                action = ACTION_STOP_OVERLAY
            }
            context.stopService(intent)
        }

        fun recenterOverlay(context: Context) {
            val intent = Intent(context, PetOverlayService::class.java).apply {
                action = ACTION_RECENTER_PET
            }
            context.startService(intent)
        }
    }
}

/**
 * Sleek, compact floating AI Chat Card displayed directly in the system overlay window.
 */
@Composable
fun OverlayMiniChatCard(
    conversations: List<ConversationEntity>,
    personality: PersonalityEntity?,
    expression: PetExpression,
    favoriteZone: String,
    isAiGenerating: Boolean,
    onCloseChat: () -> Unit,
    onOpenFullApp: () -> Unit,
    onStopOverlay: () -> Unit,
    onSendMessage: (String) -> Unit,
    onHeaderDrag: (Offset) -> Unit,
    onHeaderDragEnd: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val petName = personality?.petName ?: "Aura"
    val archetype = remember(personality?.archetype) {
        PersonalityEngine.getArchetypeById(personality?.archetype ?: "SHAMAN_GUARDIAN")
    }

    // Auto-scroll to latest message
    LaunchedEffect(conversations.size, isAiGenerating) {
        if (conversations.isNotEmpty()) {
            listState.animateScrollToItem(conversations.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .width(310.dp)
            .heightIn(max = 390.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xF2081C11))
            .border(1.2.dp, Brush.linearGradient(listOf(Color(0xFF00F5D4), Color(0xFF52B788))), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Draggable Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF102D1B))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { onHeaderDragEnd() },
                            onDragCancel = { onHeaderDragEnd() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onHeaderDrag(dragAmount)
                            }
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ShamanPetCanvas(
                        expression = expression,
                        skin = personality?.activeSkin ?: "SHAMAN_DEFAULT",
                        size = 30.dp,
                        showFpsBadge = false
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "$petName ✨",
                                color = Color(0xFF00F5D4),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E4D30),
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Text(
                                    text = "Lv.${personality?.level ?: 1}",
                                    color = Color(0xFFFFD166),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "📍 $favoriteZone",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Control Icons: Expand to full app, minimize chat, close overlay
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Open full app
                    IconButton(
                        onClick = onOpenFullApp,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "Open Full App",
                            tint = Color(0xFF00B4D8),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Minimize / Hide chat
                    IconButton(
                        onClick = onCloseChat,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Minimize Chat",
                            tint = Color(0xFFFF4D6D),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // 2. Chat Log Feed
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (conversations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Speak to $petName anytime... I learn & remember everything you tell me! 🌿",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                } else {
                    items(conversations.takeLast(12)) { item ->
                        OverlayChatBubble(item = item, petName = petName)
                    }
                }

                if (isAiGenerating) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = Color(0xFF00F5D4),
                                strokeWidth = 1.8.dp
                            )
                            Text(
                                text = "$petName is channeling thoughts... ✨",
                                color = Color(0xFF00F5D4),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 3. Quick AI Prompt Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                val chips = listOf(
                    "Tell me a deep thought 🔮",
                    "How are you feeling? 🍵",
                    "What do you remember about me? 📜",
                    "Give me gentle advice 🌿",
                    "Share an epiphany ✨"
                )
                items(chips) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF133821),
                        border = BorderStroke(0.6.dp, Color(0x6652B788)),
                        modifier = Modifier.clickable {
                            onSendMessage(prompt)
                        }
                    ) {
                        Text(
                            text = prompt,
                            color = Color(0xFFE0E0E0),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // 4. Message Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C2415))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    cursorBrush = SolidColor(Color(0xFF00F5D4)),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank() && !isAiGenerating) {
                            onSendMessage(inputText.trim())
                            inputText = ""
                        }
                    }),
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF183B27), RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFF2D6A4F), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Message $petName...",
                                color = Color.Gray,
                                fontSize = 11.5.sp
                            )
                        }
                        innerTextField()
                    }
                )

                // Send Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() && !isAiGenerating) Color(0xFF00F5D4) else Color(0xFF1E4D30)
                        )
                        .clickable(enabled = inputText.isNotBlank() && !isAiGenerating) {
                            onSendMessage(inputText.trim())
                            inputText = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isAiGenerating) Color(0xFF003822) else Color.Gray,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // =========================================================================
        // CLICKABLE BORDER TAP-TO-CLOSE STRIPS (TOP, LEFT, RIGHT — NOT BOTTOM)
        // =========================================================================
        // Top border strip (height 10.dp)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(10.dp)
                .clickable(onClick = onCloseChat)
        )

        // Left border strip (width 12.dp, top to above input bar)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxHeight(0.85f)
                .width(12.dp)
                .clickable(onClick = onCloseChat)
        )

        // Right border strip (width 12.dp, top to above input bar)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight(0.85f)
                .width(12.dp)
                .clickable(onClick = onCloseChat)
        )
    }
}

@Composable
fun OverlayChatBubble(item: ConversationEntity, petName: String) {
    val isUser = item.sender == "USER"
    val align = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Text(
            text = if (isUser) "YOU" else petName.uppercase(),
            color = if (isUser) Color(0xFFFFD166) else Color(0xFF00F5D4),
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 1.dp)
        )

        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
            ),
            color = if (isUser) Color(0xFF1B4D3E) else Color(0xFF112E1C),
            border = BorderStroke(
                0.8.dp,
                if (isUser) Color(0xFF2D6A4F) else Color(0xFF00F5D4).copy(alpha = 0.4f)
            )
        ) {
            Text(
                text = item.message,
                color = Color.White,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            )
        }
    }
}
