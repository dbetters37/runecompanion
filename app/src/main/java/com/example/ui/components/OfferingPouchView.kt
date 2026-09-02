package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.DefaultItems
import com.example.data.models.InventoryItem
import com.example.data.models.ItemCategory
import com.example.data.models.OsrsSkill
import com.example.ui.theme.*
import kotlin.math.sin

enum class OfferingRarity(
    val displayName: String,
    val color: Color,
    val glowColor: Color,
    val xpMultiplier: Double,
    val gpPerItem: Long
) {
    MYTHIC("Mythic", Color(0xFFFF4081), Color(0xFFFF80AB), 4.5, 500L),
    LEGENDARY("Legendary", Color(0xFFFFD700), Color(0xFFFFEA00), 3.0, 300L),
    EPIC("Epic", Color(0xFFBA68C8), Color(0xFFE040FB), 2.2, 180L),
    RARE("Rare", Color(0xFF00E5FF), Color(0xFF80D8FF), 1.6, 100L),
    UNCOMMON("Uncommon", Color(0xFF69F0AE), Color(0xFFB9F6CA), 1.2, 50L),
    COMMON("Common", Color(0xFFBDBDBD), Color(0xFFE0E0E0), 1.0, 20L)
}

enum class MysticalCategory(val label: String, val iconEmoji: String) {
    ALL("All", "🌐"),
    GEMS_ORES("Gems & Ores", "🪨"),
    SACRED_WOODS("Sacred Woods", "🌲"),
    SPIRIT_FISH("Spirit Fish", "🐟"),
    MYSTIC_HERBS("Herbs & Elixirs", "🌿"),
    RELICS_BONES("Relics & Talismans", "🔮"),
    RUNES_MAGIC("Runes & Essences", "✨")
}

data class MysticalOfferingSpec(
    val itemId: String,
    val name: String,
    val category: MysticalCategory,
    val rarity: OfferingRarity,
    val baseSpiritXp: Long,
    val baseMagicXp: Long,
    val baseGp: Long,
    val defaultEmoji: String,
    val mysticalFlavor: String
)

object OfferingPouchRegistry {
    fun getSpec(itemId: String, itemName: String): MysticalOfferingSpec {
        val normId = DefaultItems.normalizeItemId(itemId).lowercase()
        val lowerName = itemName.lowercase()

        // 1. GEMS & ORES
        if (normId.contains("godstone") || normId.contains("zenyte") || normId.contains("onyx")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.GEMS_ORES, OfferingRarity.MYTHIC, 800L, 600L, 800L, "💎", "Priceless celestial core vibrating with primordial energy.")
        }
        if (normId.contains("starfire") || normId.contains("dragonstone") || normId.contains("spirit_crystal")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.GEMS_ORES, OfferingRarity.LEGENDARY, 450L, 350L, 400L, "✨", "Glows with captured starlight and spirit resonance.")
        }
        if (normId.contains("diamond") || normId.contains("void_shard") || normId.contains("runite_ore")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.GEMS_ORES, OfferingRarity.EPIC, 280L, 200L, 250L, "💎", "Deep subterranean crystal charged with arcane power.")
        }
        if (normId.contains("ruby") || normId.contains("emerald") || normId.contains("adamant")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.GEMS_ORES, OfferingRarity.RARE, 160L, 120L, 120L, "💎", "Pure polished gemstone radiating mystical heat.")
        }
        if (normId.contains("sapphire") || normId.contains("mithril") || normId.contains("gold_ore") || normId.contains("silver_ore")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.GEMS_ORES, OfferingRarity.UNCOMMON, 90L, 70L, 60L, "🪨", "Gleaming mineral imbued with earth spirits.")
        }
        if (normId.contains("ore") || normId.contains("coal")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.GEMS_ORES, OfferingRarity.COMMON, 35L, 20L, 20L, "🪨", "Raw subterranean mineral ready for offering.")
        }

        // 2. SACRED WOODS
        if (normId.contains("elder_heartwood") || normId.contains("soul_sapling")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.SACRED_WOODS, OfferingRarity.MYTHIC, 750L, 500L, 650L, "🌲", "Ancient heartwood blessed by elder grove spirits.")
        }
        if (normId.contains("astral_wood") || normId.contains("redwood_logs")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.SACRED_WOODS, OfferingRarity.LEGENDARY, 400L, 300L, 350L, "🪵", "Massive sacred timber steeped in celestial resin.")
        }
        if (normId.contains("magic_logs") || normId.contains("mystic_bark")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.SACRED_WOODS, OfferingRarity.EPIC, 250L, 220L, 200L, "🪵", "Infused with raw ambient wizardry and arcane sap.")
        }
        if (normId.contains("yew_logs")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.SACRED_WOODS, OfferingRarity.RARE, 150L, 100L, 100L, "🪵", "Dense holy wood revered by druidic circles.")
        }
        if (normId.contains("maple_logs") || normId.contains("willow_logs")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.SACRED_WOODS, OfferingRarity.UNCOMMON, 70L, 50L, 45L, "🪵", "Resinous grove timber that burns with a clean flame.")
        }
        if (normId.contains("logs") || normId.contains("stick") || normId.contains("plank")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.SACRED_WOODS, OfferingRarity.COMMON, 30L, 20L, 15L, "🪵", "Forest wood suitable for humble shaman offerings.")
        }

        // 3. SPIRIT FISH
        if (normId.contains("celestial_serpent") || normId.contains("void_kraken")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.SPIRIT_FISH, OfferingRarity.MYTHIC, 850L, 700L, 900L, "🐉", "Mythical leviathan harvested from the astral deep.")
        }
        if (normId.contains("astral_whale") || normId.contains("luminous_angler")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.SPIRIT_FISH, OfferingRarity.LEGENDARY, 500L, 400L, 450L, "🐳", "Bioluminescent creature that swims through spirit currents.")
        }
        if (normId.contains("golden_carp") || normId.contains("shark")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.SPIRIT_FISH, OfferingRarity.EPIC, 300L, 220L, 250L, "🦈", "Majestic aquatic beast prized by high shamans.")
        }
        if (normId.contains("shamanic_bass") || normId.contains("swordfish")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.SPIRIT_FISH, OfferingRarity.RARE, 180L, 130L, 130L, "🐟", "Spiritual fish pulsing with natural vitality.")
        }
        if (normId.contains("lobster") || normId.contains("salmon")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.SPIRIT_FISH, OfferingRarity.UNCOMMON, 95L, 65L, 60L, "🦞", "Fresh catch carrying the blessings of the river.")
        }
        if (normId.contains("trout") || normId.contains("sardine") || normId.contains("shrimp") || normId.contains("fish") || normId.contains("raw_")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.SPIRIT_FISH, OfferingRarity.COMMON, 40L, 25L, 20L, "🐟", "Simple river offering for companion spirits.")
        }

        // 4. HERBS & ELIXIRS
        if (normId.contains("saradomin_brew") || normId.contains("mystic_sage") || normId.contains("vervain")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.MYSTIC_HERBS, OfferingRarity.MYTHIC, 600L, 500L, 700L, "🧪", "Supreme divine concoction of unparalleled purity.")
        }
        if (normId.contains("super_restore") || normId.contains("chamomile") || normId.contains("moonflower")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.MYSTIC_HERBS, OfferingRarity.LEGENDARY, 380L, 300L, 380L, "🧪", "Highly potent spiritual restorative herbs and elixirs.")
        }
        if (normId.contains("prayer_potion") || normId.contains("silverleaf") || normId.contains("elderberry")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.MYSTIC_HERBS, OfferingRarity.EPIC, 240L, 180L, 200L, "🧪", "Enriched herbal brew that replenishes holy energy.")
        }
        if (normId.contains("wintergreen") || normId.contains("ironleaf") || normId.contains("sunleaf") || normId.contains("potion")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.MYSTIC_HERBS, OfferingRarity.RARE, 140L, 100L, 100L, "🌿", "Medicinal flora harvested during full moon.")
        }
        if (normId.contains("meadow_mint") || normId.contains("wild_thyme") || normId.contains("lavender") || normId.contains("seed")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.MYSTIC_HERBS, OfferingRarity.UNCOMMON, 75L, 50L, 50L, "🌱", "Fragrant botanical leaves loved by forest familiars.")
        }
        if (normId.contains("greenleaf") || normId.contains("vial") || normId.contains("herb")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.MYSTIC_HERBS, OfferingRarity.COMMON, 30L, 20L, 15L, "🌿", "Basic cleansing herb for simple altar fumigation.")
        }

        // 5. RELICS & BONES
        if (normId.contains("shamanic_relic") || normId.contains("ancient_talisman") || normId.contains("spirit_seed")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.RELICS_BONES, OfferingRarity.LEGENDARY, 550L, 450L, 500L, "🔮", "Hallowed tribal talisman brimming with ancestral spirits.")
        }
        if (normId.contains("dragon_bones") || normId.contains("doubloons") || normId.contains("treasure")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.RELICS_BONES, OfferingRarity.EPIC, 350L, 250L, 300L, "🦴", "Powerful draconic remains that grant grand favor.")
        }
        if (normId.contains("big_bones") || normId.contains("bone_shard")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.RELICS_BONES, OfferingRarity.RARE, 180L, 120L, 120L, "🦴", "Substantial offering yielding high Magic & Spirit dedication.")
        }
        if (normId.contains("bones") || normId.contains("ashes") || normId.contains("pouch")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.RELICS_BONES, OfferingRarity.UNCOMMON, 60L, 40L, 40L, "🦴", "Sacred remains buried to appease restless wandering spirits.")
        }

        // 6. RUNES & ESSENCES
        if (normId.contains("rune_soul") || normId.contains("rune_blood") || normId.contains("rune_death") || normId.contains("rune_wrath")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.RUNES_MAGIC, OfferingRarity.EPIC, 280L, 320L, 200L, "✨", "High tier dark/soul energy crystallized in runic form.")
        }
        if (normId.contains("rune_astral") || normId.contains("rune_nature") || normId.contains("rune_law") || normId.contains("rune_chaos")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.RUNES_MAGIC, OfferingRarity.RARE, 150L, 180L, 100L, "✨", "Celestial harmonic runes used for shamanic rites.")
        }
        if (normId.contains("rune_mind") || normId.contains("rune_body") || normId.contains("rune_cosmic")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.RUNES_MAGIC, OfferingRarity.UNCOMMON, 70L, 90L, 40L, "✨", "Elemental focus crystal carrying psychic energy.")
        }
        if (normId.contains("rune_") || normId.contains("essence")) {
            return MysticalOfferingSpec(normId, itemName, MysticalCategory.RUNES_MAGIC, OfferingRarity.COMMON, 35L, 50L, 20L, "✨", "Pure elemental rune stone ready for transmutation.")
        }

        // DEFAULT FALLBACK
        return MysticalOfferingSpec(normId, itemName, MysticalCategory.ALL, OfferingRarity.COMMON, 25L, 20L, 15L, "✨", "A curious mystical curiosity collected on your journey.")
    }
}

@Composable
fun OfferingPouchView(
    inventoryItems: List<InventoryItem>,
    bankItems: List<InventoryItem>,
    coinsGp: Long,
    onOfferItem: (InventoryItem, Int) -> Unit,
    onDepositToBank: (InventoryItem, Int) -> Unit,
    onWithdrawFromBank: (InventoryItem, Int) -> Unit,
    onBatchOfferCategory: (MysticalCategory) -> Unit,
    onBatchOfferAll: () -> Unit,
    onTransmuteItem: (InventoryItem, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(MysticalCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("Rarity") } // "Rarity", "Quantity", "XP", "Name"
    var selectedOfferingItem by remember { mutableStateOf<InventoryItem?>(null) }
    var showGrandSacrificeDialog by remember { mutableStateOf(false) }

    // Combine & prepare offering items from both inventory and bank
    val combinedPouchItems = remember(inventoryItems, bankItems) {
        val map = mutableMapOf<String, InventoryItem>()
        (inventoryItems + bankItems).filter { it.quantity > 0 }.forEach { item ->
            val curr = map[item.id]
            if (curr != null) {
                map[item.id] = curr.copy(quantity = curr.quantity + item.quantity)
            } else {
                map[item.id] = item
            }
        }
        map.values.toList()
    }

    val offeringList = remember(combinedPouchItems, selectedCategory, searchQuery, sortBy) {
        val list = combinedPouchItems.map { item ->
            val spec = OfferingPouchRegistry.getSpec(item.id, item.name)
            item to spec
        }.filter { (item, spec) ->
            val matchCat = selectedCategory == MysticalCategory.ALL || spec.category == selectedCategory
            val matchSearch = searchQuery.isBlank() || 
                item.name.contains(searchQuery, ignoreCase = true) || 
                spec.rarity.displayName.contains(searchQuery, ignoreCase = true) ||
                spec.category.label.contains(searchQuery, ignoreCase = true)
            matchCat && matchSearch
        }

        when (sortBy) {
            "Quantity" -> list.sortedByDescending { it.first.quantity }
            "XP" -> list.sortedByDescending { (it.second.baseSpiritXp + it.second.baseMagicXp) * it.first.quantity }
            "Name" -> list.sortedBy { it.first.name }
            else -> list.sortedByDescending { it.second.rarity.ordinal } // Rarity default
        }
    }

    val totalMysticalCount = remember(combinedPouchItems) {
        combinedPouchItems.sumOf { it.quantity }
    }
    val totalUniqueTypes = remember(combinedPouchItems) {
        combinedPouchItems.size
    }
    val totalEstimatedOfferingXp = remember(combinedPouchItems) {
        combinedPouchItems.sumOf { item ->
            val spec = OfferingPouchRegistry.getSpec(item.id, item.name)
            (spec.baseSpiritXp + spec.baseMagicXp) * item.quantity
        }
    }
    val totalEstimatedGp = remember(combinedPouchItems) {
        combinedPouchItems.sumOf { item ->
            val spec = OfferingPouchRegistry.getSpec(item.id, item.name)
            spec.baseGp * item.quantity
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pouch_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1F1610),
                        Color(0xFF140D08),
                        Color(0xFF0D0805)
                    )
                )
            )
            .weatheredStoneBorder(cornerRadius = 12.dp)
            .padding(12.dp)
    ) {
        // --- HEADER BANNER WITH POUCH RESONANCE ---
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1D14)),
            border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(OsrsGold, Color(0xFF9C27B0), OsrsGold))),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("offering_pouch_header_card")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Color(0xFF9C27B0), Color(0xFF3E1C54))))
                                .border(1.5.dp, OsrsGold, CircleShape)
                                .scale(pulseGlow)
                        ) {
                            Text("👝", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "👝 Offering Pouch",
                                color = OsrsGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Sacred Altar of Resource Dedication",
                                color = Color(0xFFE1BEE7),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Total GP Badge
                    Surface(
                        color = Color(0xFF1F120A),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF3E2723))
                    ) {
                        Text(
                            text = "🪙 $coinsGp GP",
                            color = OsrsTextYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stats Dashboard Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1C130D))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Mystical Items", color = Color(0xFFB0BEC5), fontSize = 10.sp)
                        Text("$totalUniqueTypes Stacks • $totalMysticalCount Qty", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier.height(20.dp).width(1.dp), color = Color(0xFF4E342E))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Est. Blessing XP", color = Color(0xFFB0BEC5), fontSize = 10.sp)
                        Text("✨ +$totalEstimatedOfferingXp XP", color = Color(0xFFE040FB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier.height(20.dp).width(1.dp), color = Color(0xFF4E342E))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Est. Spirit GP", color = Color(0xFFB0BEC5), fontSize = 10.sp)
                        Text("🪙 +$totalEstimatedGp GP", color = OsrsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Master Batch Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showGrandSacrificeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("grand_sacrifice_button")
                    ) {
                        Text("🔥 Grand Sacrifice", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                    }

                    OutlinedButton(
                        onClick = { onBatchOfferCategory(selectedCategory) },
                        border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("offer_category_button")
                    ) {
                        Text("✨ Offer ${selectedCategory.label}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF80D8FF), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- CATEGORY FILTER TABS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MysticalCategory.entries.forEach { cat ->
                val isSelected = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFF6A1B9A) else Color(0xFF271910),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFFE040FB) else Color(0xFF4E342E)),
                    modifier = Modifier
                        .clickable { selectedCategory = cat }
                        .testTag("cat_chip_${cat.name.lowercase()}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(cat.iconEmoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = cat.label,
                            color = if (isSelected) Color.White else Color(0xFFD7CCC8),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- SEARCH & SORT CONTROLS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search mystical items...", fontSize = 11.sp, color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OsrsGold,
                    unfocusedBorderColor = Color(0xFF4E342E),
                    focusedContainerColor = Color(0xFF1E130B),
                    unfocusedContainerColor = Color(0xFF1E130B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("offering_search_input")
            )

            // Sort Toggle Button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2B1D14),
                border = BorderStroke(1.dp, Color(0xFF3E2723)),
                modifier = Modifier
                    .height(48.dp)
                    .clickable {
                        sortBy = when (sortBy) {
                            "Rarity" -> "Quantity"
                            "Quantity" -> "XP"
                            "XP" -> "Name"
                            else -> "Rarity"
                        }
                    }
                    .testTag("offering_sort_toggle")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Text("🔄", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sort: $sortBy",
                        color = OsrsGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- VISUAL GRID OF MYSTICAL ITEMS (NO TEXT LISTS!) ---
        if (offeringList.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1C120A))
                    .border(1.dp, Color(0xFF3E2723), RoundedCornerShape(8.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👝", fontSize = 48.sp, modifier = Modifier.scale(pulseGlow))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No Mystical Offerings Found",
                        color = OsrsGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gather mystical resources from AFK Woodcutting, Quarry Mining, Spirit Pool Fishing, or Farming to fill your pouch!",
                        color = Color(0xFFB0BEC5),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("offering_items_grid")
            ) {
                items(offeringList, key = { it.first.id }) { (item, spec) ->
                    MysticalOfferingCard(
                        item = item,
                        spec = spec,
                        onClick = { selectedOfferingItem = item },
                        onQuickOfferOne = { onOfferItem(item, 1) },
                        onQuickOfferAll = { onOfferItem(item, item.quantity) },
                        onDepositBank = { onDepositToBank(item, item.quantity) },
                        onTransmute = { onTransmuteItem(item, 1) }
                    )
                }
            }
        }
    }

    // Detail / Action Modal for Selected Item
    selectedOfferingItem?.let { item ->
        val spec = OfferingPouchRegistry.getSpec(item.id, item.name)
        OfferingItemDetailDialog(
            item = item,
            spec = spec,
            onDismiss = { selectedOfferingItem = null },
            onOfferQty = { qty ->
                onOfferItem(item, qty)
                selectedOfferingItem = null
            },
            onDepositBank = { qty ->
                onDepositToBank(item, qty)
                selectedOfferingItem = null
            },
            onTransmute = { qty ->
                onTransmuteItem(item, qty)
                selectedOfferingItem = null
            }
        )
    }

    // Grand Sacrifice Confirmation Dialog
    if (showGrandSacrificeDialog) {
        AlertDialog(
            onDismissRequest = { showGrandSacrificeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Grand Spirit Sacrifice", color = OsrsGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to offer ALL $totalMysticalCount mystical items in your pouch?",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFF26180E),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF3E2723)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Total Anticipated Rewards:", color = Color(0xFFE040FB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("✨ +$totalEstimatedOfferingXp Spirit & Magic XP", color = Color(0xFF80D8FF), fontSize = 11.sp)
                            Text("🪙 +$totalEstimatedGp Gold Coins", color = OsrsGold, fontSize = 11.sp)
                            Text("💖 +25 Companion Happiness Boost", color = Color(0xFFFF80AB), fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBatchOfferAll()
                        showGrandSacrificeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                ) {
                    Text("🔥 Sacrifice All", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGrandSacrificeDialog = false }) {
                    Text("Cancel", color = Color(0xFFB0BEC5), fontSize = 12.sp)
                }
            },
            containerColor = Color(0xFF1E130B),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun MysticalOfferingCard(
    item: InventoryItem,
    spec: MysticalOfferingSpec,
    onClick: () -> Unit,
    onQuickOfferOne: () -> Unit,
    onQuickOfferAll: () -> Unit,
    onDepositBank: () -> Unit,
    onTransmute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF24170E)),
        border = BorderStroke(1.5.dp, spec.rarity.color.copy(alpha = 0.8f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("offering_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Top Row: Rarity Badge & Stack Quantity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = spec.rarity.color.copy(alpha = 0.2f),
                    border = BorderStroke(0.8.dp, spec.rarity.color)
                ) {
                    Text(
                        text = spec.rarity.displayName.uppercase(),
                        color = spec.rarity.color,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF140D07),
                    border = BorderStroke(0.8.dp, Color(0xFF4E342E))
                ) {
                    Text(
                        text = "x${item.quantity}",
                        color = OsrsTextYellow,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Center: Icon with mystical aura glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                spec.rarity.glowColor.copy(alpha = 0.3f),
                                Color(0xFF140D07)
                            )
                        )
                    )
                    .border(1.dp, spec.rarity.color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            ) {
                OsrsItemIcon(
                    itemId = item.id,
                    itemName = item.name,
                    fallbackEmoji = spec.defaultEmoji,
                    fontSize = 30.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Item Name
            Text(
                text = item.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Offering XP & GP Value
            val totalSpiritXp = (spec.baseSpiritXp * spec.rarity.xpMultiplier).toLong()
            val totalMagicXp = (spec.baseMagicXp * spec.rarity.xpMultiplier).toLong()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "✨ +${totalSpiritXp + totalMagicXp} XP",
                    color = Color(0xFFE040FB),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "🪙 ${spec.baseGp} GP",
                    color = OsrsGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = onQuickOfferOne,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                ) {
                    Text("✨ 1", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onQuickOfferAll,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                ) {
                    Text("🔥 All", fontSize = 9.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDepositBank,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2723)),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                ) {
                    Text("🏦", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun OfferingItemDetailDialog(
    item: InventoryItem,
    spec: MysticalOfferingSpec,
    onDismiss: () -> Unit,
    onOfferQty: (Int) -> Unit,
    onDepositBank: (Int) -> Unit,
    onTransmute: (Int) -> Unit
) {
    var offerCount by remember { mutableIntStateOf(1.coerceAtMost(item.quantity)) }
    val totalXp = remember(offerCount, spec) {
        ((spec.baseSpiritXp + spec.baseMagicXp) * spec.rarity.xpMultiplier * offerCount).toLong()
    }
    val totalGp = remember(offerCount, spec) {
        (spec.baseGp * offerCount)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OsrsItemIcon(
                        itemId = item.id,
                        itemName = item.name,
                        fallbackEmoji = spec.defaultEmoji,
                        fontSize = 26.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(item.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(spec.rarity.displayName + " • " + spec.category.label, color = spec.rarity.color, fontSize = 10.sp)
                    }
                }
            }
        },
        text = {
            Column {
                Text(
                    text = spec.mysticalFlavor,
                    color = Color(0xFFD7CCC8),
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity Slider / Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Offering Quantity:", color = OsrsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { if (offerCount > 1) offerCount-- },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2723)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("-", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$offerCount / ${item.quantity}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { if (offerCount < item.quantity) offerCount++ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E2723)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { offerCount = item.quantity },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E342E)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Max", color = OsrsGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rewards summary card
                Surface(
                    color = Color(0xFF1C120A),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, spec.rarity.color.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Offering Rewards for x$offerCount:", color = Color(0xFFE040FB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("✨ Total Spirit & Magic XP:", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                            Text("+$totalXp XP", color = Color(0xFF80D8FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🪙 Gold Coins:", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                            Text("+$totalGp GP", color = OsrsGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("💖 Companion Joy:", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                            Text("+${(offerCount * 2).coerceAtMost(20)} Happiness", color = Color(0xFFFF80AB), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onOfferQty(offerCount) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
            ) {
                Text("✨ Offer x$offerCount", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = { onDepositBank(offerCount) },
                    border = BorderStroke(1.dp, Color(0xFF8D6E63))
                ) {
                    Text("🏦 Bank", color = Color(0xFFD7CCC8), fontSize = 11.sp)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color.Gray, fontSize = 11.sp)
                }
            }
        },
        containerColor = Color(0xFF22160E),
        shape = RoundedCornerShape(12.dp)
    )
}
