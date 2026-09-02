package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.PersonalityEntity
import com.example.ui.components.PetExpression
import com.example.ui.components.ShamanPetCanvas

data class SkinItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val tagColor: Color
)

val AVAILABLE_SKINS = listOf(
    SkinItem(
        id = "SHAMAN_DEFAULT",
        name = "Current Skin (Shaman Spirit)",
        subtitle = "Ancestral Spirit Form",
        description = "The core ancestral shaman form with swirling aura particles, crown horns, and spirit forehead emblem.",
        tagColor = Color(0xFF52B788)
    ),
    SkinItem(
        id = "SABLEYE",
        name = "Sableye Skin",
        subtitle = "Gem-Crested Darkness",
        description = "Dark purple crystalline shadow spirit with brilliant cyan diamond gem eyes and a chest ruby.",
        tagColor = Color(0xFF66E0FF)
    ),
    SkinItem(
        id = "DARK_CHAO",
        name = "Dark Chao Skin",
        subtitle = "Shadow Chao Form",
        description = "Playful dark chao form with a spiky floating dark halo orb, bat wings, and fiery glowing eyes.",
        tagColor = Color(0xFFFF0055)
    ),
    SkinItem(
        id = "LIGHT_CHAO",
        name = "Light Chao Skin",
        subtitle = "Hero Angel Form",
        description = "Gentle hero chao form with a floating golden angel halo, soft cyan cloud spirit body, and white wings.",
        tagColor = Color(0xFFFFD700)
    ),
    SkinItem(
        id = "CASTFORM",
        name = "Castform Skin",
        subtitle = "Weather Cloud Form",
        description = "Puffy cloud weather-sphere companion with circular mask eye ring and hovering atmospheric puffs.",
        tagColor = Color(0xFF90A4AE)
    ),
    SkinItem(
        id = "BANETTE",
        name = "Banette Skin",
        subtitle = "Ghostly Zipper Marionette",
        description = "Dark slate ghost marionette with glowing yellow eyes, long drooping spirit cap, and a gold zipper smile.",
        tagColor = Color(0xFFFFD166)
    ),
    SkinItem(
        id = "CACNEA",
        name = "Cacnea Skin",
        subtitle = "Desert Cactus Companion",
        description = "Cheerful round cactus spirit with dark green thorns, yellow flower crown blossom, and cheerful dark eyes.",
        tagColor = Color(0xFF52B788)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinsScreen(
    personality: PersonalityEntity?,
    onSelectSkin: (String) -> Unit,
    onToggleAutoShift: (Boolean) -> Unit,
    onTriggerRandomShift: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val activeSkin = personality?.activeSkin ?: "SHAMAN_DEFAULT"
    val autoShiftEnabled = personality?.autoSkinShiftEnabled ?: true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Companion Form & Skins",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Choose a form or allow AI to transform autonomously",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB7E4C7)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0E2417)
                )
            )
        },
        containerColor = Color(0xFF09170E)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Active Form Banner Card
            item {
                val currentSkinItem = AVAILABLE_SKINS.find { it.id == activeSkin } ?: AVAILABLE_SKINS[0]
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF133220),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFF2D6A4F), Color(0xFF00F5D4))))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ShamanPetCanvas(
                            expression = PetExpression.HAPPY,
                            skin = activeSkin,
                            size = 80.dp,
                            showFpsBadge = false
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = Color(0xFF00F5D4).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "CURRENTLY ACTIVE",
                                    color = Color(0xFF00F5D4),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentSkinItem.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentSkinItem.subtitle,
                                color = Color(0xFF74C69D),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Autonomous Shift Control Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF112A1B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF2D6A4F).copy(alpha = 0.4f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoMode,
                                            contentDescription = "Auto Shift",
                                            tint = Color(0xFF00F5D4),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Autonomous Skin Shifts",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Allows AI companion to shift forms 2-3x a day based on mood",
                                        color = Color(0xFFB7E4C7),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Switch(
                                checked = autoShiftEnabled,
                                onCheckedChange = { checked ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleAutoShift(checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF00F5D4),
                                    checkedTrackColor = Color(0xFF2D6A4F)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTriggerRandomShift()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(Color(0xFF2D6A4F), Color(0xFF1B4332)))
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Random Shift",
                                tint = Color(0xFF74C69D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ask Companion to Pick a Skin Now",
                                color = Color(0xFFB7E4C7),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Section Label
            item {
                Text(
                    text = "AVAILABLE FORMS",
                    color = Color(0xFF52B788),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // Skin Selector List
            items(AVAILABLE_SKINS) { skin ->
                val isSelected = skin.id == activeSkin

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectSkin(skin.id)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1B4332) else Color(0xFF112A1B)
                    ),
                    border = if (isSelected) {
                        CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF00F5D4), Color(0xFF52B788))))
                    } else {
                        CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF183B27)))
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Live Canvas Preview of this Skin
                        ShamanPetCanvas(
                            expression = if (isSelected) PetExpression.HAPPY else PetExpression.IDLE,
                            skin = skin.id,
                            size = 64.dp,
                            showFpsBadge = false
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = skin.name,
                                color = if (isSelected) Color(0xFF00F5D4) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = skin.subtitle,
                                color = skin.tagColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = skin.description,
                                color = Color(0xFFB7E4C7),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        if (isSelected) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF00F5D4),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF09170E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSelectSkin(skin.id)
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = SolidColor(Color(0xFF2D6A4F))
                                )
                            ) {
                                Text(
                                    text = "Equip",
                                    color = Color(0xFF74C69D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
