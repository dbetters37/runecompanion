package com.example.data.ai

import com.example.data.db.MovementBehaviorEntity
import org.json.JSONObject

enum class ScreenZone(val displayName: String, val defaultXRatio: Float, val defaultYRatio: Float) {
    TOP_LEFT_WATCHTOWER("Top-Left Watchtower", 0.18f, 0.16f),
    HEADER_OVERLOOK("Header Overlook", 0.50f, 0.15f),
    BOTTOM_RIGHT_RESTING_CORNER("Bottom-Right Rest Corner", 0.82f, 0.82f),
    BOTTOM_LEFT_READING_NOOK("Bottom-Left Reading Nook", 0.18f, 0.82f),
    FOOTER_PERCH("Footer Perch", 0.50f, 0.84f),
    CENTER_SANCTUARY("Center Sanctuary", 0.50f, 0.50f)
}

data class ProcessedMovementResult(
    val updatedEntity: MovementBehaviorEntity,
    val isNewFavoriteUnlocked: Boolean,
    val zoneMovedTo: ScreenZone
)

class MovementBehaviorEngine {

    fun classifyZone(xRatio: Float, yRatio: Float): ScreenZone {
        val clampedX = xRatio.coerceIn(0f, 1f)
        val clampedY = yRatio.coerceIn(0f, 1f)

        return when {
            clampedY <= 0.35f && clampedX <= 0.40f -> ScreenZone.TOP_LEFT_WATCHTOWER
            clampedY <= 0.35f -> ScreenZone.HEADER_OVERLOOK
            clampedY >= 0.65f && clampedX >= 0.60f -> ScreenZone.BOTTOM_RIGHT_RESTING_CORNER
            clampedY >= 0.65f && clampedX <= 0.40f -> ScreenZone.BOTTOM_LEFT_READING_NOOK
            clampedY >= 0.65f -> ScreenZone.FOOTER_PERCH
            else -> ScreenZone.CENTER_SANCTUARY
        }
    }

    fun processMovement(
        current: MovementBehaviorEntity?,
        xPx: Float,
        yPx: Float,
        screenWidthPx: Float,
        screenHeightPx: Float
    ): ProcessedMovementResult {
        val entity = current ?: MovementBehaviorEntity()

        val safeW = screenWidthPx.coerceAtLeast(100f)
        val safeH = screenHeightPx.coerceAtLeast(100f)
        val xRatio = (xPx / safeW).coerceIn(0.05f, 0.95f)
        val yRatio = (yPx / safeH).coerceIn(0.05f, 0.95f)

        val zoneMovedTo = classifyZone(xRatio, yRatio)

        // Parse zone count JSON
        val zoneCountsMap = mutableMapOf<String, Int>()
        try {
            val json = JSONObject(entity.topZoneCountsJson)
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                zoneCountsMap[k] = json.optInt(k, 0)
            }
        } catch (_: Exception) { }

        val newCount = (zoneCountsMap[zoneMovedTo.displayName] ?: 0) + 1
        zoneCountsMap[zoneMovedTo.displayName] = newCount

        // Find new favorite zone
        val maxEntry = zoneCountsMap.maxByOrNull { it.value }
        val newFavZoneName = maxEntry?.key ?: zoneMovedTo.displayName
        val newFavZoneCount = maxEntry?.value ?: 1

        val isNewFavoriteUnlocked = newFavZoneName != entity.favoriteZone && newFavZoneCount >= 2

        // Determine coordinates for favorite zone
        val favZoneEnum = ScreenZone.values().find { it.displayName == newFavZoneName } ?: zoneMovedTo
        val favXRatio = if (newFavZoneName == zoneMovedTo.displayName) xRatio else favZoneEnum.defaultXRatio
        val favYRatio = if (newFavZoneName == zoneMovedTo.displayName) yRatio else favZoneEnum.defaultYRatio

        val totalDrags = entity.totalDrags + 1

        val enthusiasm = when {
            totalDrags < 3 -> "Observing Touch Habits"
            totalDrags in 3..7 -> "Active Stage Companion"
            else -> "Master Sanctuary Explorer"
        }

        val patternSentence = when {
            newFavZoneCount >= 3 ->
                "Consistently loves resting in ${newFavZoneName} (Chosen $newFavZoneCount times!). Uses this spot as primary sanctuary home base."
            newFavZoneCount == 2 ->
                "Developing a strong habit for ${newFavZoneName}. Frequently drifts or stays perched here."
            else ->
                "Exploring sanctuary locations. Currently investigating ${zoneMovedTo.displayName}."
        }

        // Serialize counts back to JSON
        val updatedJson = JSONObject().apply {
            zoneCountsMap.forEach { (k, v) -> put(k, v) }
        }.toString()

        val structuredState = SpatialPlacementState(
            isSpatialCommand = false,
            commandType = SpatialCommandType.DRAG_INTERACTION,
            targetZone = zoneMovedTo,
            normalizedX = xRatio,
            normalizedY = yRatio,
            spatialAnchor = when {
                yRatio <= 0.35f && xRatio >= 0.60f -> SpatialAnchor.TOP_END
                yRatio <= 0.35f && xRatio <= 0.40f -> SpatialAnchor.TOP_START
                yRatio <= 0.35f -> SpatialAnchor.TOP_CENTER
                yRatio >= 0.65f && xRatio >= 0.60f -> SpatialAnchor.BOTTOM_END
                yRatio >= 0.65f && xRatio <= 0.40f -> SpatialAnchor.BOTTOM_START
                yRatio >= 0.65f -> SpatialAnchor.BOTTOM_CENTER
                xRatio <= 0.40f -> SpatialAnchor.CENTER_START
                xRatio >= 0.60f -> SpatialAnchor.CENTER_END
                else -> SpatialAnchor.CENTER
            },
            originalCommandText = "Manual Drag",
            interpretedRationale = "Tactile screen placement to ${zoneMovedTo.displayName} ($xRatio, $yRatio)",
            confidenceScore = 1.0f,
            animationDurationMs = 400,
            speechFeedback = "Happily moved to ${zoneMovedTo.displayName}!",
            timestamp = System.currentTimeMillis()
        )

        val updatedEntity = entity.copy(
            favoriteZone = newFavZoneName,
            learnedPattern = patternSentence,
            totalDrags = totalDrags,
            favoriteZoneCount = newFavFavZoneCountOrNew(newFavZoneCount),
            lastZoneMovedTo = zoneMovedTo.displayName,
            activeZone = zoneMovedTo.displayName,
            favoriteXRatio = favXRatio,
            favoriteYRatio = favYRatio,
            currentXRatio = xRatio,
            currentYRatio = yRatio,
            spatialAnchor = structuredState.spatialAnchor.displayName,
            lastSpatialCommand = "Manual Drag",
            lastSpatialRationale = structuredState.interpretedRationale,
            lastSpatialCommandType = "DRAG_INTERACTION",
            spatialConfidenceScore = 1.0f,
            structuredStateJson = structuredState.toJson(),
            dragEnthusiasm = enthusiasm,
            topZoneCountsJson = updatedJson,
            timestamp = System.currentTimeMillis()
        )

        return ProcessedMovementResult(
            updatedEntity = updatedEntity,
            isNewFavoriteUnlocked = isNewFavoriteUnlocked,
            zoneMovedTo = zoneMovedTo
        )
    }

    private fun newFavFavZoneCountOrNew(count: Int): Int = count
}
