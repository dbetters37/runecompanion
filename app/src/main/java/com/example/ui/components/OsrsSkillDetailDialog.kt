package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.CombatCard
import com.example.data.models.DefaultCombatCards
import com.example.data.models.OsrsSkill
import com.example.data.models.OsrsXpCalculator
import com.example.ui.theme.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun OsrsSkillDetailDialog(
    skill: OsrsSkill,
    currentXp: Long,
    onDismiss: () -> Unit,
    onTrainNow: () -> Unit,
    onResetXp: (() -> Unit)? = null
) {
    var showResetConfirm by remember { mutableStateOf(false) }
    var showSkillCardsDialog by remember { mutableStateOf(false) }
    val level = OsrsXpCalculator.getLevelForXp(currentXp)
    val progress = OsrsXpCalculator.getXpProgressToNextLevel(currentXp)
    val remainingForNext = OsrsXpCalculator.getXpRemainingForNextLevel(currentXp)
    val nextLevelXp = OsrsXpCalculator.getXpForLevel((level + 1).coerceAtMost(99))
    val level99Xp = OsrsXpCalculator.getXpForLevel(99)
    val remainingFor99 = (level99Xp - currentXp).coerceAtLeast(0L)

    val skillCards = remember(skill) { DefaultCombatCards.ALL_CARDS.filter { it.skill == skill }.sortedBy { it.reqLevel } }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(OsrsLeatherMedium)
                .border(2.dp, OsrsGold, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Title
                Text(
                    text = "${skill.displayName.uppercase()} SKILL GUIDE",
                    color = OsrsTextYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = skill.description,
                    color = OsrsParchment,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Divider(color = OsrsGold.copy(alpha = 0.5f))

                // Stats Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(OsrsLeatherDark)
                        .border(1.dp, OsrsRedFrame, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatRow(label = "Current Level:", value = "$level / 99", valueColor = OsrsTextYellow)
                        StatRow(label = "Current XP:", value = String.format("%,d", currentXp), valueColor = OsrsTextWhite)
                        StatRow(label = "Next Level XP:", value = String.format("%,d", nextLevelXp), valueColor = OsrsGold)
                        StatRow(label = "XP Remaining:", value = String.format("%,d", remainingForNext), valueColor = OsrsTextOrange)
                        StatRow(label = "XP to Level 99:", value = String.format("%,d", remainingFor99), valueColor = OsrsParchment)

                        Spacer(modifier = Modifier.height(4.dp))

                        // Progress Bar
                        Text(
                            text = "Level ${level + 1} Progress: ${(progress * 100).toInt()}%",
                            color = OsrsGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF18120C))
                                .border(1.dp, OsrsGold, RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(skill.accentColor)
                            )
                        }
                    }
                }

                // Real Life Training Instructions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF38291B))
                        .border(1.dp, OsrsGold, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "💡 Real-Life Training Guide:",
                            color = OsrsGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = skill.realLifeAction,
                            color = OsrsTextWhite,
                            fontSize = 12.sp
                        )
                    }
                }

                // View Skill Cards Button
                if (skillCards.isNotEmpty()) {
                    Button(
                        onClick = { showSkillCardsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OsrsGold, RoundedCornerShape(8.dp))
                            .testTag("view_skill_cards_button")
                    ) {
                        Text(
                            "🃏 View Cards Unlocks (${skillCards.size} Cards)",
                            color = OsrsGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsLeatherDark),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, OsrsGold, RoundedCornerShape(6.dp))
                    ) {
                        Text("Close", color = OsrsTextWhite)
                    }

                    if (onResetXp != null) {
                        Button(
                            onClick = { showResetConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381C10)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(6.dp))
                                .testTag("reset_skill_xp_button")
                        ) {
                            Text("🔄 Reset", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = {
                            onTrainNow()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("train_skill_now_button")
                    ) {
                        Text("Train (+300 XP)", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                if (showSkillCardsDialog) {
                    SkillCardsPreviewDialog(
                        skill = skill,
                        currentLevel = level,
                        cards = skillCards,
                        onDismiss = { showSkillCardsDialog = false }
                    )
                }

                if (showResetConfirm) {
                    AlertDialog(
                        onDismissRequest = { showResetConfirm = false },
                        title = {
                            Text("Reset ${skill.displayName} XP?", color = OsrsGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        },
                        text = {
                            Text("Are you sure you want to reset your ${skill.displayName} skill back to Level 1 (0 XP)?", color = OsrsParchment, fontSize = 13.sp)
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showResetConfirm = false
                                    onResetXp?.invoke()
                                    onDismiss()
                                }
                            ) {
                                Text("Reset to Lvl 1", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetConfirm = false }) {
                                Text("Cancel", color = OsrsGold)
                            }
                        },
                        containerColor = OsrsLeatherDark,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = OsrsParchment, fontSize = 12.sp)
        Text(text = value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SkillCardsPreviewDialog(
    skill: OsrsSkill,
    currentLevel: Int,
    cards: List<CombatCard>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OsrsDarkPanel,
            border = BorderStroke(2.dp, OsrsGold),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.80f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🃏 ${skill.displayName} Cards & Unlocks",
                        color = OsrsGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onDismiss) {
                        Text("✕ Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    "Cards unlocked by leveling ${skill.displayName}. Non-combat skill cards cost 0 Energy and buff your main attack cards!",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                Divider(color = Color.Gray, thickness = 0.5.dp)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(cards) { card ->
                        val isUnlocked = currentLevel >= card.reqLevel
                        val statusBg = if (isUnlocked) Color(0xFF166534) else Color(0xFF7F1D1D)
                        val statusText = if (isUnlocked) "UNLOCKED 🔓" else "REQUIRES LVL ${card.reqLevel} 🔒"

                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isUnlocked) OsrsGold else Color.Gray),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(card.iconEmoji, fontSize = 16.sp)
                                        Text(
                                            card.title,
                                            color = OsrsGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    // Status Badge
                                    Surface(
                                        color = statusBg,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            statusText,
                                            color = Color.White,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Energy badge
                                    Surface(
                                        color = if (card.energyCost == 0) Color(0xFF15803D) else Color(0xFF334155),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            if (card.energyCost == 0) "0⚡ (Buff Card)" else "${card.energyCost}⚡ Cost",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }

                                    if (card.baseDamage > 0) {
                                        Text("⚔️ ${card.baseDamage} Dmg", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (card.nextAttackBuff > 0) {
                                        Text("🔥 +${card.nextAttackBuff} Next Atk", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (card.baseShield > 0) {
                                        Text("🛡️ +${card.baseShield} Shield", color = Color(0xFF60A5FA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (card.baseHeal > 0) {
                                        Text("💖 +${card.baseHeal} HP", color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text(
                                    card.description,
                                    color = OsrsParchment,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
