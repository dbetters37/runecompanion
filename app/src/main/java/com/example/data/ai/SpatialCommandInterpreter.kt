package com.example.data.ai

import org.json.JSONObject

enum class SpatialCommandType(val label: String) {
    ABSOLUTE_ZONE("Absolute Zone Placement"),
    RELATIVE_SHIFT("Relative Vector Shift"),
    ANCHOR_DOCK("Dock Anchor Alignment"),
    RESET_CENTER("Sanctuary Center Reset"),
    DRAG_INTERACTION("Manual Screen Placement")
}

enum class SpatialAnchor(val alignmentCode: String, val displayName: String) {
    TOP_START("TopStart", "Top-Left (Watchtower)"),
    TOP_CENTER("TopCenter", "Top-Center (Header Overlook)"),
    TOP_END("TopEnd", "Top-Right (Header Corner)"),
    CENTER_START("CenterStart", "Center-Left (Reading Nook)"),
    CENTER("Center", "Center Sanctuary"),
    CENTER_END("CenterEnd", "Center-Right (Side Perch)"),
    BOTTOM_START("BottomStart", "Bottom-Left (Rest Nook)"),
    BOTTOM_CENTER("BottomCenter", "Bottom-Center (Footer Perch)"),
    BOTTOM_END("BottomEnd", "Bottom-Right (Resting Corner)")
}

data class SpatialPlacementState(
    val isSpatialCommand: Boolean = false,
    val commandType: SpatialCommandType = SpatialCommandType.RESET_CENTER,
    val targetZone: ScreenZone = ScreenZone.CENTER_SANCTUARY,
    val normalizedX: Float = 0.50f, // 0.0 to 1.0
    val normalizedY: Float = 0.50f, // 0.0 to 1.0
    val spatialAnchor: SpatialAnchor = SpatialAnchor.CENTER,
    val originalCommandText: String = "",
    val interpretedRationale: String = "Sanctuary baseline positioning",
    val confidenceScore: Float = 1.0f,
    val animationDurationMs: Int = 850,
    val springStiffness: Float = 320f,
    val springDamping: Float = 0.78f,
    val speechFeedback: String = "Resting peacefully in the center sanctuary ✨",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJson(): String {
        return JSONObject().apply {
            put("isSpatialCommand", isSpatialCommand)
            put("commandType", commandType.name)
            put("commandTypeLabel", commandType.label)
            put("targetZone", targetZone.name)
            put("zoneDisplayName", targetZone.displayName)
            put("normalizedX", normalizedX.toDouble())
            put("normalizedY", normalizedY.toDouble())
            put("spatialAnchor", spatialAnchor.name)
            put("alignmentCode", spatialAnchor.alignmentCode)
            put("anchorDisplayName", spatialAnchor.displayName)
            put("originalCommandText", originalCommandText)
            put("interpretedRationale", interpretedRationale)
            put("confidenceScore", confidenceScore.toDouble())
            put("animationDurationMs", animationDurationMs)
            put("springStiffness", springStiffness.toDouble())
            put("springDamping", springDamping.toDouble())
            put("speechFeedback", speechFeedback)
            put("timestamp", timestamp)
        }.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): SpatialPlacementState {
            if (jsonStr.isBlank() || !jsonStr.trim().startsWith("{")) {
                return SpatialPlacementState()
            }
            return try {
                val obj = JSONObject(jsonStr)
                val isCmd = obj.optBoolean("isSpatialCommand", false)
                val typeName = obj.optString("commandType", SpatialCommandType.RESET_CENTER.name)
                val cmdType = try { SpatialCommandType.valueOf(typeName) } catch (_: Exception) { SpatialCommandType.RESET_CENTER }
                val zoneName = obj.optString("targetZone", ScreenZone.CENTER_SANCTUARY.name)
                val targetZone = try { ScreenZone.valueOf(zoneName) } catch (_: Exception) { ScreenZone.CENTER_SANCTUARY }
                val normX = obj.optDouble("normalizedX", 0.50).toFloat()
                val normY = obj.optDouble("normalizedY", 0.50).toFloat()
                val anchorName = obj.optString("spatialAnchor", SpatialAnchor.CENTER.name)
                val anchor = try { SpatialAnchor.valueOf(anchorName) } catch (_: Exception) { SpatialAnchor.CENTER }
                val originalCmd = obj.optString("originalCommandText", "")
                val rationale = obj.optString("interpretedRationale", "")
                val conf = obj.optDouble("confidenceScore", 1.0).toFloat()
                val animMs = obj.optInt("animationDurationMs", 850)
                val stiffness = obj.optDouble("springStiffness", 320.0).toFloat()
                val damping = obj.optDouble("springDamping", 0.78).toFloat()
                val speech = obj.optString("speechFeedback", "")
                val ts = obj.optLong("timestamp", System.currentTimeMillis())

                SpatialPlacementState(
                    isSpatialCommand = isCmd,
                    commandType = cmdType,
                    targetZone = targetZone,
                    normalizedX = normX,
                    normalizedY = normY,
                    spatialAnchor = anchor,
                    originalCommandText = originalCmd,
                    interpretedRationale = rationale,
                    confidenceScore = conf,
                    animationDurationMs = animMs,
                    springStiffness = stiffness,
                    springDamping = damping,
                    speechFeedback = speech,
                    timestamp = ts
                )
            } catch (_: Exception) {
                SpatialPlacementState()
            }
        }
    }
}

class SpatialCommandInterpreter {

    private val movementEngine = MovementBehaviorEngine()

    /**
     * Inspects natural language text and determines if it represents a spatial placement command
     * like "move", "shift", "reposition", "dock", "glide", "perch", "drift", "align", etc.
     * Returns structured state data specifying target coordinates, ScreenZone, anchor, duration, and rationale.
     */
    fun interpret(
        input: String,
        currentX: Float = 0.50f,
        currentY: Float = 0.50f,
        petName: String = "Aura"
    ): SpatialPlacementState {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        // 1. Spatial trigger verbs check
        val spatialVerbs = listOf(
            "move", "shift", "reposition", "relocate", "glide", "perch",
            "drift", "hop", "slide", "nudge", "place", "put", "step",
            "teleport", "fly", "dock", "anchor", "go to", "head to",
            "float to", "stay at", "hang out in", "come over to", "settle in",
            "recenter", "center yourself", "reset position"
        )

        val hasSpatialVerb = spatialVerbs.any { verb ->
            lower.contains(Regex("\\b$verb\\b"))
        }

        val spatialDirectionWords = listOf(
            "top right", "top left", "top", "upper right", "upper left", "upper",
            "bottom right", "bottom left", "bottom", "lower right", "lower left", "lower",
            "left", "right", "up", "down", "center", "middle", "sanctuary",
            "watchtower", "desk perch", "reading nook", "rest corner", "overlook", "footer"
        )

        val hasDirectionWord = spatialDirectionWords.any { dir ->
            lower.contains(dir)
        }

        // If no spatial cues exist, return non-command state
        if (!hasSpatialVerb && !hasDirectionWord && !lower.startsWith("shift") && !lower.startsWith("move")) {
            return SpatialPlacementState(
                isSpatialCommand = false,
                normalizedX = currentX,
                normalizedY = currentY,
                targetZone = movementEngine.classifyZone(currentX, currentY),
                originalCommandText = trimmed
            )
        }

        // 2. Parse Absolute Zones
        val (absoluteZone, anchor, absX, absY, isAbsoluteMatch) = matchAbsoluteZone(lower)

        if (isAbsoluteMatch && absoluteZone != null) {
            val speech = when (absoluteZone) {
                ScreenZone.TOP_LEFT_WATCHTOWER -> "*ascends gracefully into the Top-Left Watchtower* Keeping a watchful spiritual eye from the high watchtower."
                ScreenZone.HEADER_OVERLOOK -> "*floats up to the Header Overlook* Gazing peacefully over our sacred sanctuary."
                ScreenZone.BOTTOM_RIGHT_RESTING_CORNER -> "*descends gently into the Bottom-Right Rest Corner* Settling into my cozy resting corner."
                ScreenZone.BOTTOM_LEFT_READING_NOOK -> "*drifts softly to the Bottom-Left Reading Nook* Curled up in the reading nook, ready to absorb thoughts."
                ScreenZone.FOOTER_PERCH -> "*steps down to the Footer Perch* Resting along the sanctuary base."
                ScreenZone.CENTER_SANCTUARY -> "*returns to the heart of the sanctuary* Centered and attuned to your inner frequency ✨"
            }

            return SpatialPlacementState(
                isSpatialCommand = true,
                commandType = if (lower.contains("dock") || lower.contains("anchor")) SpatialCommandType.ANCHOR_DOCK else SpatialCommandType.ABSOLUTE_ZONE,
                targetZone = absoluteZone,
                normalizedX = absX,
                normalizedY = absY,
                spatialAnchor = anchor,
                originalCommandText = trimmed,
                interpretedRationale = "Interpreted absolute spatial command '$trimmed' -> Directing UI placement to ${absoluteZone.displayName} ($absX, $absY)",
                confidenceScore = 0.98f,
                animationDurationMs = 850,
                springStiffness = 340f,
                springDamping = 0.76f,
                speechFeedback = speech,
                timestamp = System.currentTimeMillis()
            )
        }

        // 3. Parse Relative Vector Shifts (e.g., "shift left", "move up", "nudge right", "move down 20%")
        val relativeState = parseRelativeShift(lower, currentX, currentY, trimmed)
        if (relativeState != null) {
            return relativeState
        }

        // 4. Center / Reset fallback if spatial command was intent
        if (lower.contains("center") || lower.contains("reset") || lower.contains("home") || lower.contains("middle")) {
            return SpatialPlacementState(
                isSpatialCommand = true,
                commandType = SpatialCommandType.RESET_CENTER,
                targetZone = ScreenZone.CENTER_SANCTUARY,
                normalizedX = 0.50f,
                normalizedY = 0.50f,
                spatialAnchor = SpatialAnchor.CENTER,
                originalCommandText = trimmed,
                interpretedRationale = "Interpreted sanctuary recenter command '$trimmed' -> Returning UI placement to center (0.50, 0.50)",
                confidenceScore = 0.95f,
                animationDurationMs = 800,
                speechFeedback = "*aligns into harmonic center* Re-centered in the heart of our sanctuary 🌿",
                timestamp = System.currentTimeMillis()
            )
        }

        // Fallback spatial interpretation
        return SpatialPlacementState(
            isSpatialCommand = true,
            commandType = SpatialCommandType.ABSOLUTE_ZONE,
            targetZone = movementEngine.classifyZone(currentX, currentY),
            normalizedX = currentX,
            normalizedY = currentY,
            spatialAnchor = resolveAnchorFromRatios(currentX, currentY),
            originalCommandText = trimmed,
            interpretedRationale = "Interpreted general spatial intent '$trimmed' -> Maintaining attuned placement at ($currentX, $currentY)",
            confidenceScore = 0.75f,
            animationDurationMs = 700,
            speechFeedback = "*adjusts stance subtly* Responding to your spatial guidance!",
            timestamp = System.currentTimeMillis()
        )
    }

    private data class AbsoluteZoneMatch(
        val zone: ScreenZone?,
        val anchor: SpatialAnchor,
        val x: Float,
        val y: Float,
        val matched: Boolean
    )

    private fun matchAbsoluteZone(lower: String): AbsoluteZoneMatch {
        return when {
            // Top Left / Watchtower
            lower.contains("top left") || lower.contains("upper left") || lower.contains("top-left") ||
                    lower.contains("watchtower") || lower.contains("watch tower") || lower.contains("top left corner") -> {
                AbsoluteZoneMatch(ScreenZone.TOP_LEFT_WATCHTOWER, SpatialAnchor.TOP_START, 0.18f, 0.16f, true)
            }
            // Header Overlook / Top Center / Upper Area (excluding top-right desk perch which was removed)
            lower.contains("header") || lower.contains("overlook") || lower.contains("top center") ||
                    lower.contains("top right") || lower.contains("upper right") || lower.contains("top") -> {
                AbsoluteZoneMatch(ScreenZone.HEADER_OVERLOOK, SpatialAnchor.TOP_CENTER, 0.50f, 0.15f, true)
            }
            // Bottom Right / Resting Corner
            lower.contains("bottom right") || lower.contains("lower right") || lower.contains("bottom-right") ||
                    lower.contains("rest corner") || lower.contains("resting corner") || lower.contains("bottom right corner") -> {
                AbsoluteZoneMatch(ScreenZone.BOTTOM_RIGHT_RESTING_CORNER, SpatialAnchor.BOTTOM_END, 0.82f, 0.82f, true)
            }
            // Bottom Left / Reading Nook
            lower.contains("bottom left") || lower.contains("lower left") || lower.contains("bottom-left") ||
                    lower.contains("reading nook") || lower.contains("nook") || lower.contains("bottom left corner") -> {
                AbsoluteZoneMatch(ScreenZone.BOTTOM_LEFT_READING_NOOK, SpatialAnchor.BOTTOM_START, 0.18f, 0.82f, true)
            }
            // Footer Perch / Bottom Center
            lower.contains("footer") || lower.contains("footer perch") || lower.contains("bottom center") ||
                    (lower.contains("bottom") && !lower.contains("left") && !lower.contains("right") && !lower.contains("top")) -> {
                AbsoluteZoneMatch(ScreenZone.FOOTER_PERCH, SpatialAnchor.BOTTOM_CENTER, 0.50f, 0.84f, true)
            }
            // Center Sanctuary
            lower.contains("center") || lower.contains("sanctuary") || lower.contains("middle") || lower.contains("recenter") -> {
                AbsoluteZoneMatch(ScreenZone.CENTER_SANCTUARY, SpatialAnchor.CENTER, 0.50f, 0.50f, true)
            }
            else -> AbsoluteZoneMatch(null, SpatialAnchor.CENTER, 0.5f, 0.5f, false)
        }
    }

    private fun parseRelativeShift(
        lower: String,
        currentX: Float,
        currentY: Float,
        originalText: String
    ): SpatialPlacementState? {
        var deltaX = 0f
        var deltaY = 0f
        var recognized = false
        val shiftDescriptions = mutableListOf<String>()

        // Check percentage or custom delta
        val percentMatch = Regex("(\\d+)\\s*%").find(lower)
        val customPercent = percentMatch?.groupValues?.get(1)?.toFloatOrNull()?.let { it / 100f }

        val defaultDeltaX = customPercent ?: 0.25f
        val defaultDeltaY = customPercent ?: 0.22f

        // Horizontal shifts
        if (lower.contains("left") || lower.contains("west") || lower.contains("port")) {
            deltaX -= defaultDeltaX
            recognized = true
            shiftDescriptions.add("shifting left by ${(defaultDeltaX * 100).toInt()}%")
        } else if (lower.contains("right") || lower.contains("east") || lower.contains("starboard")) {
            deltaX += defaultDeltaX
            recognized = true
            shiftDescriptions.add("shifting right by ${(defaultDeltaX * 100).toInt()}%")
        }

        // Vertical shifts
        if (lower.contains("up") || lower.contains("higher") || lower.contains("north") || lower.contains("above") || lower.contains("upward")) {
            deltaY -= defaultDeltaY
            recognized = true
            shiftDescriptions.add("shifting upward by ${(defaultDeltaY * 100).toInt()}%")
        } else if (lower.contains("down") || lower.contains("lower") || lower.contains("south") || lower.contains("below") || lower.contains("downward")) {
            deltaY += defaultDeltaY
            recognized = true
            shiftDescriptions.add("shifting downward by ${(defaultDeltaY * 100).toInt()}%")
        }

        if (!recognized) return null

        val targetX = (currentX + deltaX).coerceIn(0.12f, 0.88f)
        val targetY = (currentY + deltaY).coerceIn(0.14f, 0.86f)
        val targetZone = movementEngine.classifyZone(targetX, targetY)
        val anchor = resolveAnchorFromRatios(targetX, targetY)

        val rationale = "Interpreted relative shift '${originalText}' -> ${shiftDescriptions.joinToString(", ")}. New target: ($targetX, $targetY) in ${targetZone.displayName}"
        val speech = "*shifts smoothly through the sanctuary air* Shifting position as requested! Now perching in ${targetZone.displayName} 🍃"

        return SpatialPlacementState(
            isSpatialCommand = true,
            commandType = SpatialCommandType.RELATIVE_SHIFT,
            targetZone = targetZone,
            normalizedX = targetX,
            normalizedY = targetY,
            spatialAnchor = anchor,
            originalCommandText = originalText,
            interpretedRationale = rationale,
            confidenceScore = 0.94f,
            animationDurationMs = 750,
            springStiffness = 350f,
            springDamping = 0.80f,
            speechFeedback = speech,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun resolveAnchorFromRatios(xRatio: Float, yRatio: Float): SpatialAnchor {
        val isTop = yRatio <= 0.35f
        val isBottom = yRatio >= 0.65f
        val isLeft = xRatio <= 0.35f
        val isRight = xRatio >= 0.65f

        return when {
            isTop && isLeft -> SpatialAnchor.TOP_START
            isTop && isRight -> SpatialAnchor.TOP_END
            isTop -> SpatialAnchor.TOP_CENTER
            isBottom && isLeft -> SpatialAnchor.BOTTOM_START
            isBottom && isRight -> SpatialAnchor.BOTTOM_END
            isBottom -> SpatialAnchor.BOTTOM_CENTER
            isLeft -> SpatialAnchor.CENTER_START
            isRight -> SpatialAnchor.CENTER_END
            else -> SpatialAnchor.CENTER
        }
    }
}
