package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.InventoryItem

data class ItemVisualSpec(
    val baseSymbol: String,
    val themeColor: Color,
    val badgeLabel: String? = null
)

fun getItemVisualSpec(itemId: String, itemName: String, fallbackEmoji: String): ItemVisualSpec {
    val lowerId = itemId.lowercase()
    val lowerName = itemName.lowercase()

    // 1. ORES - All ores use 🪨 with distinct color & badge
    if (lowerId.contains("ore") || lowerId == "item_coal_ore" || lowerId == "item_rune_essence" || lowerName.contains(" ore")) {
        val color = when {
            lowerId.contains("copper") || lowerName.contains("copper") -> Color(0xFFD97724) // Copper Orange
            lowerId.contains("tin") || lowerName.contains("tin") -> Color(0xFFB0BEC5) // Tin Silver
            lowerId.contains("iron") || lowerName.contains("iron") -> Color(0xFF90A4AE) // Iron Gray
            lowerId.contains("coal") || lowerName.contains("coal") -> Color(0xFF37474F) // Charcoal Black
            lowerId.contains("silver") || lowerName.contains("silver") -> Color(0xFFECEFF1) // Silver White
            lowerId.contains("gold") || lowerName.contains("gold") -> Color(0xFFFFC107) // Gold Yellow
            lowerId.contains("mithril") || lowerName.contains("mithril") -> Color(0xFF1E88E5) // Mithril Royal Blue
            lowerId.contains("adamant") || lowerName.contains("adamant") -> Color(0xFF4CAF50) // Adamant Emerald Green
            lowerId.contains("runite") || lowerId.contains("rune_ore") || lowerName.contains("runite") -> Color(0xFF00E5FF) // Runite Azure Cyan
            lowerId.contains("rune_essence") || lowerName.contains("essence") -> Color(0xFFAB47BC) // Essence Magic Purple
            else -> Color(0xFF8D6E63)
        }
        val label = when {
            lowerId.contains("copper") || lowerName.contains("copper") -> "Cu"
            lowerId.contains("tin") || lowerName.contains("tin") -> "Sn"
            lowerId.contains("iron") || lowerName.contains("iron") -> "Fe"
            lowerId.contains("coal") || lowerName.contains("coal") -> "C"
            lowerId.contains("silver") || lowerName.contains("silver") -> "Ag"
            lowerId.contains("gold") || lowerName.contains("gold") -> "Au"
            lowerId.contains("mithril") || lowerName.contains("mithril") -> "Mi"
            lowerId.contains("adamant") || lowerName.contains("adamant") -> "Ad"
            lowerId.contains("runite") || lowerName.contains("runite") -> "Ru"
            lowerId.contains("essence") || lowerName.contains("essence") -> "Ess"
            else -> "Ore"
        }
        return ItemVisualSpec(baseSymbol = "🪨", themeColor = color, badgeLabel = label)
    }

    // 2. BARS - All bars use 🧈 with distinct color & badge
    if ((lowerId.contains("bar") || lowerName.contains("bar")) &&
        !lowerId.contains("barrier") && !lowerId.contains("barrel") &&
        !lowerName.contains("barrier") && !lowerName.contains("barrel") && !lowerName.contains("barrow")) {
        val color = when {
            lowerId.contains("bronze") || lowerName.contains("bronze") -> Color(0xFFCD7F32) // Bronze
            lowerId.contains("iron") || lowerName.contains("iron") -> Color(0xFFB0BEC5) // Iron Gray
            lowerId.contains("steel") || lowerName.contains("steel") -> Color(0xFFCFD8DC) // Steel Bright
            lowerId.contains("silver") || lowerName.contains("silver") -> Color(0xFFFFFFFF) // Silver White
            lowerId.contains("gold") || lowerName.contains("gold") -> Color(0xFFFFD700) // Gold Yellow
            lowerId.contains("mithril") || lowerName.contains("mithril") -> Color(0xFF2196F3) // Mithril Blue
            lowerId.contains("adamant") || lowerName.contains("adamant") -> Color(0xFF388E3C) // Adamant Green
            lowerId.contains("rune") || lowerName.contains("rune") -> Color(0xFF00E5FF) // Rune Cyan
            else -> Color(0xFFBDBDBD)
        }
        val label = when {
            lowerId.contains("bronze") || lowerName.contains("bronze") -> "Br"
            lowerId.contains("iron") || lowerName.contains("iron") -> "Fe"
            lowerId.contains("steel") || lowerName.contains("steel") -> "St"
            lowerId.contains("silver") || lowerName.contains("silver") -> "Ag"
            lowerId.contains("gold") || lowerName.contains("gold") -> "Au"
            lowerId.contains("mithril") || lowerName.contains("mithril") -> "Mi"
            lowerId.contains("adamant") || lowerName.contains("adamant") -> "Ad"
            lowerId.contains("rune") || lowerName.contains("rune") -> "Ru"
            else -> "Bar"
        }
        return ItemVisualSpec(baseSymbol = "🧈", themeColor = color, badgeLabel = label)
    }

    // 3. LOGS - All logs use 🪵 with distinct color & badge
    if (lowerId.contains("logs") || lowerId == "item_logs" || lowerName.contains("log")) {
        val color = when {
            lowerId.contains("oak") || lowerName.contains("oak") -> Color(0xFF8D5B28) // Amber Oak
            lowerId.contains("willow") || lowerName.contains("willow") -> Color(0xFF688B58) // Willow Sage Green
            lowerId.contains("maple") || lowerName.contains("maple") -> Color(0xFFE65100) // Maple Orange Red
            lowerId.contains("yew") || lowerName.contains("yew") -> Color(0xFF2E7D32) // Yew Forest Green
            lowerId.contains("magic") || lowerName.contains("magic") -> Color(0xFF00B0FF) // Magic Cyan Blue
            lowerId.contains("redwood") || lowerName.contains("redwood") -> Color(0xFFB71C1C) // Redwood Crimson
            else -> Color(0xFFA17A4A) // Normal Log
        }
        val label = when {
            lowerId.contains("oak") || lowerName.contains("oak") -> "Oak"
            lowerId.contains("willow") || lowerName.contains("willow") -> "Wil"
            lowerId.contains("maple") || lowerName.contains("maple") -> "Map"
            lowerId.contains("yew") || lowerName.contains("yew") -> "Yew"
            lowerId.contains("magic") || lowerName.contains("magic") -> "Mag"
            lowerId.contains("redwood") || lowerName.contains("redwood") -> "Red"
            else -> "Log"
        }
        return ItemVisualSpec(baseSymbol = "🪵", themeColor = color, badgeLabel = label)
    }

    // 4. PLANKS - All planks use 🪚 with distinct color & badge
    if (lowerId.contains("plank") || lowerName.contains("plank")) {
        val color = when {
            lowerId.contains("oak") || lowerName.contains("oak") -> Color(0xFFB37222) // Oak Amber Plank
            lowerId.contains("teak") || lowerName.contains("teak") -> Color(0xFFA0522D) // Teak Reddish Wood
            lowerId.contains("mahogany") || lowerName.contains("mahogany") -> Color(0xFF6A1B0A) // Mahogany Deep Red
            else -> Color(0xFFD2B48C) // Pine Light Wood
        }
        val label = when {
            lowerId.contains("oak") || lowerName.contains("oak") -> "Oak"
            lowerId.contains("teak") || lowerName.contains("teak") -> "Teak"
            lowerId.contains("mahogany") || lowerName.contains("mahogany") -> "Mah"
            else -> "Plk"
        }
        return ItemVisualSpec(baseSymbol = "🪚", themeColor = color, badgeLabel = label)
    }

    // 5. TRAPS - All traps use 🪤 with distinct color & badge
    if (lowerId.contains("snare") || lowerId.contains("trap") || lowerId.contains("wand") || lowerId.contains("impling_net") ||
        lowerName.contains("snare") || lowerName.contains("trap") || lowerName.contains("wand") || lowerName.contains("impling net") || lowerName.contains("butterfly net")) {
        val color = when {
            lowerId.contains("bird_snare") || lowerName.contains("bird snare") -> Color(0xFF4CAF50) // Green Snare
            lowerId.contains("net_trap") || lowerName.contains("net trap") -> Color(0xFFFF9800) // Orange Rope Net
            lowerId.contains("box_trap") || lowerName.contains("box trap") -> Color(0xFF795548) // Mesh Brown Box
            lowerId.contains("noose_wand") || lowerName.contains("noose") -> Color(0xFFE91E63) // Magenta Wand
            lowerId.contains("impling") || lowerName.contains("impling") || lowerName.contains("butterfly") -> Color(0xFF9C27B0) // Violet Net
            else -> Color(0xFFFF5722)
        }
        val label = when {
            lowerId.contains("bird_snare") || lowerName.contains("bird snare") -> "Snare"
            lowerId.contains("net_trap") || lowerName.contains("net trap") -> "Net"
            lowerId.contains("box_trap") || lowerName.contains("box trap") -> "Box"
            lowerId.contains("noose_wand") || lowerName.contains("noose") -> "Wand"
            lowerId.contains("impling") || lowerName.contains("impling") -> "ImpNet"
            else -> "Trap"
        }
        return ItemVisualSpec(baseSymbol = "🪤", themeColor = color, badgeLabel = label)
    }

    // 6. TALISMANS - All talismans use 🧿 with distinct color & badge
    if (lowerId.contains("talisman") || lowerName.contains("talisman")) {
        val color = when {
            lowerId.contains("air") || lowerName.contains("air") -> Color(0xFFE0F7FA) // Light Cyan
            lowerId.contains("mind") || lowerName.contains("mind") -> Color(0xFFE1BEE7) // Soft Violet
            lowerId.contains("water") || lowerName.contains("water") -> Color(0xFF0288D1) // Deep Blue
            lowerId.contains("earth") || lowerName.contains("earth") -> Color(0xFF795548) // Earth Brown
            lowerId.contains("fire") || lowerName.contains("fire") -> Color(0xFFF44336) // Flame Red
            lowerId.contains("body") || lowerName.contains("body") -> Color(0xFF78909C) // Slate Gray
            lowerId.contains("cosmic") || lowerName.contains("cosmic") -> Color(0xFFFBC02D) // Star Gold
            lowerId.contains("chaos") || lowerName.contains("chaos") -> Color(0xFF512DA8) // Dark Purple
            lowerId.contains("nature") || lowerName.contains("nature") -> Color(0xFF388E3C) // Emerald Green
            lowerId.contains("law") || lowerName.contains("law") -> Color(0xFF1976D2) // Royal Blue
            lowerId.contains("death") || lowerName.contains("death") -> Color(0xFF212121) // Charcoal Black
            lowerId.contains("blood") || lowerName.contains("blood") -> Color(0xFFB71C1C) // Blood Crimson
            lowerId.contains("wrath") || lowerName.contains("wrath") -> Color(0xFFBF360C) // Lava Orange
            else -> Color(0xFF9E9E9E)
        }
        val label = when {
            lowerId.contains("air") || lowerName.contains("air") -> "Air"
            lowerId.contains("mind") || lowerName.contains("mind") -> "Mnd"
            lowerId.contains("water") || lowerName.contains("water") -> "Wtr"
            lowerId.contains("earth") || lowerName.contains("earth") -> "Eth"
            lowerId.contains("fire") || lowerName.contains("fire") -> "Fir"
            lowerId.contains("body") || lowerName.contains("body") -> "Bdy"
            lowerId.contains("cosmic") || lowerName.contains("cosmic") -> "Cos"
            lowerId.contains("chaos") || lowerName.contains("chaos") -> "Chs"
            lowerId.contains("nature") || lowerName.contains("nature") -> "Nat"
            lowerId.contains("law") || lowerName.contains("law") -> "Law"
            lowerId.contains("death") || lowerName.contains("death") -> "Dth"
            lowerId.contains("blood") || lowerName.contains("blood") -> "Bld"
            lowerId.contains("wrath") || lowerName.contains("wrath") -> "Wth"
            else -> "Tali"
        }
        return ItemVisualSpec(baseSymbol = "🧿", themeColor = color, badgeLabel = label)
    }

    // 7. SEEDS - All seeds use 🌱 with distinct color & badge
    if (lowerId.contains("seed") || lowerName.contains("seed") || lowerId.contains("spore")) {
        val color = when {
            lowerId.contains("potato") || lowerName.contains("potato") -> Color(0xFF8BC34A) // Potato Lime Green
            lowerId.contains("onion") || lowerName.contains("onion") -> Color(0xFFCDDC39) // Yellow Green
            lowerId.contains("cabbage") || lowerName.contains("cabbage") -> Color(0xFF4CAF50) // Cabbage Green
            lowerId.contains("tomato") || lowerName.contains("tomato") -> Color(0xFFF44336) // Tomato Red
            lowerId.contains("sweetcorn") || lowerName.contains("sweetcorn") -> Color(0xFFFFEB3B) // Corn Yellow
            lowerId.contains("strawberry") || lowerName.contains("strawberry") -> Color(0xFFE91E63) // Strawberry Pink
            lowerId.contains("watermelon") || lowerName.contains("watermelon") -> Color(0xFF2E7D32) // Watermelon Green
            lowerId.contains("sunleaf") || lowerName.contains("sunleaf") -> Color(0xFF009688) // Sunleaf Teal
            lowerId.contains("mystic_sage") || lowerName.contains("mystic_sage") -> Color(0xFF00838F) // Mystic Sage Cyan
            lowerId.contains("snape") || lowerName.contains("snape") -> Color(0xFF81C784) // Snape Sage
            lowerId.contains("oak") || lowerName.contains("oak") -> Color(0xFF8D5B28) // Oak Amber
            lowerId.contains("willow") || lowerName.contains("willow") -> Color(0xFF688B58) // Willow Olive
            lowerId.contains("maple") || lowerName.contains("maple") -> Color(0xFFE65100) // Maple Orange
            lowerId.contains("yew") || lowerName.contains("yew") -> Color(0xFF1B5E20) // Yew Dark Green
            lowerId.contains("magic") || lowerName.contains("magic") -> Color(0xFF00B0FF) // Magic Blue
            lowerId.contains("spirit") || lowerName.contains("spirit") -> Color(0xFF9C27B0) // Spirit Purple
            else -> Color(0xFF4CAF50)
        }
        val label = when {
            lowerId.contains("potato") || lowerName.contains("potato") -> "Pot"
            lowerId.contains("onion") || lowerName.contains("onion") -> "Oni"
            lowerId.contains("cabbage") || lowerName.contains("cabbage") -> "Cab"
            lowerId.contains("tomato") || lowerName.contains("tomato") -> "Tom"
            lowerId.contains("sweetcorn") || lowerName.contains("sweetcorn") -> "Crn"
            lowerId.contains("strawberry") || lowerName.contains("strawberry") -> "Str"
            lowerId.contains("watermelon") || lowerName.contains("watermelon") -> "Mel"
            lowerId.contains("sunleaf") || lowerName.contains("sunleaf") -> "Sun"
            lowerId.contains("mystic_sage") || lowerName.contains("mystic_sage") -> "Mys"
            lowerId.contains("snape") || lowerName.contains("snape") -> "Snp"
            lowerId.contains("oak") || lowerName.contains("oak") -> "Oak"
            lowerId.contains("willow") || lowerName.contains("willow") -> "Wil"
            lowerId.contains("maple") || lowerName.contains("maple") -> "Map"
            lowerId.contains("yew") || lowerName.contains("yew") -> "Yew"
            lowerId.contains("magic") || lowerName.contains("magic") -> "Mag"
            lowerId.contains("spirit") || lowerName.contains("spirit") -> "Spi"
            else -> "Seed"
        }
        return ItemVisualSpec(baseSymbol = "🌱", themeColor = color, badgeLabel = label)
    }

    // 8. RUNES - All magic runes use 🔮 with distinct theme color & badge (like logs and planks)
    val isRune = (lowerId.contains("rune_") || lowerName.contains("rune")) &&
            !lowerId.contains("rune_axe") && !lowerId.contains("rune_bar") && !lowerId.contains("rune_essence") &&
            !lowerId.contains("rune_ore") && !lowerId.contains("rune_scimitar") && !lowerId.contains("rune_sword") &&
            !lowerId.contains("rune_platebody") && !lowerId.contains("rune_nails") && !lowerId.contains("rune_armour") &&
            !lowerName.contains("axe") && !lowerName.contains("bar") && !lowerName.contains("essence") &&
            !lowerName.contains("sword") && !lowerName.contains("scimitar") && !lowerName.contains("platebody") &&
            !lowerName.contains("armour") && !lowerName.contains("pickaxe") && !lowerName.contains("dagger")

    if (isRune) {
        val color = when {
            lowerId.contains("air") || lowerName.contains("air") -> Color(0xFF81D4FA) // Light Air Blue
            lowerId.contains("mind") || lowerName.contains("mind") -> Color(0xFFE1BEE7) // Soft Lavender
            lowerId.contains("water") || lowerName.contains("water") -> Color(0xFF0288D1) // Ocean Blue
            lowerId.contains("earth") || lowerName.contains("earth") -> Color(0xFF6D4C41) // Earth Brown
            lowerId.contains("fire") || lowerName.contains("fire") -> Color(0xFFF44336) // Fiery Red
            lowerId.contains("body") || lowerName.contains("body") -> Color(0xFF546E7A) // Body Gray Blue
            lowerId.contains("cosmic") || lowerName.contains("cosmic") -> Color(0xFFFFD54F) // Cosmic Gold
            lowerId.contains("chaos") || lowerName.contains("chaos") -> Color(0xFFAB47BC) // Chaos Purple
            lowerId.contains("nature") || lowerName.contains("nature") -> Color(0xFF388E3C) // Emerald Nature Green
            lowerId.contains("law") || lowerName.contains("law") -> Color(0xFF1976D2) // Cobalt Law Blue
            lowerId.contains("death") || lowerName.contains("death") -> Color(0xFF37474F) // Dark Charcoal
            lowerId.contains("astral") || lowerName.contains("astral") -> Color(0xFFF48FB1) // Astral Pink
            lowerId.contains("blood") || lowerName.contains("blood") -> Color(0xFFB71C1C) // Crimson Red
            lowerId.contains("soul") || lowerName.contains("soul") -> Color(0xFF00ACC1) // Soul Teal
            lowerId.contains("wrath") || lowerName.contains("wrath") -> Color(0xFFE65100) // Wrath Orange
            else -> Color(0xFF9E9E9E)
        }
        val label = when {
            lowerId.contains("air") || lowerName.contains("air") -> "Air"
            lowerId.contains("mind") || lowerName.contains("mind") -> "Mnd"
            lowerId.contains("water") || lowerName.contains("water") -> "Wtr"
            lowerId.contains("earth") || lowerName.contains("earth") -> "Eth"
            lowerId.contains("fire") || lowerName.contains("fire") -> "Fir"
            lowerId.contains("body") || lowerName.contains("body") -> "Bdy"
            lowerId.contains("cosmic") || lowerName.contains("cosmic") -> "Cos"
            lowerId.contains("chaos") || lowerName.contains("chaos") -> "Chs"
            lowerId.contains("nature") || lowerName.contains("nature") -> "Nat"
            lowerId.contains("law") || lowerName.contains("law") -> "Law"
            lowerId.contains("death") || lowerName.contains("death") -> "Dth"
            lowerId.contains("astral") || lowerName.contains("astral") -> "Ast"
            lowerId.contains("blood") || lowerName.contains("blood") -> "Bld"
            lowerId.contains("soul") || lowerName.contains("soul") -> "Soul"
            lowerId.contains("wrath") || lowerName.contains("wrath") -> "Wrth"
            else -> "Rune"
        }
        return ItemVisualSpec(baseSymbol = "🔮", themeColor = color, badgeLabel = label)
    }

    // 9. RAW FOOD / UNCOOKED FISH - Uses brown / earthy raw tone & RAW badge
    val isRaw = lowerId.contains("raw_") || lowerId.startsWith("item_raw_") ||
            lowerName.startsWith("raw ", ignoreCase = true) || lowerName.contains("uncooked", ignoreCase = true)

    if (isRaw) {
        val baseSymbol = when {
            lowerId.contains("meat") || lowerName.contains("meat") || lowerId.contains("chicken") || lowerName.contains("chicken") -> "🥩"
            lowerId.contains("shark") || lowerName.contains("shark") -> "🦈"
            lowerId.contains("lobster") || lowerName.contains("lobster") -> "🦞"
            lowerId.contains("shrimp") || lowerName.contains("shrimp") -> "🦐"
            else -> "🐟"
        }
        // Earthy muddy brown tone for raw food
        val rawBrownColor = Color(0xFF6D4C41)
        return ItemVisualSpec(baseSymbol = baseSymbol, themeColor = rawBrownColor, badgeLabel = "RAW")
    }

    // 10. COOKED FOOD / MEALS - Uses golden-roasted brown & COOKED badge
    val isCooked = lowerId.contains("cooked") || lowerId.startsWith("item_cooked_") ||
            lowerName.contains("cooked", ignoreCase = true) || lowerName.contains("baked", ignoreCase = true) ||
            lowerName.contains("roasted", ignoreCase = true) || lowerId == "item_bread" ||
            lowerId == "item_trout" || lowerId == "item_salmon" || lowerId == "item_lobster" ||
            lowerId == "item_swordfish" || lowerId == "item_shark" || lowerId == "item_manta_ray" ||
            lowerId.contains("stew") || lowerId.contains("pie") || lowerId.contains("cake") || lowerId.contains("soup")

    if (isCooked) {
        val baseSymbol = when {
            lowerId.contains("bread") || lowerName.contains("bread") -> "🍞"
            lowerId.contains("pie") || lowerName.contains("pie") -> "🥧"
            lowerId.contains("cake") || lowerName.contains("cake") -> "🍰"
            lowerId.contains("stew") || lowerName.contains("stew") || lowerId.contains("soup") || lowerName.contains("soup") -> "🍲"
            lowerId.contains("lobster") || lowerName.contains("lobster") -> "🦞"
            lowerId.contains("shark") || lowerName.contains("shark") -> "🦈"
            else -> "🍖"
        }
        // Roasted golden-brown theme color for cooked food
        val cookedGoldColor = Color(0xFFD35400)
        return ItemVisualSpec(baseSymbol = baseSymbol, themeColor = cookedGoldColor, badgeLabel = "🔥COOKED")
    }

    // Default for all other items
    return ItemVisualSpec(baseSymbol = fallbackEmoji, themeColor = Color.Transparent, badgeLabel = null)
}

@Composable
fun OsrsItemIcon(
    itemId: String,
    itemName: String,
    fallbackEmoji: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    showBadge: Boolean = true
) {
    val spec = getItemVisualSpec(itemId, itemName, fallbackEmoji)

    if (spec.themeColor == Color.Transparent) {
        Text(text = spec.baseSymbol, fontSize = fontSize, modifier = modifier)
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            // Soft colored background circle halo
            Box(
                modifier = Modifier
                    .size((fontSize.value * 1.35).dp)
                    .clip(CircleShape)
                    .background(spec.themeColor.copy(alpha = 0.28f))
                    .border(1.dp, spec.themeColor.copy(alpha = 0.85f), CircleShape)
            )

            // Base uniform symbol
            Text(
                text = spec.baseSymbol,
                fontSize = fontSize,
                color = Color.Unspecified
            )

            // Badge indicator
            if (showBadge && spec.badgeLabel != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-3).dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(spec.themeColor)
                        .border(0.5.dp, Color.Black, RoundedCornerShape(3.dp))
                        .padding(horizontal = 2.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        text = spec.badgeLabel,
                        fontSize = 7.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OsrsItemIcon(
    item: InventoryItem,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    showBadge: Boolean = true
) {
    OsrsItemIcon(
        itemId = item.id,
        itemName = item.name,
        fallbackEmoji = item.iconEmoji,
        modifier = modifier,
        fontSize = fontSize,
        showBadge = showBadge
    )
}
