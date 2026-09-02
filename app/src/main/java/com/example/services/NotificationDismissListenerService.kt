package com.example.services

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.db.AppDatabase
import com.example.data.models.OsrsSkill
import com.example.data.repository.PetRepository
import com.example.widget.OsrsPetWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class NotificationDismissListenerService : NotificationListenerService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val pkg = sbn?.packageName ?: "app"
        _appEventFlow.tryEmit(pkg)
        com.example.utils.PhoneContextHelper.updateLastActiveApp(applicationContext, pkg)

        // Process background XP directly to database
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = PetRepository(db.petDao())
            val petTypeName = db.petDao().getPetStateDirect()?.petTypeName ?: "PIKACHU"
            handleBackgroundAppEvent(repository, petTypeName, pkg, "Notification Arrived")
            updateWidget(applicationContext, repository)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        val pkg = sbn?.packageName ?: "app"
        _notificationDismissedFlow.tryEmit(pkg)
        _appEventFlow.tryEmit(pkg)
        com.example.utils.PhoneContextHelper.updateLastActiveApp(applicationContext, pkg)

        // Process background notification swipe XP directly to database
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = PetRepository(db.petDao())
            val petTypeName = db.petDao().getPetStateDirect()?.petTypeName ?: "PIKACHU"

            // 1. Award Woodcutting XP & tier logs for swiping notification based on equipped Axe
            val equippedMap = repository.getEquippedItemsDirect(petTypeName)
            val axe = equippedMap[com.example.data.models.EquipmentSlot.AXE]
            val (logItemId, logName, xpReward) = when (axe?.id) {
                "item_bronze_axe" -> Triple("item_logs", "Logs", 100L)
                "item_iron_axe" -> Triple("item_oak_logs", "Oak Logs", 200L)
                "item_steel_axe" -> Triple("item_willow_logs", "Willow Logs", 350L)
                "item_mithril_axe" -> Triple("item_maple_logs", "Maple Logs", 500L)
                "item_adamant_axe" -> Triple("item_yew_logs", "Yew Logs", 750L)
                "item_rune_axe" -> Triple("item_magic_logs", "Magic Logs", 1100L)
                "item_dragon_axe" -> Triple("item_redwood_logs", "Redwood Logs", 1600L)
                else -> Triple("item_logs", "Logs", 50L)
            }

            repository.addXpToSkillDirect(
                petTypeName = petTypeName,
                skill = OsrsSkill.WOODCUTTING,
                amount = xpReward,
                gpReward = 20L,
                logTitle = "Swiped Notification ($pkg)",
                logDesc = "Cleared notification clutter from $pkg while app was closed & chopped 1x $logName using ${axe?.name ?: "Basic Axe"}!"
            )

            // Add tier logs to inventory
            val existingInv = db.petDao().getInventoryItemsDirect(petTypeName).find { it.itemId == logItemId }
            val currentQty = existingInv?.quantity ?: 0
            repository.saveInventoryItem(petTypeName, logItemId, currentQty + 1)

            // 2. Check category-specific background XP
            handleBackgroundAppEvent(repository, petTypeName, pkg, "Swiped Notification")

            // 3. Update Widget
            updateWidget(applicationContext, repository)
        }
    }

    private val lastMediaXpTime = java.util.concurrent.ConcurrentHashMap<String, Long>()

    private suspend fun handleBackgroundAppEvent(repository: PetRepository, petTypeName: String, pkg: String, actionType: String) {
        val pkgLower = pkg.lowercase()
        val now = System.currentTimeMillis()
        val lastTime = lastMediaXpTime[pkgLower] ?: 0L

        // Throttle media app notifications so song skipping doesn't spam XP
        if (now - lastTime < 20_000L) {
            return
        }

        val xpManager = com.example.data.models.TaskXpManager(applicationContext)
        val customPackages = xpManager.getCustomAppPackages()
        val isCustomApp = customPackages.any { pkgLower.contains(it) }

        when {
            pkgLower.contains("duolingo") || pkgLower.contains("babbel") || pkgLower.contains("memrise") || pkgLower.contains("rosetta") || pkgLower.contains("busuu") || pkgLower.contains("lingodeeer") || pkgLower.contains("language") -> {
                lastMediaXpTime[pkgLower] = now
                val xp = xpManager.getTaskXp("duolingo", 600L)
                val gp = xpManager.getTaskGp("duolingo", 150L)
                repository.addXpToSkillDirect(
                    petTypeName = petTypeName,
                    skill = OsrsSkill.MAGIC,
                    amount = xp,
                    gpReward = gp,
                    logTitle = "$actionType: Duolingo Practice ($pkg)",
                    logDesc = "Gained Magic XP from Duolingo language lesson!"
                )
            }
            pkgLower.contains("youtube") || pkgLower.contains("netflix") || pkgLower.contains("hulu") || pkgLower.contains("twitch") || pkgLower.contains("peacock") -> {
                lastMediaXpTime[pkgLower] = now
                val xp = xpManager.getTaskXp("streaming", 600L)
                val gp = xpManager.getTaskGp("streaming", 150L)
                repository.addXpToSkillDirect(
                    petTypeName = petTypeName,
                    skill = OsrsSkill.MAGIC,
                    amount = xp,
                    gpReward = gp,
                    logTitle = "$actionType: Streaming ($pkg)",
                    logDesc = "Gained Magic XP from streaming media notification!"
                )
            }
            pkgLower.contains("audible") || pkgLower.contains("books") || pkgLower.contains("kindle") || pkgLower.contains("audiobook") -> {
                lastMediaXpTime[pkgLower] = now
                val xp = xpManager.getTaskXp("audiobook", 500L)
                val gp = xpManager.getTaskGp("audiobook", 120L)
                repository.addXpToSkillDirect(
                    petTypeName = petTypeName,
                    skill = OsrsSkill.RUNECRAFT,
                    amount = xp,
                    gpReward = gp,
                    logTitle = "$actionType: Audiobook ($pkg)",
                    logDesc = "Gained Runecraft XP from reading/audiobook notification!"
                )
            }
            pkgLower.contains("spotify") || pkgLower.contains("music") || pkgLower.contains("pandora") || pkgLower.contains("apple music") -> {
                lastMediaXpTime[pkgLower] = now
                val xp = xpManager.getTaskXp("music", 450L)
                val gp = xpManager.getTaskGp("music", 100L)
                repository.addXpToSkillDirect(
                    petTypeName = petTypeName,
                    skill = OsrsSkill.MAGIC,
                    amount = xp,
                    gpReward = gp,
                    logTitle = "$actionType: Music ($pkg)",
                    logDesc = "Gained Incantations XP from music streaming notification!"
                )
            }
            pkgLower.contains("fit") || pkgLower.contains("health") || pkgLower.contains("strava") || pkgLower.contains("pedometer") -> {
                lastMediaXpTime[pkgLower] = now
                val xp = xpManager.getTaskXp("fitness", 500L)
                val gp = xpManager.getTaskGp("fitness", 100L)
                repository.addXpToSkillDirect(
                    petTypeName = petTypeName,
                    skill = OsrsSkill.AGILITY,
                    amount = xp,
                    gpReward = gp,
                    logTitle = "$actionType: Fitness ($pkg)",
                    logDesc = "Gained Agility XP from fitness notification!"
                )
            }
            pkgLower.contains("lens") || pkgLower.contains("googlequicksearchbox") || pkgLower.contains("ar.lens") || pkgLower.contains("smartlens") -> {
                lastMediaXpTime[pkgLower] = now
                val xp = xpManager.getTaskXp("lens", 450L)
                val gp = xpManager.getTaskGp("lens", 120L)
                repository.addXpToSkillDirect(
                    petTypeName = petTypeName,
                    skill = OsrsSkill.THIEVING,
                    amount = xp,
                    gpReward = gp,
                    logTitle = "$actionType: Google Lens ($pkg)",
                    logDesc = "Gained Thieving XP from Google Smart Lens scan!"
                )
            }
            pkgLower.contains("sleep") || pkgLower.contains("inactivity") -> {
                lastMediaXpTime[pkgLower] = now
                val xp = xpManager.getTaskXp("sleep", 1500L)
                val gp = xpManager.getTaskGp("sleep", 500L)
                repository.addXpToSkillDirect(
                    petTypeName = petTypeName,
                    skill = OsrsSkill.HITPOINTS,
                    amount = xp,
                    gpReward = gp,
                    logTitle = "$actionType: 7 Hours Sleep / Inactivity ($pkg)",
                    logDesc = "Gained Hitpoints XP and restored full health from 7+ hours of phone inactivity and sleep!"
                )
            }
            pkgLower.contains("messaging") || pkgLower.contains("sms") || pkgLower.contains("whatsapp") || pkgLower.contains("signal") || pkgLower.contains("telegram") || pkgLower.contains("messenger") || pkgLower.contains("chat") || pkgLower.contains("text") || pkgLower.contains("mms") -> {
                lastMediaXpTime[pkgLower] = now
                val xp = xpManager.getTaskXp("messaging", 350L)
                val gp = xpManager.getTaskGp("messaging", 100L)
                repository.addXpToSkillDirect(
                    petTypeName = petTypeName,
                    skill = OsrsSkill.DIVINATION,
                    amount = xp,
                    gpReward = gp,
                    logTitle = "$actionType: Text Message ($pkg)",
                    logDesc = "Harvested divine memory energy! Gained Divination XP from text message!"
                )
            }
            isCustomApp -> {
                lastMediaXpTime[pkgLower] = now
                val xp = (500L * xpManager.getXpMultiplier()).toLong()
                val gp = (150L * xpManager.getXpMultiplier()).toLong()
                repository.addXpToSkillDirect(
                    petTypeName = petTypeName,
                    skill = OsrsSkill.HERBLORE,
                    amount = xp,
                    gpReward = gp,
                    logTitle = "$actionType: Custom Tracked App ($pkg)",
                    logDesc = "Gained XP from custom tracked app notification!"
                )
            }
        }
    }

    private suspend fun updateWidget(context: Context, repository: PetRepository) {
        try {
            val petState = repository.getPetStateDirect() ?: return
            val widgetComponent = ComponentName(context, OsrsPetWidgetProvider::class.java)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)
            for (id in widgetIds) {
                OsrsPetWidgetProvider.updateAppWidget(
                    context = context,
                    appWidgetManager = appWidgetManager,
                    appWidgetId = id,
                    petName = petState.customName,
                    petIcon = petState.petType.iconSymbol,
                    hunger = petState.hunger,
                    happiness = petState.happiness,
                    petQuote = "\"${petState.currentQuote}\"",
                    pohRoomsCount = 1,
                    pohStatus = "🏡 POH Estate Ready"
                )
            }
        } catch (e: Exception) {
            // Widget not added or error
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    companion object {
        private val _notificationDismissedFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
        val notificationDismissedFlow = _notificationDismissedFlow.asSharedFlow()

        private val _appEventFlow = MutableSharedFlow<String>(extraBufferCapacity = 64)
        val appEventFlow = _appEventFlow.asSharedFlow()

        fun emitSimulatedAppEvent(pkgName: String, context: Context? = null) {
            _appEventFlow.tryEmit(pkgName)
            _notificationDismissedFlow.tryEmit(pkgName)
            if (context != null) {
                com.example.utils.PhoneContextHelper.updateLastActiveApp(context, pkgName)
            }
        }

        fun isNotificationServiceEnabled(context: Context): Boolean {
            val packageName = context.packageName
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            return flat != null && flat.contains(packageName)
        }
    }
}
