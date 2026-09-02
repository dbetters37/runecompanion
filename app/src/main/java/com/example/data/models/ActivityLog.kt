package com.example.data.models

data class ActivityLog(
    val id: Long = 0,
    val title: String,
    val description: String,
    val skill: OsrsSkill,
    val xpGained: Long,
    val coinsGained: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiQuest(
    val id: String,
    val title: String,
    val description: String,
    val realLifeTaskInstructions: String,
    val targetSkill: OsrsSkill,
    val rewardXp: Long,
    val rewardGp: Long,
    val isCompleted: Boolean = false,
    val targetPackageKeyword: String = "",
    val triggerCount: Int = 0,
    val targetTriggerCount: Int = 1,
    val isAutoPhoneTriggered: Boolean = false
)
