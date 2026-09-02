package com.example.data.models

data class OfflineHarvestedItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val iconEmoji: String = "📦"
)

data class GolemOfflineGains(
    val golemName: String,
    val golemEmoji: String = "🗿",
    val activityName: String,
    val activityEmoji: String = "⚡",
    val skillName: String? = null,
    val actionsCompleted: Int,
    val xpGained: Long,
    val gpGained: Long,
    val isCompleted: Boolean = false,
    val itemsGained: List<OfflineHarvestedItem> = emptyList()
)

data class OfflineGainsReport(
    val activityName: String,
    val activityEmoji: String = "⚔️",
    val skill: OsrsSkill? = null,
    val elapsedMillis: Long,
    val actionsCompleted: Int,
    val xpGained: Long,
    val gpGained: Long = 0L,
    val hungerStart: Int,
    val hungerUsed: Int,
    val hungerRemaining: Int,
    val maxHunger: Int = 100,
    val itemsGained: List<OfflineHarvestedItem> = emptyList(),
    val extraBonusMaterialsGained: List<OfflineHarvestedItem> = emptyList(),
    val extraNpcBonusProcCount: Int = 0,
    val stoppedReason: String? = null,
    val golemGains: GolemOfflineGains? = null
) {
    val formattedDuration: String
        get() {
            val hours = elapsedMillis / 3600000L
            val mins = (elapsedMillis % 3600000L) / 60000L
            val secs = (elapsedMillis % 60000L) / 1000L
            return when {
                hours > 0 -> "${hours}h ${mins}m"
                mins > 0 -> "${mins}m ${secs}s"
                else -> "${secs}s"
            }
        }
}
