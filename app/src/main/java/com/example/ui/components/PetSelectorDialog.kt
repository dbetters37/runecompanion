package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.PetState
import com.example.data.models.PetType
import com.example.ui.theme.*

@Composable
fun PetSelectorDialog(
    currentPetState: PetState,
    onSelectPetType: (PetType) -> Unit,
    onAdoptPet: (PetType) -> Unit = {},
    onRenamePet: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var petNameInput by remember { mutableStateOf(currentPetState.customName) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPets = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            PetType.entries
        } else {
            val q = searchQuery.trim().lowercase()
            PetType.entries.filter { pet ->
                pet.displayName.lowercase().contains(q) ||
                        pet.description.lowercase().contains(q) ||
                        pet.primarySkill.displayName.lowercase().contains(q)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(OsrsLeatherMedium)
                .border(2.dp, OsrsGold, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🐾 COMPANION PET HAVEN (${filteredPets.size} Roster)",
                    color = OsrsTextYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                // Rename Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = petNameInput,
                        onValueChange = { petNameInput = it },
                        label = { Text("Active Pet Name", color = OsrsGold, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OsrsGold,
                            unfocusedBorderColor = OsrsParchment,
                            focusedTextColor = OsrsTextYellow,
                            unfocusedTextColor = OsrsTextWhite
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = { onRenamePet(petNameInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = OsrsRedFrame),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Rename", color = OsrsTextYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("🔍 Search Sinnoh Gen 4, Divination, Sailing...", color = Color.Gray, fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OsrsGold,
                        unfocusedBorderColor = OsrsParchment,
                        focusedTextColor = OsrsTextYellow,
                        unfocusedTextColor = OsrsTextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                Divider(color = OsrsGold.copy(alpha = 0.5f))

                // List of Pets & Pokemon
                if (filteredPets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pets or Pokémon found matching \"$searchQuery\"",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(filteredPets) { petType ->
                            val isSelected = currentPetState.petType == petType
                            val isUnlocked = currentPetState.unlockedPets.contains(petType)
                            val isEvolutionOnly = !isUnlocked && (petType.stage == 2 || petType.stage == 3)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF381C10) else OsrsLeatherDark)
                                    .border(1.dp, if (isSelected) OsrsGoldBright else OsrsRedFrame, RoundedCornerShape(8.dp))
                                    .clickable(enabled = isUnlocked) {
                                        onSelectPetType(petType)
                                        onDismiss()
                                    }
                                    .padding(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(text = petType.iconSymbol, fontSize = 32.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = petType.displayName,
                                                color = if (isSelected) OsrsTextYellow else if (isUnlocked) OsrsParchment else Color.Gray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            if (isSelected) {
                                                Text(
                                                    text = "✓ ACTIVE",
                                                    color = Color(0xFF42D392),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            } else if (isUnlocked) {
                                                Text(
                                                    text = "✓ READY",
                                                    color = Color(0xFF38B000),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Text(
                                            text = petType.description,
                                            color = if (isUnlocked) Color.LightGray else Color.Gray,
                                            fontSize = 10.sp
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = "Skill: ${petType.primarySkill.displayName}",
                                                color = OsrsGold,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            if (isEvolutionOnly) {
                                                val base = petType.evolvesFromName ?: "Base Stage"
                                                Text(
                                                    text = "🔒 Evolves from $base (Lvl ${petType.evolutionLevelReq})",
                                                    color = Color(0xFFFFAA00),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else if (!isUnlocked) {
                                                Button(
                                                    onClick = {
                                                        onAdoptPet(petType)
                                                        onDismiss()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6A4F)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier.height(26.dp)
                                                ) {
                                                    Text(
                                                        text = "ADOPT (${petType.unlockCostGp} GP)",
                                                        color = OsrsTextYellow,
                                                        fontSize = 9.sp,
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
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OsrsLeatherDark),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OsrsGold, RoundedCornerShape(6.dp))
                ) {
                    Text("Close", color = OsrsTextWhite)
                }
            }
        }
    }
}
