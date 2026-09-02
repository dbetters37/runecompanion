package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.InventoryItem
import com.example.ui.theme.OsrsGold
import com.example.ui.theme.OsrsLeatherMedium
import com.example.ui.theme.OsrsParchment
import com.example.ui.theme.OsrsTextYellow

@Composable
fun UniqueItemDetailDialog(
    item: InventoryItem,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF141E28),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF223040),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OsrsGold),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = item.iconEmoji, fontSize = 24.sp)
                    }
                }

                Column {
                    Text(
                        text = item.name,
                        color = OsrsTextYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = item.equipmentSlot?.displayName?.let { "Equipment Slot: $it" }
                            ?: "Category: ${item.category.name.replace("_", " ")}",
                        color = Color(0xFF00B4D8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Description Card
                Surface(
                    color = Color(0xFF1E2A38),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3E50)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "📜 Item Description",
                            color = Color(0xFF00FF9D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = item.description,
                            color = OsrsParchment,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Stats & Properties Breakdown
                Surface(
                    color = Color(0xFF1E2A38),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3E50)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📊 Item Stats & Attributes",
                            color = OsrsGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )

                        var hasStat = false

                        if (item.combatPowerBonus > 0) {
                            hasStat = true
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("⚔️ Combat Power Bonus:", color = Color.LightGray, fontSize = 11.sp)
                                Text("+${item.combatPowerBonus}", color = Color(0xFF00FF9D), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        if (item.defPowerBonus > 0) {
                            hasStat = true
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("🛡️ Defence Power Bonus:", color = Color.LightGray, fontSize = 11.sp)
                                Text("+${item.defPowerBonus}", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        if (item.healHp > 0) {
                            hasStat = true
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("❤️ Health Restoration:", color = Color.LightGray, fontSize = 11.sp)
                                Text("+${item.healHp} HP", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        if (item.restoreHunger > 0) {
                            hasStat = true
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("🍗 Hunger Restoration:", color = Color.LightGray, fontSize = 11.sp)
                                Text("+${item.restoreHunger} Hunger", color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        if (item.addHappiness > 0) {
                            hasStat = true
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("😊 Happiness Gain:", color = Color.LightGray, fontSize = 11.sp)
                                Text("+${item.addHappiness} Joy", color = Color(0xFFBA68C8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        if (item.bonusXpSkill != null && item.bonusXpAmount > 0) {
                            hasStat = true
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("🧠 Skill XP Bonus:", color = Color.LightGray, fontSize = 11.sp)
                                Text("+${item.bonusXpAmount} ${item.bonusXpSkill.displayName} XP", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("🪙 Market / Sell Value:", color = Color.LightGray, fontSize = 11.sp)
                            Text("${item.costGp} GP", color = OsrsGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        if (!hasStat) {
                            Text(
                                text = "• Unique Quest / Story Artifact",
                                color = Color.Gray,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
            ) {
                Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}
