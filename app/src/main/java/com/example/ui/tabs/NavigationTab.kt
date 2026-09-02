package com.example.ui.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.OsrsSkill
import com.example.data.models.OsrsXpCalculator
import com.example.ui.components.CaptainBarnabyNpcCompanion
import com.example.ui.components.NauticalOceanicPanel
import com.example.ui.components.NauticalHeaderBanner
import com.example.ui.theme.*
import com.example.viewmodel.PetViewModel

data class NavColorUnlock(
    val name: String,
    val colorInt: Long,
    val reqLevel: Int
)

data class NavAnimUnlock(
    val name: String,
    val emoji: String,
    val desc: String,
    val reqLevel: Int
)

val NAV_COLOR_PROGRESSION_UNLOCKS = listOf(
    NavColorUnlock("Ocean Azure", 0xFF0096C7, 1),
    NavColorUnlock("Seafoam Cyan", 0xFF00E5FF, 3),
    NavColorUnlock("Sapphire Deep", 0xFF0077B6, 6),
    NavColorUnlock("Coral Gold", 0xFFFFB74D, 9),
    NavColorUnlock("Emerald Cove", 0xFF4CAF50, 12),
    NavColorUnlock("Sunken Amethyst", 0xFF9C27B0, 15),
    NavColorUnlock("Siren Rose", 0xFFE91E63, 18),
    NavColorUnlock("Kraken Gold", 0xFFFFD700, 21),
    NavColorUnlock("Glacial Turquoise", 0xFF80DEEA, 24),
    NavColorUnlock("Leviathan Crimson", 0xFFD50000, 27),
    NavColorUnlock("Abyss Obsidian", 0xFF37474F, 30),
    NavColorUnlock("Crest Pure White", 0xFFFFFFFF, 33),
    NavColorUnlock("Tempest Purple", 0xFF7C4DFF, 36),
    NavColorUnlock("Island Sunrise", 0xFFFF6E40, 39),
    NavColorUnlock("Bioluminescent Lime", 0xFF76FF03, 42),
    NavColorUnlock("Electric Current", 0xFF304FFE, 45),
    NavColorUnlock("Red Sea Flame", 0xFFC62828, 48),
    NavColorUnlock("Frost Iceberg", 0xFF80D8FF, 51),
    NavColorUnlock("Pearl Pink", 0xFFFF4081, 54),
    NavColorUnlock("Bronze Nautical", 0xFFA1887F, 57),
    NavColorUnlock("Steel Anchor", 0xFFB0BEC5, 60),
    NavColorUnlock("Sol Sun Gold", 0xFFFFD54F, 63),
    NavColorUnlock("Emerald Tide", 0xFF00E676, 66),
    NavColorUnlock("Sapphire Wave", 0xFF2979FF, 69),
    NavColorUnlock("Diamond Horizon", 0xFFEA80FC, 72),
    NavColorUnlock("Amber Lagoon", 0xFFFFAB40, 75),
    NavColorUnlock("Mystic Deep Purple", 0xFFAA00FF, 78),
    NavColorUnlock("Onyx Trench", 0xFF212121, 81),
    NavColorUnlock("Zenyte Sunset", 0xFFFF6D00, 84),
    NavColorUnlock("Void Wave", 0xFF4A148C, 87),
    NavColorUnlock("Prismatic Rainbow", 0xFFE040FB, 90),
    NavColorUnlock("Sacred Aurora", 0xFFFAFAFA, 93),
    NavColorUnlock("Maelstrom Blue", 0xFF00B0FF, 96),
    NavColorUnlock("Master Admiral Gold", 0xFFFFD700, 99)
)

val NAV_ANIMATION_PROGRESSION_UNLOCKS = listOf(
    NavAnimUnlock("Ocean Waves", "🌊", "Gentle rolling sea crests & foam ripples", 1),
    NavAnimUnlock("Bioluminescent Surge", "✨", "Glow-in-the-dark plankton trail sparkles", 10),
    NavAnimUnlock("Wind Vortex Ring", "🌪️", "Swirling wind gusts & current halo", 20),
    NavAnimUnlock("Kraken Wake", "🐙", "Pulsing deep sea tentacles & tide foam", 30),
    NavAnimUnlock("Tidal Shockwave", "⚡", "Radial wave blast with water droplets", 40),
    NavAnimUnlock("Star Navigation Burst", "⭐", "Celestial star map light rays & guiding beacons", 50),
    NavAnimUnlock("Meteor Tide Streamer", "🌠", "Streaking shooting star sea trail", 60),
    NavAnimUnlock("Storm Lightning Flash", "⚡", "Ocean storm lightning bolt sparks", 70),
    NavAnimUnlock("Tsunami Crest Surge", "🌊", "High seas crest surge with water spray", 80),
    NavAnimUnlock("Astral Horizon Comet", "☄️", "Deep space astral compass comet trail", 90),
    NavAnimUnlock("Master Admiral Crown", "👑", "Golden admiral anchor & wave aura eruption", 99)
)

@Composable
fun NavigationTab(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val skillXpMap by viewModel.skillXpMap.collectAsStateWithLifecycle()
    val navColor1 by viewModel.navColor1.collectAsStateWithLifecycle()
    val navColor2 by viewModel.navColor2.collectAsStateWithLifecycle()
    val selectedNavAnimations by viewModel.selectedNavAnimations.collectAsStateWithLifecycle()

    val navXp = skillXpMap[OsrsSkill.SAILING] ?: 0L
    val navLevel = OsrsXpCalculator.getLevelForXp(navXp)
    val nextLevelXp = OsrsXpCalculator.getXpForLevel(navLevel + 1)
    val currentLevelXp = OsrsXpCalculator.getXpForLevel(navLevel)
    val progress = if (navLevel >= 99) 1f else {
        ((navXp - currentLevelXp).toFloat() / (nextLevelXp - currentLevelXp).toFloat()).coerceIn(0f, 1f)
    }

    var activeColorWheelSlot by remember { mutableIntStateOf(1) } // 1: Color 1, 2: Color 2
    var selectedProgressionSubTab by remember { mutableIntStateOf(0) } // 0: Colors, 1: Styles

    val unlockedColorsCount = NAV_COLOR_PROGRESSION_UNLOCKS.count { navLevel >= it.reqLevel }
    val unlockedAnimsCount = NAV_ANIMATION_PROGRESSION_UNLOCKS.count { navLevel >= it.reqLevel }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OsrsLeatherDark)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        // --- Top Header Banner (Fully reformatted to display all text cleanly) ---
        NauticalOceanicPanel(
            modifier = Modifier.fillMaxWidth(),
            accentIcon = "⛵",
            borderColor = Color(0xFF00E5FF),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
            // Row 1: Title, Level & XP Badge + Outfit
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⛵", fontSize = 18.sp)
                    Column {
                        Text(
                            "Navigation Guild",
                            color = OsrsGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Level $navLevel • ${String.format("%,d", navXp)} XP",
                            color = Color(0xFF00E5FF),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF0096C7),
                trackColor = Color(0xFF002738)
            )
        }

        // --- Captain Barnaby's Courier Parcel Favor ---
        val activeContracts by viewModel.contractsMap.collectAsStateWithLifecycle()
        val invItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
        val sailingContract = activeContracts[OsrsSkill.SAILING]
        val parcelInfo = remember(activeContracts, invItems) { viewModel.getActiveBarnabyParcelInfo() }
        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

        if (sailingContract != null && parcelInfo != null) {
            val sVillager = com.example.data.models.NpcData.findNpcById(parcelInfo.sourceNpcId)
            val dVillager = com.example.data.models.NpcData.findNpcById(parcelInfo.destNpcId)
            val isCompleted = sailingContract.currentQty >= sailingContract.targetQty

            NauticalOceanicPanel(
                modifier = Modifier.fillMaxWidth(),
                accentIcon = "📦",
                borderColor = Color(0xFF00E5FF),
                contentPadding = PaddingValues(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("⛵", fontSize = 16.sp)
                            Text(
                                "Captain Barnaby's Courier Dispatch",
                                color = OsrsGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isCompleted) Color(0xFF2E7D32) else Color(0xFF0277BD)
                        ) {
                            Text(
                                text = if (isCompleted) "DELIVERED ✓" else if (parcelInfo.hasCollectedParcel) "IN TRANSIT 🏃" else "READY TO PICK UP 📦",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Route Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF001A29)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF0077B6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Source NPC
                                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                                    Text(
                                        "FROM (Sender)",
                                        color = Color(0xFF80DEEA),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(sVillager?.avatarEmoji ?: "👤", fontSize = 14.sp)
                                        Text(
                                            parcelInfo.sourceNpcName,
                                            color = Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        sVillager?.hutLocation ?: "Tribal Hut",
                                        color = Color.LightGray,
                                        fontSize = 9.sp
                                    )
                                }

                                Text("➡️", fontSize = 16.sp, modifier = Modifier.padding(horizontal = 4.dp))

                                // Destination NPC
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                    Text(
                                        "TO (Recipient)",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            parcelInfo.destNpcName,
                                            color = Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(dVillager?.avatarEmoji ?: "👤", fontSize = 14.sp)
                                    }
                                    Text(
                                        dVillager?.hutLocation ?: "Tribal Hut",
                                        color = Color.LightGray,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            Divider(color = Color(0xFF003853))

                            // Status instructions
                            val instructionText = when {
                                isCompleted -> "🎉 Parcel delivered! Report back to Captain Barnaby to claim your Navigation rewards & Favor Box!"
                                parcelInfo.hasCollectedParcel -> "🎒 Parcel is in your backpack! Deliver it to ${parcelInfo.destNpcName} at ${dVillager?.hutLocation ?: "their hut"}."
                                else -> "📦 Pick up Captain Barnaby's sealed parcel from ${parcelInfo.sourceNpcName} at ${sVillager?.hutLocation ?: "their hut"}."
                            }
                            Text(
                                text = instructionText,
                                color = Color(0xFFE0F7FA),
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isCompleted) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    viewModel.claimSkillContract(OsrsSkill.SAILING)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Text(
                                    "🎉 Complete Favor (+250 XP, +500 GP) 🎁",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else if (parcelInfo.hasCollectedParcel) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    viewModel.deliverBarnabyParcel(parcelInfo.destNpcId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Text(
                                    "📦 Deliver to ${parcelInfo.destNpcName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    viewModel.collectBarnabyParcel(parcelInfo.sourceNpcId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Text(
                                    "📦 Collect from ${parcelInfo.sourceNpcName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                viewModel.assignNewSkillContract(OsrsSkill.SAILING)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB74D)),
                            border = BorderStroke(1.dp, Color(0xFF0077B6)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("🔄 Reroll", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- NAVIGATION PROGRESSION GUIDE (Formatted in Columns & Rows) ---
        NauticalOceanicPanel(
            modifier = Modifier.fillMaxWidth(),
            accentIcon = "📜",
            borderColor = OsrsGold,
            contentPadding = PaddingValues(12.dp)
        ) {
            // Title and Sub-tab Toggle buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📜", fontSize = 18.sp)
                        Text(
                            "Navigation Progression Guide",
                            color = OsrsGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Columns & Rows Sub-Tab Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { selectedProgressionSubTab = 0 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedProgressionSubTab == 0) OsrsRedFrame else Color(0xFF2C2018)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "🎨 Vessel Colors ($unlockedColorsCount/34)",
                                color = if (selectedProgressionSubTab == 0) OsrsTextYellow else Color.Gray,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = { selectedProgressionSubTab = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedProgressionSubTab == 1) OsrsRedFrame else Color(0xFF2C2018)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "✨ Animation Styles ($unlockedAnimsCount/11)",
                                color = if (selectedProgressionSubTab == 1) OsrsTextYellow else Color.Gray,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (selectedProgressionSubTab == 0) {
                    // --- COLOR PALETTE UNLOCKS (Formatted in Columns & Rows Grid) ---
                    Text(
                        "Unlock 1 custom Vessel & Aura Color every 3 Navigation Levels! Tap any unlocked color to change the active vessel color.",
                        color = OsrsParchment,
                        fontSize = 11.sp,
                        softWrap = true
                    )

                    // Target Color Slot Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Active Slot Target:", color = OsrsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        Button(
                            onClick = { activeColorWheelSlot = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeColorWheelSlot == 1) Color(0xFF0096C7) else Color(0xFF2B2017)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(10.dp).background(Color(navColor1), CircleShape))
                                Text("Slot 1 (Primary)", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { activeColorWheelSlot = 2 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeColorWheelSlot == 2) Color(0xFF00E5FF) else Color(0xFF2B2017)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f).height(32.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(10.dp).background(Color(navColor2), CircleShape))
                                Text("Slot 2 (Aura Glow)", fontSize = 10.sp, color = if (activeColorWheelSlot == 2) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // COLUMNS & ROWS GRID FOR COLORS (3 items per row)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NAV_COLOR_PROGRESSION_UNLOCKS.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { colorUnlock ->
                                    val isUnlocked = navLevel >= colorUnlock.reqLevel
                                    val isEquipped = navColor1 == colorUnlock.colorInt.toInt() || navColor2 == colorUnlock.colorInt.toInt()

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isUnlocked) Color(0xFF1E140C) else Color(0xFF120C08),
                                        border = BorderStroke(
                                            if (isEquipped) 2.dp else 1.dp,
                                            if (isEquipped) OsrsGold else if (isUnlocked) Color(0xFF4A3828) else Color(0xFF2A1C12)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(enabled = isUnlocked) {
                                                if (activeColorWheelSlot == 1) {
                                                    viewModel.setNavigationColors(colorUnlock.colorInt.toInt(), navColor2)
                                                } else {
                                                    viewModel.setNavigationColors(navColor1, colorUnlock.colorInt.toInt())
                                                }
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isUnlocked) Color(colorUnlock.colorInt) else Color.DarkGray)
                                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (!isUnlocked) {
                                                    Text("🔒", fontSize = 11.sp)
                                                }
                                            }

                                            Text(
                                                colorUnlock.name,
                                                color = if (isUnlocked) OsrsTextWhite else Color.Gray,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1
                                            )

                                            Text(
                                                if (isUnlocked) (if (isEquipped) "Equipped" else "Tap to Set") else "Lvl ${colorUnlock.reqLevel}",
                                                color = if (isEquipped) OsrsTextYellow else if (isUnlocked) Color(0xFF00E5FF) else Color.Red,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                // Fill missing slots in last incomplete row if any
                                if (rowItems.size < 3) {
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // --- SEA TRAIL ANIMATION STYLES (Formatted in Columns & Rows Grid) ---
                    Text(
                        "Tap any unlocked animation style below to select it! Tapping will immediately update and change the background particle animation.",
                        color = OsrsParchment,
                        fontSize = 11.sp,
                        softWrap = true
                    )

                    // COLUMNS & ROWS GRID FOR ANIMATIONS (2 items per row)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NAV_ANIMATION_PROGRESSION_UNLOCKS.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { anim ->
                                    val isUnlocked = navLevel >= anim.reqLevel
                                    val isActive = selectedNavAnimations.contains(anim.name)

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isActive) Color(0xFF003847) else if (isUnlocked) Color(0xFF1E140C) else Color(0xFF120C08),
                                        border = BorderStroke(
                                            if (isActive) 2.dp else 1.dp,
                                            if (isActive) Color(0xFF00E5FF) else if (isUnlocked) Color(0xFF4A3828) else Color(0xFF2A1C12)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable(enabled = isUnlocked) {
                                                viewModel.toggleNavigationAnimation(anim.name)
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(anim.emoji, fontSize = 20.sp)
                                                Text(
                                                    anim.name,
                                                    color = if (isActive) OsrsTextYellow else if (isUnlocked) OsrsTextWhite else Color.Gray,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                            }

                                            Text(
                                                anim.desc,
                                                color = OsrsParchment,
                                                fontSize = 9.sp,
                                                maxLines = 2,
                                                softWrap = true
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Button(
                                                onClick = { viewModel.toggleNavigationAnimation(anim.name) },
                                                enabled = isUnlocked,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isActive) Color(0xFF00E5FF) else Color(0xFF2B2017)
                                                ),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.fillMaxWidth().height(26.dp),
                                                contentPadding = PaddingValues(vertical = 0.dp)
                                            ) {
                                                Text(
                                                    if (isUnlocked) (if (isActive) "SELECTED ✓" else "TAP TO SELECT") else "LVL ${anim.reqLevel} REQ",
                                                    fontSize = 8.5.sp,
                                                    color = if (isActive) Color.Black else if (isUnlocked) Color.White else Color.Gray,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                                // Fill missing slot if odd number
                                if (rowItems.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        CaptainBarnabyNpcCompanion(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
        )
    }
}
