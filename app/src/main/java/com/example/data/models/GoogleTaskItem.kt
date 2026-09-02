package com.example.data.models

data class GoogleTaskItem(
    val id: String,
    val title: String,
    val notes: String? = null,
    val status: String = "needsAction", // "needsAction" or "completed"
    val createdTimestampMs: Long = System.currentTimeMillis(),
    val completedTimestampMs: Long? = null,
    val due: String? = null,
    val listId: String = "@default"
) {
    val isCompleted: Boolean get() = status == "completed"

    /**
     * Calculates task age in days (from createdTimestampMs to now).
     */
    fun getAgeInDays(nowMs: Long = System.currentTimeMillis()): Double {
        val diffMs = (nowMs - createdTimestampMs).coerceAtLeast(0L)
        return diffMs.toDouble() / (1000.0 * 60.0 * 60.0 * 24.0)
    }

    /**
     * Calculates Mining XP reward based on task age:
     * Base XP = 200 XP
     * +150 XP for each day task was pending (up to 3,000 XP max)!
     */
    fun getMiningXpReward(nowMs: Long = System.currentTimeMillis()): Long {
        val days = getAgeInDays(nowMs)
        val bonusXp = (days * 150.0).toLong()
        return (200L + bonusXp).coerceAtMost(3000L)
    }

    /**
     * Calculates GP reward based on task age:
     * Base GP = 50 GP + 50 GP per day pending (up to 1,000 GP)
     */
    fun getGpReward(nowMs: Long = System.currentTimeMillis()): Long {
        val days = getAgeInDays(nowMs)
        val bonusGp = (days * 50.0).toLong()
        return (50L + bonusGp).coerceAtMost(1000L)
    }

    /**
     * Determines Ore Reward based on task age!
     * The longer the task has been pending, the rarer and more valuable the ore!
     */
    fun getOreReward(nowMs: Long = System.currentTimeMillis()): GoogleTaskOreReward {
        val days = getAgeInDays(nowMs)
        return when {
            days >= 7.0 -> GoogleTaskOreReward(
                oreId = "item_runite_ore",
                oreName = "Runite Ore 💙",
                oreEmoji = "💙",
                quantity = ((days / 2.0).toInt().coerceIn(2, 6)),
                rarityLabel = "ANCIENT TASK REWARD (7+ Days Pending)"
            )
            days >= 3.0 -> GoogleTaskOreReward(
                oreId = "item_adamant_ore",
                oreName = "Adamantite Ore 🟢",
                oreEmoji = "🟢",
                quantity = ((days).toInt().coerceIn(2, 5)),
                rarityLabel = "AGED TASK REWARD (3-7 Days Pending)"
            )
            days >= 1.0 -> GoogleTaskOreReward(
                oreId = "item_mithril_ore",
                oreName = "Mithril Ore 🔷",
                oreEmoji = "🔷",
                quantity = ((days * 1.5).toInt().coerceIn(2, 4)),
                rarityLabel = "MATURE TASK REWARD (1-3 Days Pending)"
            )
            else -> GoogleTaskOreReward(
                oreId = "item_iron_ore",
                oreName = "Iron Ore 🪨",
                oreEmoji = "🪨",
                quantity = 2,
                rarityLabel = "FRESH TASK REWARD (<1 Day Pending)"
            )
        }
    }
}

data class GoogleTaskOreReward(
    val oreId: String,
    val oreName: String,
    val oreEmoji: String,
    val quantity: Int,
    val rarityLabel: String
)
