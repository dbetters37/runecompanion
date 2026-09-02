package com.example.data.models

/**
 * Shaman Village NPC data model representing a villager of the Shaman Tribe.
 * Each NPC has up to 3 distinct types of favors they can ask the player for.
 */
data class ShamanVillagerNpc(
    val id: String,
    val name: String,
    val title: String,
    val role: String,
    val avatarEmoji: String,
    val hutLocation: String,
    val description: String,
    val greeting: String,
    val completionQuote: String,
    /**
     * Up to 3 distinct favor types (represented by OsrsSkill activities).
     * Strictly enforced: NPCs will NEVER ask for random favors outside their 3 specialties.
     */
    val favoredActivities: List<OsrsSkill>,
    /** Human-readable labels for their 3 favor types */
    val favorTypeLabels: List<String>,
    val defaultIcon: String,
    val completedFavorsCount: Int = 0,
    val affinityXp: Long = 0L
)

object NpcData {

    /**
     * Comprehensive Database of Shaman Tribe Villagers.
     * Each NPC has up to 3 specific favor specialties.
     */
    val VILLAGERS: List<ShamanVillagerNpc> = listOf(
        ShamanVillagerNpc(
            id = "npc_arlg",
            name = "Afrig",
            title = "The Forgefather",
            role = "Tribe Blacksmith",
            avatarEmoji = "⚒️",
            hutLocation = "Village Forge & Anvil",
            description = "Master metalworker of the tribe. Afrig crafts durable tools, hunting spearheads, forged nails, and structural iron brackets for the village.",
            greeting = "May the ancestral spirits guide your hammer! The village needs ore mined and sturdy iron forged.",
            completionQuote = "Fine craftsmanship! You have the spirit of a true metalshaper in your veins.",
            favoredActivities = listOf(OsrsSkill.SMITHING, OsrsSkill.CONSTRUCTION, OsrsSkill.ADVENTURING),
            favorTypeLabels = listOf("Ore Mining & Forging", "Hut Keeping (Nails & Brackets)", "Relic & Tool Crafting"),
            defaultIcon = "⚒️"
        ),
        ShamanVillagerNpc(
            id = "npc_elnya",
            name = "Elder Elnya",
            title = "Voice of the Spirits",
            role = "High Shaman Elder",
            avatarEmoji = "🧙‍♀️",
            hutLocation = "Sacred Totem Shrine",
            description = "Eldest spiritual guide of the tribe. She communes with ancestral animal spirits and maintains the mystical barrier protecting the settlement.",
            greeting = "Peace be upon your spirit, child. The totems whisper of ancient wisdom waiting to be awakened.",
            completionQuote = "The spirits hum in harmonious approval of your sacred devotion.",
            favoredActivities = listOf(OsrsSkill.MAGIC, OsrsSkill.DIVINATION, OsrsSkill.HERBLORE),
            favorTypeLabels = listOf("Incantations & Mystic Rituals", "Spirit Divination", "Sacred Herbology"),
            defaultIcon = "✨"
        ),
        ShamanVillagerNpc(
            id = "npc_bram",
            name = "Bram",
            title = "Grove Woodtender",
            role = "Harvesting & Whittling Master",
            avatarEmoji = "🪓",
            hutLocation = "Whispering Grove Canopy",
            description = "Guardian of the sacred forest groves. Bram oversees the harvesting of fallen timber, whittling bows and shafts on the bench, and planting saplings for future generations.",
            greeting = "Listen to the leaves rustle... The forest gives generously. Ready for timber harvesting or whittling?",
            completionQuote = "A bountiful timber harvest and whittled goods! The forest spirits smile upon your diligence.",
            favoredActivities = listOf(OsrsSkill.WOODCUTTING, OsrsSkill.FLETCHING, OsrsSkill.FARMING),
            favorTypeLabels = listOf("Timber Harvesting (Logs)", "Whittling Bench & Bow Carving", "Crop & Sprout Harvesting"),
            defaultIcon = "🪓"
        ),
        ShamanVillagerNpc(
            id = "npc_finbar",
            name = "Finbar",
            title = "Streamwarden",
            role = "River Fisher",
            avatarEmoji = "🎣",
            hutLocation = "Spirit Stream Pier",
            description = "Expert angler who reads the flow of the spirit rivers and knows the sacred migration seasons of all aquatic creatures.",
            greeting = "Ahoy! The trout and salmon are running thick near the waterfall rapids today.",
            completionQuote = "A bountiful catch indeed! The tribe's feast pots will be overflowing tonight.",
            favoredActivities = listOf(OsrsSkill.FISHING, OsrsSkill.COOKING, OsrsSkill.HUNTER),
            favorTypeLabels = listOf("River & Ocean Fishing", "Campfire Roasting", "Water Creature Trapping"),
            defaultIcon = "🐟"
        ),
        ShamanVillagerNpc(
            id = "npc_ember",
            name = "Ember",
            title = "Flamekeeper",
            role = "Hearth Pyromancer & Cook",
            avatarEmoji = "🔥",
            hutLocation = "Central Great Hearth",
            description = "Tender of the eternal tribal fire that has burned unbroken for eight generations. Ember cooks restorative feast broths, roasted meats, and hearty recipes.",
            greeting = "Come warm your hands by the great fire! The embers are ready for delicious meals and brews.",
            completionQuote = "The flame burns brighter than ever and the feast is savored thanks to your noble dedication.",
            favoredActivities = listOf(OsrsSkill.COOKING, OsrsSkill.FIREMAKING, OsrsSkill.SMITHING),
            favorTypeLabels = listOf("Cooking Foods & Recipes", "Sacred Hearth Fires", "Forge Coal Smelting"),
            defaultIcon = "🔥"
        ),
        ShamanVillagerNpc(
            id = "npc_nia",
            name = "Nia",
            title = "Hearth Builder",
            role = "Master Hut-Keeper & Carver",
            avatarEmoji = "🛠️",
            hutLocation = "Weaving & Carving Lodge",
            description = "Master builder and keeper of tribal dwellings. Nia oversees the milling of planks, forging of nails, carving of limestone bricks, and construction of furniture.",
            greeting = "Every plank and carved brick strengthens our home. Come, lend your hands to hut-keeping and building!",
            completionQuote = "Exquisite construction! These crafted goods and sturdy planks will keep our settlement safe.",
            favoredActivities = listOf(OsrsSkill.CONSTRUCTION, OsrsSkill.FLETCHING, OsrsSkill.HUNTER),
            favorTypeLabels = listOf("Hut-Keeping (Planks, Nails & Building)", "Whittling Shafts & Tips", "Trapper Gear"),
            defaultIcon = "🛠️"
        ),
        ShamanVillagerNpc(
            id = "npc_zahur",
            name = "Zahur",
            title = "Leaf Whisperer",
            role = "Tribe Herbalist & Botanist",
            avatarEmoji = "🌿",
            hutLocation = "Apothecary Greenery",
            description = "Healer and herbalist who crushes aromatic herbs and brews potent remedies, stamina tonics, and magical combat potions.",
            greeting = "Breathe in the scent of crushed herbs and fresh extracts. Nature holds a cure for every ailment.",
            completionQuote = "These crushed herbs and brewed potions will mend wounds and restore weary souls.",
            favoredActivities = listOf(OsrsSkill.HERBLORE, OsrsSkill.FARMING, OsrsSkill.DIVINATION),
            favorTypeLabels = listOf("Herb Crushing & Potion Brewing", "Sacred Farm Patches", "Energy Attunement"),
            defaultIcon = "🌿"
        ),
        ShamanVillagerNpc(
            id = "npc_ren",
            name = "Ren",
            title = "Silent Crow",
            role = "Tribe Shadow Scout & Trickster",
            avatarEmoji = "🥷",
            hutLocation = "Treetop Lookout Post",
            description = "Quick-footed shadow scout who moves silently among tree canopies and crowded bazaars, pickpocketing targets and raiding merchant stalls.",
            greeting = "Quiet now... keep your eyes on the marks and your hands swift. Ready for trickery?",
            completionQuote = "Silent, swift, and effective. You have the quiet grace of a master rogue.",
            favoredActivities = listOf(OsrsSkill.THIEVING, OsrsSkill.AGILITY, OsrsSkill.HUNTER),
            favorTypeLabels = listOf("Pickpocketing & Stall Trickery", "Canopy Agility", "Wild Beast Trapping"),
            defaultIcon = "🥷"
        ),
        ShamanVillagerNpc(
            id = "npc_grace",
            name = "Grace",
            title = "Wind Strider",
            role = "Agility Messenger",
            avatarEmoji = "👟",
            hutLocation = "Agility Ridge Trail",
            description = "Chief runner of the shaman tribe, delivering urgent messages across rugged mountains and treacherous rope bridges.",
            greeting = "Keep moving! Momentum is the key to enduring any trial.",
            completionQuote = "Impressive pace! Your endurance honors the wind spirits.",
            favoredActivities = listOf(OsrsSkill.AGILITY, OsrsSkill.ADVENTURING, OsrsSkill.THIEVING),
            favorTypeLabels = listOf("Obstacle Trails", "Wild Expeditions", "Lock Navigation"),
            defaultIcon = "👟"
        ),
        ShamanVillagerNpc(
            id = "npc_sedri",
            name = "Sedri",
            title = "Rift Inscriber & Summoner",
            role = "Rift Shaman & Summoner",
            avatarEmoji = "🔮",
            hutLocation = "Rune Altar & Summoning Circle",
            description = "Mystic scholar dedicated to inscribing ancient runes, binding elemental totems, and shaping mystical effigies.",
            greeting = "The ley lines hum with latent power. I have tasks for both rune-making and spirit summoning!",
            completionQuote = "The runes, effigies, and spirit totems are charged with immense spiritual potency. Well done!",
            favoredActivities = listOf(OsrsSkill.RUNECRAFT, OsrsSkill.FIREMAKING, OsrsSkill.DIVINATION, OsrsSkill.MAGIC),
            favorTypeLabels = listOf("Rune-Making Favors", "Spirit Summoning Favors", "Spellcraft", "Memory Wisps"),
            defaultIcon = "🔮"
        ),
        ShamanVillagerNpc(
            id = "npc_barnaby",
            name = "Captain Barnaby",
            title = "River Voyager",
            role = "Nautical Navigator",
            avatarEmoji = "⛵",
            hutLocation = "Creek Pier & Shipwright",
            description = "Seasoned river captain who sails the uncharted waterways and coordinates courier parcel deliveries across the shaman tribe.",
            greeting = "Ahoy! Keep the waterways clear and deliver cargo parcels safely across the village!",
            completionQuote = "Fair winds and great fortune! That courier parcel reached its destination safely.",
            favoredActivities = listOf(OsrsSkill.SAILING),
            favorTypeLabels = listOf("Parcel Courier Deliveries"),
            defaultIcon = "⛵"
        ),
        ShamanVillagerNpc(
            id = "npc_theron",
            name = "Theron",
            title = "Bounty Warden",
            role = "Spirit Warrior",
            avatarEmoji = "💀",
            hutLocation = "Warrior Hall of Horns",
            description = "Protector of the village perimeter who tracks dangerous corrupted beasts and awards trophies to courageous hunters.",
            greeting = "Gird your spirit for battle. Darkness prowls the forgotten cave networks.",
            completionQuote = "The perimeter is secure once again. You have fought valiantly for the tribe!",
            favoredActivities = listOf(OsrsSkill.SLAYER, OsrsSkill.HUNTER, OsrsSkill.ADVENTURING),
            favorTypeLabels = listOf("Bounty Slayer Tasks", "Beast Stalking", "Cave Dungeon Raids"),
            defaultIcon = "💀"
        ),
        ShamanVillagerNpc(
            id = "npc_kael",
            name = "Kael",
            title = "Beast Tracker",
            role = "Tribe Trapper",
            avatarEmoji = "🐾",
            hutLocation = "Hunting Encampment",
            description = "Master tracker of rare wildlife, chinchompas, and exotic birds across the vast shamanic savannahs.",
            greeting = "Check the wind direction and set the box traps with care.",
            completionQuote = "A masterfully placed trap! These creatures will aid our village greatly.",
            favoredActivities = listOf(OsrsSkill.HUNTER),
            favorTypeLabels = listOf("Beast Tracking"),
            defaultIcon = "🐾"
        ),
        ShamanVillagerNpc(
            id = "npc_orla",
            name = "Orla",
            title = "Star Weaver",
            role = "Celestial Diviner",
            avatarEmoji = "📱",
            hutLocation = "Divination Springs",
            description = "Attuned to the glowing wisps that rise from ancient springs, weaving memory wisps into sacred skill effigies.",
            greeting = "The glowing wisps carry memories of forgotten ages. Let us shape celestial skill effigies.",
            completionQuote = "The celestial effigies resonate with pure divine harmony. Beautiful work!",
            favoredActivities = listOf(OsrsSkill.DIVINATION, OsrsSkill.RUNECRAFT, OsrsSkill.HERBLORE),
            favorTypeLabels = listOf("Skill Effigies & Memory Weaving", "Soul Runecraft", "Mystic Alchemy"),
            defaultIcon = "📱"
        ),
        ShamanVillagerNpc(
            id = "npc_eric",
            name = "Eric",
            title = "Dungeon Delver",
            role = "Cave Explorer",
            avatarEmoji = "🗺️",
            hutLocation = "Catacomb Cavern Entrance",
            description = "Fearless explorer who maps underground labyrinth passages and brings back lost shamanic relics.",
            greeting = "Pack your torches and check your boots! Deep caverns hold unimaginable secrets.",
            completionQuote = "Another forgotten chamber cleared and secured for the tribe!",
            favoredActivities = listOf(OsrsSkill.SLAYER, OsrsSkill.SAILING, OsrsSkill.HUNTER),
            favorTypeLabels = listOf("Monster Bounties", "Nautical Voyages", "Beast Tracking"),
            defaultIcon = "🗺️"
        ),
        ShamanVillagerNpc(
            id = "npc_bryan",
            name = "Farmer Bryan",
            title = "Cropwarden",
            role = "Agriculture Master",
            avatarEmoji = "🌱",
            hutLocation = "Tribal Allotment Plots",
            description = "Oversees the communal farmlands, guiding the planting of sacred herbs, vegetables, and orchard trees.",
            greeting = "Rich soil and gentle sun—that is the shaman's true blessing. Let's tend the vegetables, herbs, and trees!",
            completionQuote = "A magnificent harvest! The tribal silos are full to the brim.",
            favoredActivities = listOf(OsrsSkill.FARMING, OsrsSkill.WOODCUTTING, OsrsSkill.FIREMAKING),
            favorTypeLabels = listOf("Vegetable & Herb Growing", "Tree Planting (Max 1)", "Field Clearing Fires"),
            defaultIcon = "🌱"
        )
    )

    /**
     * Finds an NPC by their unique identifier.
     */
    fun findNpcById(id: String): ShamanVillagerNpc? {
        val clean = id.trim().lowercase()
        return VILLAGERS.find {
            it.id == id ||
            it.id == "npc_$id" ||
            it.id.removePrefix("npc_") == clean ||
            it.name.equals(id, ignoreCase = true) ||
            (clean.contains("arlg") && (it.id.contains("arlg") || it.name.contains("Afrig") || it.name.contains("Arlg"))) ||
            (clean.contains("afrig") && (it.id.contains("arlg") || it.name.contains("Afrig"))) ||
            (clean.contains("arflig") && (it.id.contains("arlg") || it.name.contains("Afrig"))) ||
            (clean.contains("bryan") && (it.id.contains("bryan") || it.name.contains("Bryan"))) ||
            (clean.contains("elnya") && it.id.contains("elnya")) ||
            (clean.contains("elenya") && it.id.contains("elnya"))
        }
    }

    /**
     * Finds all NPCs who are interested in favors for a given skill.
     * Guaranteed that each NPC only appears if the skill is one of their up to 3 favored activities.
     */
    fun getNpcsForSkill(skill: OsrsSkill): List<ShamanVillagerNpc> {
        return VILLAGERS.filter { it.favoredActivities.contains(skill) }
    }

    /**
     * Selects the primary NPC associated with a skill.
     */
    fun getPrimaryNpcForSkill(skill: OsrsSkill): ShamanVillagerNpc {
        return when (skill) {
            OsrsSkill.SMITHING -> findNpcById("npc_arlg") ?: VILLAGERS.first()
            OsrsSkill.WOODCUTTING -> findNpcById("npc_bram") ?: VILLAGERS.first()
            OsrsSkill.FLETCHING -> findNpcById("npc_bram") ?: findNpcById("npc_nia") ?: VILLAGERS.first()
            OsrsSkill.FISHING -> findNpcById("npc_finbar") ?: VILLAGERS.first()
            OsrsSkill.COOKING -> findNpcById("npc_ember") ?: VILLAGERS.first()
            OsrsSkill.CONSTRUCTION -> findNpcById("npc_nia") ?: VILLAGERS.first()
            OsrsSkill.HERBLORE -> findNpcById("npc_zahur") ?: VILLAGERS.first()
            OsrsSkill.THIEVING -> findNpcById("npc_ren") ?: VILLAGERS.first()
            OsrsSkill.RUNECRAFT -> findNpcById("npc_sedri") ?: VILLAGERS.first()
            OsrsSkill.DIVINATION -> findNpcById("npc_orla") ?: VILLAGERS.first()
            OsrsSkill.FARMING -> findNpcById("npc_bryan") ?: VILLAGERS.first()
            OsrsSkill.FIREMAKING -> findNpcById("npc_sedri") ?: VILLAGERS.first()
            OsrsSkill.MAGIC -> findNpcById("npc_elnya") ?: findNpcById("npc_sedri") ?: VILLAGERS.first()
            OsrsSkill.AGILITY -> findNpcById("npc_grace") ?: findNpcById("npc_ren") ?: VILLAGERS.first()
            OsrsSkill.SAILING -> findNpcById("npc_barnaby") ?: VILLAGERS.first()
            OsrsSkill.SLAYER -> findNpcById("npc_theron") ?: VILLAGERS.first()
            OsrsSkill.HUNTER -> findNpcById("npc_kael") ?: findNpcById("npc_finbar") ?: VILLAGERS.first()
            OsrsSkill.ADVENTURING -> findNpcById("npc_theron") ?: findNpcById("npc_grace") ?: VILLAGERS.first()
            else -> getNpcsForSkill(skill).firstOrNull() ?: VILLAGERS.first()
        }
    }
}
