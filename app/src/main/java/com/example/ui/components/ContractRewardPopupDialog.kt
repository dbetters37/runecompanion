package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OsrsParchment
import com.example.ui.theme.OsrsTextYellow
import com.example.viewmodel.ContractRewardOpenResult

@Composable
fun ContractRewardPopupDialog(
    result: ContractRewardOpenResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_contract_reward_popup"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🎁", fontSize = 24.sp)
                Column {
                    Text(
                        text = "Contract Reward Opened!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Text(
                        text = "${result.openedCount}x ${result.skill.displayName} Box${if (result.openedCount > 1) "es" else ""}",
                        fontSize = 12.sp,
                        color = OsrsParchment
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Outfits Unlocked Banner (if any)
                if (result.outfitPiecesUnlocked.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF332608)),
                        border = BorderStroke(1.5.dp, Color(0xFFFFD700))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "🌟 ULTRA RARE OUTFIT DROP! (1% Chance)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            result.outfitPiecesUnlocked.forEach { piece ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(piece.iconEmoji, fontSize = 18.sp)
                                    Text(
                                        text = "${piece.name} (+5% ${result.skill.displayName} XP)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF81C784)
                                    )
                                }
                            }
                        }
                    }
                }

                // XP & GP Rewards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // GP Gained
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E1710),
                        border = BorderStroke(1.dp, Color(0xFF6B533E)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("💰", fontSize = 18.sp)
                            Column {
                                Text(
                                    text = "+${result.totalGp} GP",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    text = "Coin Pouch",
                                    fontSize = 9.5.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }

                    // XP Gained
                    if (result.totalXp > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E1710),
                            border = BorderStroke(1.dp, Color(0xFF6B533E)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("⚡", fontSize = 18.sp)
                                Column {
                                    Text(
                                        text = "+${result.totalXp} XP",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF81C784)
                                    )
                                    Text(
                                        text = result.skill.displayName,
                                        fontSize = 9.5.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Items Gained List
                if (result.itemsGained.isNotEmpty()) {
                    Text(
                        text = "Items & Materials Received (${result.itemsGained.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )

                    result.itemsGained.forEach { (item, qty) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E1710))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(item.iconEmoji, fontSize = 16.sp)
                                Text(
                                    text = item.name,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "+$qty",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C784)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier.testTag("button_collect_contract_loot")
            ) {
                Text("Collect Loot!", fontWeight = FontWeight.Bold, color = OsrsTextYellow)
            }
        },
        containerColor = Color(0xFF231A12)
    )
}
