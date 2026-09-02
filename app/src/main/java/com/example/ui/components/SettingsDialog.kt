package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.ForestAmbientAudioPlayer.SfxType
import com.example.data.models.SwipeSensitivity
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

@Composable
fun SettingsDialog(
    viewModel: PetViewModel,
    onDismiss: () -> Unit,
    onOpenMasterControlPanel: () -> Unit,
    onOpenTaskXpEditor: () -> Unit = {},
    onResetAllData: () -> Unit
) {
    val isAmbientEnabled by viewModel.isAmbientAudioEnabled.collectAsState()
    val isSfxEnabled by viewModel.isSfxAudioEnabled.collectAsState()
    val ambientVol by viewModel.ambientAudioVolume.collectAsState()
    val sfxVol by viewModel.sfxAudioVolume.collectAsState()
    val isNpcCompanionsEnabled by viewModel.isNpcCompanionsEnabled.collectAsState()
    val swipeSensitivity by viewModel.swipeTabSensitivity.collectAsState()

    val stepCount by viewModel.stepCounterManager.stepCount.collectAsState()
    val quests by viewModel.quests.collectAsState()
    val skillAppListeners by viewModel.skillAppListeners.collectAsState()

    var showResetConfirmation by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .border(2.dp, OsrsGold, RoundedCornerShape(12.dp))
                .testTag("settings_dialog_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140F0B)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("⚙️", fontSize = 22.sp)
                        Column {
                            Text(
                                text = "GAME OPTIONS & SETTINGS",
                                color = OsrsGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Swipe Sensitivity, Soundscapes, Guides & Controls",
                                color = OsrsParchment,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2C1E14))
                            .border(1.dp, Color(0xFF5A4532), RoundedCornerShape(6.dp))
                            .testTag("close_settings_button")
                    ) {
                        Text("✕", color = OsrsTextYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = Color(0xFF382A1B), thickness = 1.dp)

                // Scrollable content with vertically stacked cards
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ==========================================
                    // 1. SWIPE GESTURES & NAVIGATION SENSITIVITY
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1710)),
                        border = BorderStroke(1.dp, Color(0xFF4E3824))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("👆", fontSize = 18.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "TAB SWIPE NAVIGATION",
                                        color = OsrsGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Swipe left or right on screen to cycle tabs",
                                        color = OsrsParchment,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }

                            // 4-Button Segmented Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SwipeSensitivity.values().forEach { sensitivity ->
                                    val isSelected = swipeSensitivity == sensitivity
                                    val bg = if (isSelected) Color(0xFF2E6B44) else Color(0xFF281E15)
                                    val border = if (isSelected) Color(0xFF00FF9D) else Color(0xFF4A3423)
                                    val textColor = if (isSelected) Color.White else OsrsParchment

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(bg)
                                            .border(1.dp, border, RoundedCornerShape(6.dp))
                                            .clickable {
                                                viewModel.setSwipeTabSensitivity(sensitivity)
                                                viewModel.playTestSfx(SfxType.CLICK)
                                            }
                                            .padding(vertical = 8.dp, horizontal = 2.dp)
                                            .testTag("swipe_sensitivity_${sensitivity.name.lowercase()}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(sensitivity.iconEmoji, fontSize = 14.sp)
                                            Text(
                                                text = sensitivity.displayName,
                                                color = textColor,
                                                fontSize = 10.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            // Dynamic Helper Description
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF140D07))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "ℹ️ ${swipeSensitivity.description}",
                                    color = Color(0xFFA5D6A7),
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // ==========================================
                    // 2. AUDIO SOUNDSCAPES & SFX
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1710)),
                        border = BorderStroke(1.dp, Color(0xFF4E3824))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "🔊 AUDIO & SOUNDSCAPES",
                                color = OsrsGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            // Ambient Forest Audio
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🌲", fontSize = 16.sp)
                                    Column {
                                        Text(
                                            text = "Ambient Forest Atmosphere",
                                            color = OsrsTextYellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Gentle wind in leaves, stream & birds",
                                            color = OsrsParchment,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = isAmbientEnabled,
                                    onCheckedChange = { viewModel.toggleAmbientForestAudio() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32),
                                        uncheckedThumbColor = Color.Gray,
                                        uncheckedTrackColor = Color(0xFF2C2018)
                                    ),
                                    modifier = Modifier.testTag("ambient_audio_switch")
                                )
                            }

                            if (isAmbientEnabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Vol:", color = OsrsParchment, fontSize = 10.5.sp)
                                    Slider(
                                        value = ambientVol,
                                        onValueChange = { viewModel.setAmbientAudioVolume(it) },
                                        valueRange = 0f..1f,
                                        modifier = Modifier.weight(1f).height(24.dp).testTag("ambient_volume_slider"),
                                        colors = SliderDefaults.colors(
                                            thumbColor = OsrsGold,
                                            activeTrackColor = Color(0xFF2E7D32),
                                            inactiveTrackColor = Color(0xFF382A1B)
                                        )
                                    )
                                    Text("${(ambientVol * 100).toInt()}%", color = OsrsGold, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(color = Color(0xFF332316), thickness = 1.dp)

                            // Sound Effects (SFX)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("✨", fontSize = 16.sp)
                                    Column {
                                        Text(
                                            text = "Game Sound Effects (SFX)",
                                            color = OsrsTextYellow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Crafting, level-ups, chants & wood cutting",
                                            color = OsrsParchment,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = isSfxEnabled,
                                    onCheckedChange = { viewModel.toggleSfxAudio() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32),
                                        uncheckedThumbColor = Color.Gray,
                                        uncheckedTrackColor = Color(0xFF2C2018)
                                    ),
                                    modifier = Modifier.testTag("sfx_audio_switch")
                                )
                            }

                            if (isSfxEnabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Vol:", color = OsrsParchment, fontSize = 10.5.sp)
                                    Slider(
                                        value = sfxVol,
                                        onValueChange = { viewModel.setSfxAudioVolume(it) },
                                        valueRange = 0f..1f,
                                        modifier = Modifier.weight(1f).height(24.dp).testTag("sfx_volume_slider"),
                                        colors = SliderDefaults.colors(
                                            thumbColor = OsrsGold,
                                            activeTrackColor = Color(0xFF2E7D32),
                                            inactiveTrackColor = Color(0xFF382A1B)
                                        )
                                    )
                                    Text("${(sfxVol * 100).toInt()}%", color = OsrsGold, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }

                                // SFX Test Buttons
                                Text("Test Sound Effects:", color = OsrsParchment, fontSize = 10.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        "🔮 Chant" to SfxType.CHANT,
                                        "🎉 Level" to SfxType.LEVEL_UP,
                                        "🔨 Craft" to SfxType.CRAFT,
                                        "🪓 Chop" to SfxType.CHOP,
                                        "🖱️ Click" to SfxType.CLICK
                                    ).forEach { (label, type) ->
                                        Button(
                                            onClick = { viewModel.playTestSfx(type) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1F14)),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.weight(1f).height(28.dp).border(1.dp, Color(0xFF5A4532), RoundedCornerShape(4.dp))
                                        ) {
                                            Text(label, fontSize = 9.sp, color = OsrsTextYellow, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 3. TRIBAL NPC GUIDES & COMPANIONS
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1710)),
                        border = BorderStroke(1.dp, Color(0xFF4E3824))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🧙‍♂️ TRIBAL NPC COMPANIONS",
                                color = OsrsGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Floating NPC Guides (Finbar & Eric)",
                                        color = OsrsTextYellow,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Interactive tribal guides with live tips and quick lore",
                                        color = OsrsParchment,
                                        fontSize = 10.sp
                                    )
                                }
                                Switch(
                                    checked = isNpcCompanionsEnabled,
                                    onCheckedChange = { viewModel.toggleNpcCompanions() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32),
                                        uncheckedThumbColor = Color.Gray,
                                        uncheckedTrackColor = Color(0xFF2C2018)
                                    ),
                                    modifier = Modifier.testTag("npc_companions_switch")
                                )
                            }

                            if (isNpcCompanionsEnabled) {
                                Button(
                                    onClick = {
                                        viewModel.restoreNpcCompanions()
                                        viewModel.playTestSfx(SfxType.CLICK)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3D59)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("restore_minimized_npcs_button")
                                ) {
                                    Text("🔄 Restore All Minimized NPCs", color = Color(0xFF90E0EF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // ==========================================
                    // 4. REAL-LIFE HABITS & FITNESS
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1710)),
                        border = BorderStroke(1.dp, Color(0xFF4E3824))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🏃 REAL-LIFE HABITS & FITNESS",
                                color = OsrsGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("👣 Pedometer Step Count:", color = OsrsParchment, fontSize = 11.sp)
                                Text("$stepCount Steps", color = Color(0xFF00FF9D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenTaskXpEditor()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E4057)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_task_xp_editor_button")
                            ) {
                                Text("⚙️ Configure Task XP Editor", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // ==========================================
                    // 5. INVENTORY & AUTOMATION
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1710)),
                        border = BorderStroke(1.dp, Color(0xFF4E3824))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🎒 AUTOMATION & INVENTORY",
                                color = OsrsGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Button(
                                onClick = {
                                    viewModel.autoRouteCookedFoodToBag()
                                    viewModel.playTestSfx(SfxType.CRAFT)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auto_route_food_button")
                            ) {
                                Text("🎒 Store Cooked Food into Food Bag", color = Color(0xFFA5D6A7), fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.cleanupDuplicates()
                                    viewModel.playTestSfx(SfxType.CLICK)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF382A1B)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cleanup_duplicates_button")
                            ) {
                                Text("🧹 Deduplicate & Clean Inventory Items", color = OsrsTextYellow, fontSize = 11.sp)
                            }
                        }
                    }

                    // ==========================================
                    // 6. MASTER CONTROLS & DATA RESET
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1710)),
                        border = BorderStroke(1.dp, Color(0xFF4E3824))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚙️ ADVANCED CONTROLS & DATA",
                                color = OsrsGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenMasterControlPanel()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_master_control_panel_button")
                            ) {
                                Text("⚙️ Master Control Panel (XP Rates & Tasks)", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showResetConfirmation = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reset_app_data_button")
                            ) {
                                Text("⚠️ Reset All Pet & Game Data", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            containerColor = Color(0xFF140F0B),
            title = {
                Text("⚠️ Reset All Game Data?", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to reset all pet levels, GP, bank items, and progression? This action cannot be undone.", color = OsrsParchment)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmation = false
                        onDismiss()
                        onResetAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("YES, RESET ALL DATA", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showResetConfirmation = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C1E14))
                ) {
                    Text("CANCEL", color = OsrsParchment)
                }
            }
        )
    }
}
