package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.OsrsSkill
import com.example.data.models.PetState
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BattleLocation(
    val name: String,
    val iconEmoji: String,
    val reqLevel: Int,
    val monsterName: String,
    val monsterIcon: String,
    val monsterHp: Int,
    val monsterLevel: Int,
    val rewardXp: Long,
    val rewardGp: Long,
    val possibleDropName: String,
    val possibleDropIcon: String
)

val BATTLE_LOCATIONS = listOf(
    BattleLocation("Lumbridge Swamps", "🌲", 1, "Giant Frog", "🐸", 50, 15, 250L, 80L, "Raw Lobster", "🦞"),
    BattleLocation("Varrock Sewers", "🗡️", 10, "Moss Giant", "🧌", 120, 42, 600L, 250L, "Rune Scimitar", "🗡️"),
    BattleLocation("TzHaar Volcano", "🌋", 30, "TzHaar-Ket", "🗿", 250, 140, 1500L, 800L, "Toktz-Ket-Xil", "🛡️"),
    BattleLocation("The Wilderness", "💀", 50, "Green Dragon", "🐲", 400, 210, 3000L, 2000L, "Dragon Bones", "🦴"),
    BattleLocation("God Wars Dungeon", "⚡", 70, "General Graardor", "👺", 800, 624, 8000L, 7500L, "Bandos Chestplate", "🛡️")
)

@Composable
fun PetBattleDialog(
    petState: PetState,
    maxPetHp: Int = 100,
    onWinBattle: (location: BattleLocation, combatSkill: OsrsSkill) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedLocation by remember { mutableStateOf(BATTLE_LOCATIONS[0]) }
    var inBattle by remember { mutableStateOf(false) }
    var petHp by remember { mutableIntStateOf(petState.health.coerceAtMost(maxPetHp)) }
    var monsterHp by remember { mutableIntStateOf(selectedLocation.monsterHp) }
    var combatLog by remember { mutableStateOf("Select an area and press 'Engage Battle'!") }
    var isVictorious by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame)
            ) {
                Text("Close Arena", color = OsrsTextYellow)
            }
        },
        title = {
            Text("⚔️ PET MONSTER BATTLE ARENA", color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Location Selector
                Text("📍 Select Battle Destination:", color = OsrsGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(BATTLE_LOCATIONS) { loc ->
                        val isSelected = loc == selectedLocation
                        Card(
                            onClick = {
                                if (!inBattle) {
                                    selectedLocation = loc
                                    monsterHp = loc.monsterHp
                                    petHp = petState.health.coerceAtMost(maxPetHp)
                                    isVictorious = false
                                    combatLog = "Ready for battle against ${loc.monsterName}!"
                                }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) OsrsRedFrame else Color(0xFF2B2018)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) OsrsGold else Color.Gray),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(loc.iconEmoji, fontSize = 16.sp)
                                Text(loc.name, color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Divider(color = OsrsGold)

                // Battle Screen Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1610)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OsrsParchment)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pet Side
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(petState.petType.iconSymbol, fontSize = 42.sp)
                                Text(petState.customName, color = OsrsTextYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("HP: $petHp / $maxPetHp", color = Color(0xFF70E000), fontSize = 11.sp)
                            }

                            Text("VS", color = Color.Red, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)

                            // Monster Side
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(selectedLocation.monsterIcon, fontSize = 42.sp)
                                Text("${selectedLocation.monsterName} (Lvl ${selectedLocation.monsterLevel})", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("HP: $monsterHp / ${selectedLocation.monsterHp}", color = Color.Red, fontSize = 11.sp)
                            }
                        }

                        // Combat Log Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Color(0xFF120E0A))
                                .border(1.dp, Color(0xFF4A3828))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(combatLog, color = OsrsTextWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Battle Action Buttons
                        if (isVictorious) {
                            Button(
                                onClick = {
                                    onWinBattle(selectedLocation, OsrsSkill.ATTACK)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70E000)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("claim_battle_victory_button")
                            ) {
                                Text("🏆 Victory! Claim Rewards & Drops", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        if (!inBattle) {
                                            scope.launch {
                                                inBattle = true
                                                combatLog = "💥 ${petState.customName} attacks ${selectedLocation.monsterName}!"
                                                delay(600)

                                                val hit = (12..45).random()
                                                monsterHp = (monsterHp - hit).coerceAtLeast(0)
                                                combatLog = "💥 Hit $hit damage on ${selectedLocation.monsterName}!"

                                                if (monsterHp <= 0) {
                                                    isVictorious = true
                                                    combatLog = "🎉 ${selectedLocation.monsterName} defeated! You got ${selectedLocation.rewardGp} GP & ${selectedLocation.possibleDropName}!"
                                                    inBattle = false
                                                    return@launch
                                                }

                                                delay(600)
                                                val monHit = (4..20).random()
                                                petHp = (petHp - monHit).coerceAtLeast(0)
                                                combatLog = "⚠️ ${selectedLocation.monsterName} retaliates for $monHit damage!"
                                                inBattle = false
                                            }
                                        }
                                    },
                                    enabled = !inBattle,
                                    colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("battle_attack_button")
                                ) {
                                    Text("⚔️ Hand Combat", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        if (!inBattle) {
                                            scope.launch {
                                                inBattle = true
                                                combatLog = "⚡ ${petState.customName} unleashes SPECIAL ATTACK!"
                                                delay(600)

                                                val specHit = (35..80).random()
                                                monsterHp = (monsterHp - specHit).coerceAtLeast(0)
                                                combatLog = "⚡ CRITICAL SPEC HIT: $specHit DAMAGE!"

                                                if (monsterHp <= 0) {
                                                    isVictorious = true
                                                    combatLog = "🎉 ${selectedLocation.monsterName} vanquished by Special Attack!"
                                                    inBattle = false
                                                    return@launch
                                                }

                                                delay(600)
                                                val monHit = (2..15).random()
                                                petHp = (petHp - monHit).coerceAtLeast(0)
                                                combatLog = "⚠️ ${selectedLocation.monsterName} hits back for $monHit damage!"
                                                inBattle = false
                                            }
                                        }
                                    },
                                    enabled = !inBattle,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("battle_spec_button")
                                ) {
                                    Text("⚡ Spec", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = OsrsLeatherDark
    )
}
