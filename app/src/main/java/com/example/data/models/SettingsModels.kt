package com.example.data.models

enum class SwipeSensitivity(
    val displayName: String,
    val thresholdDp: Float,
    val description: String,
    val iconEmoji: String
) {
    OFF(
        displayName = "Disabled",
        thresholdDp = 99999f,
        description = "Tab swipe navigation is turned off. Use the bottom navigation bar.",
        iconEmoji = "🚫"
    ),
    LOW(
        displayName = "Low",
        thresholdDp = 130f,
        description = "Firm, deliberate swipe required (~130dp). Reduces accidental tab switches.",
        iconEmoji = "🐢"
    ),
    MEDIUM(
        displayName = "Medium",
        thresholdDp = 65f,
        description = "Standard balanced swipe sensitivity (~65dp). Smooth and responsive.",
        iconEmoji = "⚖️"
    ),
    HIGH(
        displayName = "High",
        thresholdDp = 32f,
        description = "Quick, light touch swipe (~32dp). Rapid tab switching.",
        iconEmoji = "⚡"
    )
}
