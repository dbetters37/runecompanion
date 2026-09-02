package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.db.AppDatabase
import com.example.data.models.OsrsSkill
import com.example.data.models.ItemCategory
import com.example.data.models.PetState
import com.example.data.repository.PetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.random.Random

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.example.data.api.GeminiAiService

private fun formatCleanCompleteSentenceQuote(raw: String, maxChars: Int = 85): String {
    return com.example.data.repository.OsrsQuotesRepository.formatCleanCompleteSentenceQuote(raw, maxChars)
}

class FloatingPetOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var params: WindowManager.LayoutParams? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val petStateFlow = MutableStateFlow(PetState())
    private val currentAiQuoteFlow = MutableStateFlow("⚡ Flutter Mane has a Base Speed stat of 135!")
    private val geminiService = GeminiAiService()

    private fun setWindowFocusable(focusable: Boolean) {
        val p = params ?: return
        val wm = windowManager ?: return
        if (focusable) {
            p.flags = p.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        } else {
            p.flags = p.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }
        try {
            wm.updateViewLayout(overlayView, p)
        } catch (e: Exception) {}
    }


    @Volatile
    private var isBeingDragged = false

    private val aiQuotesList = listOf(
        "⚡ Flutter Mane has a Base Speed stat of 135!",
        "⚡ Regieleki leads with a Base Speed stat of 200!",
        "⚡ Deoxys (Speed Forme) holds a Base Speed stat of 180!",
        "⚡ Calyrex (Shadow Rider) commands a Base Speed stat of 150!",
        "⚡ Dragapult zips with a Base Speed stat of 142!",
        "⚡ Iron Bundle blazes with a Base Speed stat of 136!",
        "⚡ Chien-Pao rocks a Base Speed stat of 135!",
        "⚡ Koraidon has a Base Speed stat of 135!",
        "⚡ Miraidon charges in with a Base Speed stat of 135!",
        "⚡ Zacian (Crowned Sword) dashes in with a Base Speed stat of 148!",
        "⚡ Roaring Moon sweeps with a Base Speed stat of 119!",
        "⚡ Iron Valiant strikes fast with a Base Speed stat of 116!",
        "⚡ Whimsicott sets Tailwind with a Base Speed stat of 116!",
        "⚡ Garchomp outspeeds 100s with a Base Speed stat of 102!",
        "⚡ Incineroar Fake Out-pivots at a Base Speed stat of 60!",
        "⚡ Rillaboom plays Grassy Glide at a Base Speed stat of 85!",
        "⚡ Urshifu (Rapid Strike) has a Base Speed stat of 97!",
        "⚡ Gholdengo Make It Rains with a Base Speed stat of 84!",
        "⚡ Landorus (Therian Forme) has a Base Speed stat of 91!",
        "⚡ Kingambit Kowtow Cleaves with a Base Speed stat of 50!",
        "⚡ Iron Hands Drain Punches at a Base Speed stat of 50!",
        "⚡ Amoonguss Spores in Trick Room at a Base Speed stat of 30!",
        "⚡ Torkoal Erupts in Sun at a Base Speed stat of 20!"
    )

    override fun onCreate() {
        super.onCreate()
        isRunning.value = true
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        startForegroundNotification()

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = PetRepository(db.petDao())

        serviceScope.launch {
            repository.petState.collectLatest { state ->
                petStateFlow.value = state
            }
        }

        // Start AI creative wandering & occasional speech loop
        startAiCreativeEngine(repository)

        if (Settings.canDrawOverlays(this)) {
            showOverlayView(repository)
        }
    }

    private fun startAiCreativeEngine(repository: PetRepository) {
        // Coroutine 1: Creative Autonomous Wandering Movement
        serviceScope.launch {
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            while (serviceJob.isActive) {
                delay(Random.nextLong(2500, 5000))
                if (!isBeingDragged && windowManager != null && overlayView != null && params != null) {
                    val p = params ?: continue
                    val minX = 30
                    val maxX = (screenWidth - 220).coerceAtLeast(100)
                    val minY = 100
                    val maxY = (screenHeight - 350).coerceAtLeast(200)

                    // Choose a creative target offset (wander, hop, diagonal move)
                    val dx = Random.nextInt(-160, 160)
                    val dy = Random.nextInt(-160, 160)

                    val targetX = (p.x + dx).coerceIn(minX, maxX)
                    val targetY = (p.y + dy).coerceIn(minY, maxY)

                    val startX = p.x
                    val startY = p.y
                    val steps = 25

                    for (step in 1..steps) {
                        if (isBeingDragged) break
                        p.x = startX + ((targetX - startX) * (step / steps.toFloat())).toInt()
                        p.y = startY + ((targetY - startY) * (step / steps.toFloat())).toInt()
                        try {
                            windowManager?.updateViewLayout(overlayView, p)
                        } catch (e: Exception) {
                            break
                        }
                        delay(25L) // Smooth 40fps movement step
                    }
                }
            }
        }

        // Coroutine 2: Periodic AI Speech Bubbles with Phone Context & App Detection
        serviceScope.launch {
            var cycleCount = 0
            while (serviceJob.isActive) {
                delay(Random.nextLong(22000, 35000))
                val dbState = repository.getPetStateDirect()
                if (dbState != null && !dbState.isMuted) {
                    cycleCount++
                    val phoneContext = com.example.utils.PhoneContextHelper.refreshWeather(this@FloatingPetOverlayService)
                    val moodLevel = com.example.data.models.PetMoodLevel.fromScore((dbState.happiness + dbState.hunger) / 2)
                    
                    val randomQuote = geminiService.generateFreshOverlayQuote(
                        petName = dbState.customName,
                        petTypeDisplayName = dbState.petType.displayName,
                        petType = dbState.petType,
                        phoneContext = phoneContext,
                        moodLevel = moodLevel,
                        cycleIndex = cycleCount
                    )

                    currentAiQuoteFlow.value = randomQuote

                    // Save to repository as quote
                    try {
                        val updated = dbState.copy(currentQuote = randomQuote)
                        repository.savePetState(updated)
                    } catch (e: Exception) {}
                }
            }
        }
    }

    private fun startForegroundNotification() {

        val channelId = "floating_pet_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "OSRS Floating Pet",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("OSRS Floating Pet Active")
                .setContentText("Your pet companion is wandering freely over apps!")
                .setSmallIcon(com.example.R.drawable.ic_notification_shaman)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("OSRS Floating Pet Active")
                .setContentText("Your pet companion is wandering freely over apps!")
                .setSmallIcon(com.example.R.drawable.ic_notification_shaman)
                .build()
        }

        startForeground(999, notification)
    }

    private fun showOverlayView(repository: PetRepository) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 120
            y = 280
        }

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingPetOverlayService)
            setViewTreeSavedStateRegistryOwner(this@FloatingPetOverlayService)

            setContent {
                MaterialTheme(colorScheme = darkColorScheme()) {
                    val state by petStateFlow.collectAsState()
                    val aiQuote by currentAiQuoteFlow.collectAsState()
                    var isExpanded by remember { mutableStateOf(false) }
                    var isAfkSailing by remember { mutableStateOf(false) }

                    LaunchedEffect(isAfkSailing) {
                        while (isAfkSailing) {
                            kotlinx.coroutines.delay(8000L)
                            repository.addSkillXp(state.petType.name, OsrsSkill.SAILING, 45L)
                            currentAiQuoteFlow.value = "⛵ Rowed across blue waters! Gained +45 Sailing XP!"
                        }
                    }

                    fun checkActiveAfkActivityName(): String? {
                        val prefs = getSharedPreferences("poh_prefs", Context.MODE_PRIVATE)
                        return when {
                            prefs.getBoolean("afk_woodcutting", false) -> "Woodcutting"
                            prefs.getBoolean("afk_mining", false) -> "Mining"
                            prefs.getBoolean("afk_fishing", false) -> "Fishing"
                            prefs.getBoolean("afk_cooking", false) -> "Cooking"
                            prefs.getBoolean("afk_campfire", false) -> "Campfire"
                            prefs.getBoolean("afk_smelting", false) -> "Smelting"
                            prefs.getBoolean("afk_sawmill", false) -> "Sawmill"
                            prefs.getBoolean("afk_nail_crafting", false) -> "Nail Crafting"
                            prefs.getBoolean("afk_stick_crafting", false) -> "Stick Crafting"
                            prefs.getBoolean("afk_arrowtip_crafting", false) -> "Arrowtip Crafting"
                            prefs.getBoolean("afk_trap_crafting", false) -> "Hunter Trap Crafting"
                            prefs.getBoolean("afk_fletching", false) -> "Fletching"
                            prefs.getBoolean("afk_slayer", false) -> "Slayer"
                            prefs.getBoolean("afk_hunter", false) -> "Hunter"
                            prefs.getBoolean("afk_boss", false) -> "Boss Combat"
                            prefs.getBoolean("afk_farming", false) -> "Farming"
                            prefs.getBoolean("afk_bone_burying", false) -> "Bone Burying"
                            prefs.getBoolean("afk_sailing", false) -> "Sailing"
                            prefs.getBoolean("afk_runecrafting", false) -> "Runecrafting"
                            prefs.getBoolean("afk_thieving", false) -> "Pickpocketing Thieving"
                            prefs.getBoolean("afk_catacombs", false) || prefs.getBoolean("afk_sepulchre", false) -> "Shamanic Catacombs"
                            else -> null
                        }
                    }

                    fun stopAllAfkActivitiesInPrefs() {
                        val prefs = getSharedPreferences("poh_prefs", Context.MODE_PRIVATE)
                        prefs.edit()
                            .putBoolean("afk_woodcutting", false)
                            .putBoolean("afk_mining", false)
                            .putBoolean("afk_fishing", false)
                            .putBoolean("afk_cooking", false)
                            .putBoolean("afk_campfire", false)
                            .putBoolean("afk_smelting", false)
                            .putBoolean("afk_sawmill", false)
                            .putBoolean("afk_nail_crafting", false)
                            .putBoolean("afk_stick_crafting", false)
                            .putBoolean("afk_arrowtip_crafting", false)
                            .putBoolean("afk_trap_crafting", false)
                            .putBoolean("afk_fletching", false)
                            .putBoolean("afk_slayer", false)
                            .putBoolean("afk_hunter", false)
                            .putBoolean("afk_boss", false)
                            .putBoolean("afk_farming", false)
                            .putBoolean("afk_bone_burying", false)
                            .putBoolean("afk_sailing", false)
                            .putBoolean("afk_runecrafting", false)
                            .putBoolean("afk_thieving", false)
                            .putBoolean("afk_sepulchre", false)
                            .apply()
                    }

                    fun feedFromLowestFood() {
                        serviceScope.launch {
                            val currentInv = repository.getInventoryItemsDirect(state.petType.name)
                            val currentBank = repository.getBankItemsDirect(state.petType.name)
                            val invFood = currentInv.filter { it.isCookedReadyToEatFood && it.quantity > 0 }
                                .sortedBy { it.restoreHunger }
                            val bankFood = currentBank.filter { it.isCookedReadyToEatFood && it.quantity > 0 }
                                .sortedBy { it.restoreHunger }

                            val skillMap = repository.getAllSkillXpDirect(state.petType.name)
                            var totalLvl = 0
                            for (xp in skillMap.values) {
                                totalLvl += com.example.data.models.OsrsXpCalculator.getLevelForXp(xp)
                            }
                            val maxHunger = 100 + totalLvl

                            if (invFood.isNotEmpty()) {
                                val food = invFood.first()
                                val newQty = food.quantity - 1
                                repository.saveInventoryItem(state.petType.name, food.id, newQty)

                                val newHunger = (state.hunger + food.restoreHunger).coerceAtMost(maxHunger)
                                val newQuote = "🍖 Ate 1x ${food.name} (+${food.restoreHunger} Hunger)!"
                                currentAiQuoteFlow.value = newQuote
                                val updated = state.copy(hunger = newHunger, currentQuote = newQuote)
                                repository.savePetState(updated)
                            } else if (bankFood.isNotEmpty()) {
                                val food = bankFood.first()
                                val newQty = food.quantity - 1
                                repository.saveBankItem(state.petType.name, food.id, newQty)

                                val newHunger = (state.hunger + food.restoreHunger).coerceAtMost(maxHunger)
                                val newQuote = "🏦 Ate 1x ${food.name} from Bank (+${food.restoreHunger} Hunger)!"
                                currentAiQuoteFlow.value = newQuote
                                val updated = state.copy(hunger = newHunger, currentQuote = newQuote)
                                repository.savePetState(updated)
                            } else {
                                val newQuote = "⚠️ No cooked food in backpack or bank! Cook raw food first."
                                currentAiQuoteFlow.value = newQuote
                                val updated = state.copy(currentQuote = newQuote)
                                repository.savePetState(updated)
                            }
                        }
                    }

                    // Floating Overlay Pet Design
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        // Always show speech bubble if quote is present or expanded
                        Box(
                            modifier = Modifier
                                .width(235.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF221A14))
                                .border(1.2.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                val rawQuote = if (isExpanded) state.currentQuote else aiQuote
                                val formattedQuote = formatCleanCompleteSentenceQuote(rawQuote, maxChars = 90)
                                Text(
                                    text = "💬 \"$formattedQuote\"",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 14.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Row 1: Care & Sailing Actions
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Pet Button
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFC84B31))
                                                .clickable {
                                                    serviceScope.launch {
                                                        val newQuote = formatCleanCompleteSentenceQuote("💖 *Purrs happily!* (+10 Happiness)", maxChars = 72)
                                                        currentAiQuoteFlow.value = newQuote
                                                        val updated = state.copy(
                                                            happiness = (state.happiness + 10).coerceAtMost(100),
                                                            currentQuote = newQuote
                                                        )
                                                        repository.savePetState(updated)
                                                    }
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Pet 💖", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        // Feed Button
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF2E6B38))
                                                .clickable { feedFromLowestFood() }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Feed 🍖", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        // AFK Sail Button
                                        Box(
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isAfkSailing) Color(0xFF0284C7) else Color(0xFF1E293B))
                                                .clickable {
                                                    isAfkSailing = !isAfkSailing
                                                    val msg = if (isAfkSailing) "⛵ Pet in rowboat! Rowing across ocean..." else "⚓ Docked at port."
                                                    currentAiQuoteFlow.value = formatCleanCompleteSentenceQuote(msg, maxChars = 72)
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(if (isAfkSailing) "⛵ Rowing" else "⛵ Sail AFK", fontSize = 9.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Row 2: Secondary Controls (Deactivate AFK, Hide, Close)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Deactivate Last AFK Activity Button
                                        val activeAfkName = checkActiveAfkActivityName() ?: if (isAfkSailing) "Sailing" else null
                                        val hasActiveAfk = activeAfkName != null

                                        Box(
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (hasActiveAfk) Color(0xFFB91C1C) else Color(0xFF4A4A4A))
                                                .clickable {
                                                    val currentAfk = checkActiveAfkActivityName() ?: if (isAfkSailing) "Sailing" else null
                                                    if (currentAfk != null) {
                                                        if (state.energy >= 5 && state.hunger >= 5) {
                                                            isAfkSailing = false
                                                            stopAllAfkActivitiesInPrefs()
                                                            val newQuote = formatCleanCompleteSentenceQuote("⏹️ Deactivated AFK $currentAfk activity! (-5 Energy, -5 Hunger)", maxChars = 72)
                                                            currentAiQuoteFlow.value = newQuote
                                                            serviceScope.launch {
                                                                val updated = state.copy(
                                                                    energy = (state.energy - 5).coerceAtLeast(0),
                                                                    hunger = (state.hunger - 5).coerceAtLeast(0),
                                                                    currentQuote = newQuote
                                                                )
                                                                repository.savePetState(updated)
                                                            }
                                                        } else {
                                                            val newQuote = formatCleanCompleteSentenceQuote("⚠️ Low resources! Need 5+ Energy & Hunger to deactivate AFK safely.", maxChars = 72)
                                                            currentAiQuoteFlow.value = newQuote
                                                        }
                                                    } else {
                                                        val newQuote = formatCleanCompleteSentenceQuote("ℹ️ No active AFK activity to stop.", maxChars = 72)
                                                        currentAiQuoteFlow.value = newQuote
                                                    }
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Stop AFK ⏹️", fontSize = 9.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        // Hide Overlay Button
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF7F1D1D))
                                                .clickable { stopSelf() }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Hide 🙈", fontSize = 9.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }

                                        // Collapse Bubble Button
                                        Box(
                                            modifier = Modifier
                                                .weight(0.8f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF4A4A4A))
                                                .clickable {
                                                    isExpanded = false
                                                    setWindowFocusable(false)
                                                }
                                                .padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Close ✕", fontSize = 9.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Pet Floating Sprite Box with Gesture Drag + Tap
                        val infiniteTransition = rememberInfiniteTransition(label = "overlay_bounce")
                        val bounceOffset by infiniteTransition.animateFloat(
                            initialValue = -5f,
                            targetValue = 5f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(700, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "bounce"
                        )

                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        var totalDragX = 0f
                                        var totalDragY = 0f
                                        var isDragging = false
                                        isBeingDragged = true

                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull() ?: break
                                            if (change.pressed) {
                                                val delta = change.positionChange()
                                                if (delta.x != 0f || delta.y != 0f) {
                                                    totalDragX += Math.abs(delta.x)
                                                    totalDragY += Math.abs(delta.y)
                                                    if (totalDragX > 5f || totalDragY > 5f) {
                                                        isDragging = true
                                                    }
                                                    if (isDragging) {
                                                        change.consume()
                                                        val p = params
                                                        if (p != null) {
                                                            p.x += delta.x.toInt()
                                                            p.y += delta.y.toInt()
                                                            try {
                                                                windowManager?.updateViewLayout(overlayView, p)
                                                            } catch (e: Exception) {}
                                                        }
                                                    }
                                                }
                                            } else {
                                                break
                                            }
                                        }

                                        isBeingDragged = false
                                        if (!isDragging) {
                                            val nextExpanded = !isExpanded
                                            isExpanded = nextExpanded
                                            setWindowFocusable(nextExpanded)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier.offset(y = bounceOffset.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAfkSailing) {
                                    Box(
                                        modifier = Modifier
                                            .size(82.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0077B6).copy(alpha = 0.55f))
                                            .border(1.5.dp, Color(0xFF90E0EF), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.BottomCenter) {
                                                Text(
                                                    text = state.petType.iconSymbol,
                                                    fontSize = 26.sp,
                                                    modifier = Modifier.offset(y = (-6).dp)
                                                )
                                                Text(
                                                    text = "🛶",
                                                    fontSize = 34.sp
                                                )
                                            }
                                            Text("⛵ Sailing 🌊", color = Color(0xFFE0F1FF), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else if (state.petType == com.example.data.models.PetType.TANGLEROOT) {
                                    com.example.ui.components.ShamanicTreantSprite(sizeDp = 64.dp)
                                } else {
                                    Text(
                                        text = state.petType.iconSymbol,
                                        fontSize = 48.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Enable smooth grab-and-drag touch listener
            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f

            setOnTouchListener { view, event ->
                val p = params ?: return@setOnTouchListener false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isBeingDragged = true
                        initialX = p.x
                        initialY = p.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        p.x = initialX + (event.rawX - initialTouchX).toInt()
                        p.y = initialY + (event.rawY - initialTouchY).toInt()
                        try {
                            windowManager?.updateViewLayout(overlayView, p)
                        } catch (e: Exception) {}
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isBeingDragged = false
                        val diffX = Math.abs(event.rawX - initialTouchX)
                        val diffY = Math.abs(event.rawY - initialTouchY)
                        if (diffX < 10 && diffY < 10) {
                            view.performClick()
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        windowManager?.addView(overlayView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning.value = false
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceJob.cancel()
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        val isRunning = MutableStateFlow(false)
    }
}
