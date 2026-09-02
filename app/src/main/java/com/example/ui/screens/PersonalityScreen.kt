package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.PersonalityEngine
import com.example.data.db.PersonalityEntity
import com.example.data.db.PersonalityLogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun PersonalityScreen(
    personality: PersonalityEntity?,
    personalityLogs: List<PersonalityLogEntity>,
    latestPetMessage: String? = null,
    isGenerating: Boolean = false,
    onRenamePet: (String) -> Unit,
    onSelectArchetype: (String) -> Unit,
    onUpdateDirectives: (String) -> Unit,
    onUpdateTraits: (
        warmth: Float,
        openness: Float,
        mysticism: Float,
        playfulness: Float,
        energy: Float,
        humor: Float,
        empathy: Float,
        creativity: Float
    ) -> Unit,
    onTriggerReflection: () -> Unit,
    onTestCustomResponse: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showRenameDialog by remember { mutableStateOf(false) }
    var newPetNameInput by remember { mutableStateOf(personality?.petName ?: "Aura") }
    var customDirectivesInput by remember(personality?.customDirectives) { mutableStateOf(personality?.customDirectives ?: "") }
    var isDirectivesSavedMessage by remember { mutableStateOf(false) }

    // Trait Sliders Local State
    var warmthVal by remember(personality) { mutableStateOf(personality?.warmth ?: 0.6f) }
    var opennessVal by remember(personality) { mutableStateOf(personality?.openness ?: 0.5f) }
    var mysticismVal by remember(personality) { mutableStateOf(personality?.mysticism ?: 0.85f) }
    var playfulnessVal by remember(personality) { mutableStateOf(personality?.playfulness ?: 0.5f) }
    var energyVal by remember(personality) { mutableStateOf(personality?.energy ?: 0.75f) }
    var humorVal by remember(personality) { mutableStateOf(personality?.humorLevel ?: 0.5f) }
    var empathyVal by remember(personality) { mutableStateOf(personality?.empathyLevel ?: 0.8f) }
    var creativityVal by remember(personality) { mutableStateOf(personality?.creativityLevel ?: 0.7f) }

    // Test Bench Input
    var testPromptInput by remember { mutableStateOf("Tell me a custom quote about us!") }
    var testResponseResult by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val currentArchetype = PersonalityEngine.getArchetypeById(personality?.archetype ?: "Shaman Guardian")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07140B))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PERSONALITY ENGINE STUDIO",
                    color = Color(0xFFFFD166),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gemini AI Personas, Trait Matrices & Custom Prompt Directives",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = { showRenameDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Rename",
                    tint = Color(0xFF00F5D4)
                )
            }
        }

        // Shaman Profile Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF7209B7), RoundedCornerShape(20.dp)),
            color = Color(0xFF1E0A38)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Pet Icon",
                        tint = Color(0xFFFFD166),
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = personality?.petName ?: "Aura",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentArchetype.name} • ${personality?.evolutionStage ?: "Wise Shaman"}",
                            color = Color(0xFF00F5D4),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val xpInCurrentLevel = (personality?.xp ?: 0) % 100
                val progress = xpInCurrentLevel / 100f

                Text(
                    text = "Level ${personality?.level ?: 1} (${xpInCurrentLevel}/100 XP to next stage)",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF00F5D4),
                    trackColor = Color(0xFF3B1566)
                )
            }
        }

        // Psychological Preferences Summary Card (Likes, Dislikes & Fears)
        val preferences = remember(personality) {
            PersonalityEngine.getDynamicPetPreferences(personality, personality?.activeSkin ?: "SHAMAN_DEFAULT")
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF381A66), RoundedCornerShape(16.dp)),
            color = Color(0xFF16032B)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PSYCHOLOGICAL INCLINATIONS",
                        color = Color(0xFF00F5D4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Live Sync",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Likes row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("❤️ Likes: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00F5D4))
                    Text(
                        text = preferences.likes.take(3).joinToString(" • ") { it.title },
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Dislikes row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💔 Dislikes: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB703))
                    Text(
                        text = preferences.dislikes.take(2).joinToString(" • ") { it.title },
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Fears row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("😱 Fears: ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF4D6D))
                    Text(
                        text = preferences.fears.take(2).joinToString(" • ") { it.title },
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // 1. AI PERSONA ARCHETYPE SELECTOR CAROUSEL
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "SELECT AI PERSONA ARCHETYPE",
                color = Color(0xFFFFB703),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(PersonalityEngine.ARCHETYPES) { archetype ->
                    val isSelected = currentArchetype.id == archetype.id
                    Surface(
                        modifier = Modifier
                            .width(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectArchetype(archetype.id) }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF00F5D4) else Color(0xFF3D166E),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        color = if (isSelected) Color(0xFF2C1154) else Color(0xFF1B0B33)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = archetype.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = Color(0xFF00F5D4),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = archetype.title,
                                color = Color(0xFFFFD166),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = archetype.description,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }

        // 2. CUSTOM SYSTEM DIRECTIVES & PROMPT RULES EDITOR
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF70D6FF), RoundedCornerShape(16.dp)),
            color = Color(0xFF160A2C)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Directives",
                        tint = Color(0xFF70D6FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "CUSTOM GEMINI AI PROMPT DIRECTIVES & VOICE MODIFIER",
                        color = Color(0xFFFFD166),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Set strict mandates for how your pet speaks and behaves in every response (e.g. 'Speak in rhymes', 'Talk like a pirate', 'Be witty yet caring'):",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // One-Tap Voice Presets
                Text(
                    text = "QUICK VOICE PRESETS (Tap to load):",
                    color = Color(0xFF00F5D4),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                val presets = listOf(
                    "🏴‍☠️ Pirate" to "Speak like a jolly pirate captain! Use 'Ahoy', 'me hearty', 'shiver me timbers', and nautical terms in every reply.",
                    "🌌 Cosmic Poet" to "Speak in rhyming cosmic poetry with starlight metaphors, gentle wisdom, and soft rhyming couplets.",
                    "🐸 Frog" to "End every single sentence with *ribbit!* and make references to lily pads, bugs, and peaceful ponds.",
                    "💅 Sassy Bestie" to "Be witty, hilarious, extra sassy, and use fun modern slang while caring deeply for your human bestie!",
                    "🤠 Space Cowboy" to "Speak like a cosmic space cowboy! Use 'Howdy partner', 'reckon', and starry trail metaphors.",
                    "🤖 Cyberpunk" to "Speak like a neon cyberpunk AI netrunner! Use tech jargon, matrix references, and energetic style.",
                    "🦉 Zen Master" to "Speak as an ancient serene Zen master. Offer brief, profound riddles and gentle mindfulness prompts."
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { (title, promptText) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF281347),
                            border = BorderStroke(0.6.dp, Color(0xFF70D6FF)),
                            modifier = Modifier.clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                customDirectivesInput = promptText
                                isDirectivesSavedMessage = false
                            }
                        ) {
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customDirectivesInput,
                    onValueChange = {
                        customDirectivesInput = it
                        isDirectivesSavedMessage = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    placeholder = {
                        Text(
                            text = "e.g. Always speak with cosmic poetry and encourage me gently...",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00F5D4),
                        unfocusedBorderColor = Color(0xFF381A66),
                        focusedContainerColor = Color(0xFF1E0A38),
                        unfocusedContainerColor = Color(0xFF1E0A38)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Active Directives Status Badge
                if (!personality?.customDirectives.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0D2818),
                        border = BorderStroke(0.6.dp, Color(0xFF00F5D4)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🟢 ACTIVE IN GEMINI ENGINE:",
                                color = Color(0xFF00F5D4),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "\"${personality?.customDirectives}\"",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (customDirectivesInput.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                customDirectivesInput = ""
                                onUpdateDirectives("")
                                isDirectivesSavedMessage = true
                            },
                            border = BorderStroke(0.6.dp, Color(0xFFFF80AB)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Clear", fontSize = 10.sp, color = Color(0xFFFF80AB))
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onUpdateDirectives(customDirectivesInput)
                            isDirectivesSavedMessage = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B6)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Apply Directives", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onUpdateDirectives(customDirectivesInput)
                            isDirectivesSavedMessage = true
                            val prompt = if (customDirectivesInput.isNotBlank()) {
                                "Hello! Please introduce yourself using your new active voice directive: '$customDirectivesInput'"
                            } else {
                                "Hello! Speak to me in your natural companion voice!"
                            }
                            onTestCustomResponse(prompt)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7209B7)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Apply & Test Live ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD166))
                    }
                }

                if (isDirectivesSavedMessage) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✓ Directives saved and active in AI companion engine!",
                        color = Color(0xFF00F5D4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. FINE-TUNE TRAIT SLIDERS MATRIX
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFFF70A6), RoundedCornerShape(16.dp)),
            color = Color(0xFF1E0A38)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Traits",
                        tint = Color(0xFFFF70A6),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "PERSONALITY TRAIT MATRIX TUNER",
                        color = Color(0xFFFFD166),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                InteractiveTraitSlider("Warmth & Affection", warmthVal, Color(0xFFFF70A6)) { warmthVal = it }
                InteractiveTraitSlider("Mysticism & Intuition", mysticismVal, Color(0xFF00F5D4)) { mysticismVal = it }
                InteractiveTraitSlider("Playfulness & Wit", playfulnessVal, Color(0xFFFFD166)) { playfulnessVal = it }
                InteractiveTraitSlider("Humor & Sarcasm Level", humorVal, Color(0xFFFF9E00)) { humorVal = it }
                InteractiveTraitSlider("Empathy & Active Listening", empathyVal, Color(0xFF70D6FF)) { empathyVal = it }
                InteractiveTraitSlider("Creativity & Abstraction", creativityVal, Color(0xFFC77DFF)) { creativityVal = it }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        onUpdateTraits(
                            warmthVal, opennessVal, mysticismVal, playfulnessVal,
                            energyVal, humorVal, empathyVal, creativityVal
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7209B7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Save Custom Trait Matrix",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 4. INTERACTIVE CUSTOM RESPONSE TEST BENCH
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFFFD166), RoundedCornerShape(16.dp)),
            color = Color(0xFF220C42)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Test",
                        tint = Color(0xFFFFD166),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "CUSTOM VOICE & DIRECTIVE TEST BENCH",
                        color = Color(0xFFFFD166),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Send a test message to your AI Companion to verify your active personality traits and voice directives in real-time:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = testPromptInput,
                    onValueChange = { testPromptInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD166),
                        unfocusedBorderColor = Color(0xFF381A66),
                        focusedContainerColor = Color(0xFF14072B),
                        unfocusedContainerColor = Color(0xFF14072B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        testResponseResult = null
                        onTestCustomResponse(testPromptInput)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B1566)),
                    border = BorderStroke(1.dp, Color(0xFFFFD166)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = Color(0xFFFFD166),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing Voice Directive...", fontSize = 11.sp, color = Color(0xFFFFD166))
                    } else {
                        Text("Send Test Prompt ⚡", fontSize = 11.sp, color = Color(0xFFFFD166), fontWeight = FontWeight.Bold)
                    }
                }

                val displayResponse = latestPetMessage ?: testResponseResult
                if (!displayResponse.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF100424),
                        border = BorderStroke(0.6.dp, Color(0xFF00F5D4)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "✨ LIVE AI RESPONSE WITH ACTIVE VOICE:",
                                    color = Color(0xFF00F5D4),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = displayResponse,
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // 5. DEEP MEMORY REFLECTION BUTTON
        Button(
            onClick = onTriggerReflection,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A189A)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reflect",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF00F5D4)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Perform Deep Memory Reflection & Synthesis",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 6. PERSONALITY LOGS TIMELINE
        Text(
            text = "PERSONALITY SHIFT HISTORY LOG",
            color = Color(0xFFFFB703),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        if (personalityLogs.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1B0B33)
            ) {
                Text(
                    text = "No personality shifts logged yet. Interactions will dynamically build history!",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                personalityLogs.take(8).forEach { log ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1B0B33),
                        border = BorderStroke(0.5.dp, Color(0xFF3F1F70))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${log.eventType}",
                                    color = Color(0xFF00F5D4),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = log.description,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Your Lifelong Pet") },
            text = {
                OutlinedTextField(
                    value = newPetNameInput,
                    onValueChange = { newPetNameInput = it },
                    label = { Text("Pet Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPetNameInput.isNotBlank()) {
                        onRenamePet(newPetNameInput)
                    }
                    showRenameDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InteractiveTraitSlider(
    name: String,
    value: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Text(text = "${(value * 100).toInt()}%", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.1f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color(0xFF3B1566)
            )
        )
    }
}
