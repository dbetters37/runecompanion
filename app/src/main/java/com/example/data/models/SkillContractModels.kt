package com.example.data.models

import com.example.data.db.FavorContractEntity

/**
 * Typealias representing a Shaman Tribe Favor (formerly referred to as SkillContract).
 * Every favor is assigned by a specific Shaman Tribe Villager NPC according to their specialized disciplines.
 */
typealias TribeFavor = SkillContract

data class SkillContract(
    val skill: OsrsSkill,
    val guildName: String,
    val guildMaster: String,
    val taskTitle: String,
    val targetQty: Int,
    val currentQty: Int = 0,
    val targetEntityId: String = "",
    val iconSymbol: String,
    val rewardXp: Long,
    val rewardGp: Long,
    val rewardItemName: String,
    val rewardItemId: String,
    val rewardFavorXp: Long = 75L,
    val npcId: String = "npc_arlg",
    val npcName: String = "Afrig",
    val npcRole: String = "Tribe Blacksmith",
    val npcEmoji: String = "⚒️",
    val npcLoreQuote: String = "",
    val favorTypeTitle: String = ""
) {
    fun toEntity(petTypeName: String): FavorContractEntity {
        return FavorContractEntity(
            petTypeName = petTypeName,
            skillName = skill.name,
            taskTitle = taskTitle,
            targetQty = targetQty,
            currentQty = currentQty,
            targetEntityId = targetEntityId,
            iconSymbol = iconSymbol,
            rewardXp = rewardXp,
            rewardGp = rewardGp,
            rewardItemName = rewardItemName,
            rewardItemId = rewardItemId,
            rewardFavorXp = rewardFavorXp,
            npcId = npcId,
            npcName = npcName,
            npcRole = npcRole,
            npcEmoji = npcEmoji,
            npcLoreQuote = npcLoreQuote,
            favorTypeTitle = favorTypeTitle,
            guildName = guildName,
            guildMaster = guildMaster,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    }

    companion object {
        fun fromEntity(entity: FavorContractEntity): SkillContract {
            val skill = try {
                OsrsSkill.valueOf(entity.skillName)
            } catch (e: Exception) {
                OsrsSkill.WOODCUTTING
            }
            return SkillContract(
                skill = skill,
                guildName = entity.guildName.ifBlank { "Shaman Guild" },
                guildMaster = entity.guildMaster.ifBlank { entity.npcName },
                taskTitle = entity.taskTitle,
                targetQty = entity.targetQty,
                currentQty = entity.currentQty,
                targetEntityId = entity.targetEntityId,
                iconSymbol = entity.iconSymbol,
                rewardXp = entity.rewardXp,
                rewardGp = entity.rewardGp,
                rewardItemName = entity.rewardItemName,
                rewardItemId = entity.rewardItemId,
                rewardFavorXp = if (entity.rewardFavorXp > 0) entity.rewardFavorXp else 75L,
                npcId = entity.npcId.ifBlank { "npc_arlg" },
                npcName = entity.npcName.ifBlank { "Afrig" },
                npcRole = entity.npcRole.ifBlank { "Tribe Blacksmith" },
                npcEmoji = entity.npcEmoji.ifBlank { "⚒️" },
                npcLoreQuote = entity.npcLoreQuote,
                favorTypeTitle = entity.favorTypeTitle
            )
        }
    }
}

/**
 * Definition of item drop rewards contained in NPC Favor Supply Chests.
 */
data class ContractRewardDrop(
    val itemId: String,
    val itemName: String,
    val itemIcon: String,
    val minQty: Int,
    val maxQty: Int,
    val chancePercent: Int
)

/**
 * A favor task candidate with strict level gating to ensure players are never assigned
 * tasks higher than their current skill level.
 */
data class FavorTaskOption(
    val minLevel: Int,
    val targetEntityId: String,
    val titleGenerator: (qty: Int) -> String,
    val icon: String,
    val minQty: Int = 10,
    val maxQty: Int = 25
)

data class SelectedFavorTask(
    val taskTitle: String,
    val targetEntityId: String,
    val icon: String,
    val targetQty: Int
)

data class CookingFavorTaskOption(
    val reqCooking: Int,
    val reqFishing: Int = 1,
    val targetEntityId: String,
    val titleGenerator: (Int) -> String,
    val icon: String,
    val minQty: Int,
    val maxQty: Int
)

object SkillContractData {

    val CONTRACT_SUPPORTED_SKILLS = listOf(
        OsrsSkill.WOODCUTTING,
        OsrsSkill.FISHING,
        OsrsSkill.SMITHING,
        OsrsSkill.COOKING,
        OsrsSkill.FIREMAKING,
        OsrsSkill.FLETCHING,
        OsrsSkill.HERBLORE,
        OsrsSkill.RUNECRAFT,
        OsrsSkill.CONSTRUCTION,
        OsrsSkill.FARMING,
        OsrsSkill.THIEVING,
        OsrsSkill.AGILITY,
        OsrsSkill.SAILING,
        OsrsSkill.ADVENTURING,
        OsrsSkill.SLAYER,
        OsrsSkill.HUNTER,
        OsrsSkill.DIVINATION
    )

    fun getGuildInfo(skill: OsrsSkill): Pair<ShamanVillagerNpc, String> {
        val npc = NpcData.getPrimaryNpcForSkill(skill)
        val guild = "${npc.name}'s Hut & Workshop"
        return Pair(npc, guild)
    }

    val CONTRACT_REWARD_ITEMS: List<InventoryItem> = CONTRACT_SUPPORTED_SKILLS.map { skill ->
        val prefix = skill.name.lowercase()
        val (npc, _) = getGuildInfo(skill)
        InventoryItem(
            id = "item_contract_reward_$prefix",
            name = "${npc.name}'s Favor Supply Box",
            category = ItemCategory.MISC,
            iconEmoji = "🎁",
            description = "A sealed gift crate from ${npc.name} (${npc.role}). Open it to receive assorted ${skill.displayName} materials, resources, GP, and supplies!",
            costGp = 500L
        )
    }

    fun getSkillDropPool(skill: OsrsSkill): List<ContractRewardDrop> {
        return when (skill) {
            OsrsSkill.SMITHING -> listOf(
                ContractRewardDrop("item_coal", "Coal Ore", "⚫", 15, 45, 100),
                ContractRewardDrop("item_iron_ore", "Iron Ore", "🪨", 15, 40, 90),
                ContractRewardDrop("item_mithril_ore", "Mithril Ore", "⛏️", 8, 25, 75),
                ContractRewardDrop("item_steel_bar", "Steel Bars", "🧱", 6, 18, 60),
                ContractRewardDrop("item_adamantite_ore", "Adamantite Ore", "⛏️", 4, 12, 45),
                ContractRewardDrop("item_rune_ore", "Runite Ore", "⛏️", 2, 5, 25),
                ContractRewardDrop("item_nails", "Bronze Nails", "🔩", 30, 80, 80),
                ContractRewardDrop("item_iron_nails", "Iron Nails", "🔩", 20, 60, 65)
            )
            OsrsSkill.WOODCUTTING -> listOf(
                ContractRewardDrop("item_oak_logs", "Oak Logs", "🪵", 20, 60, 95),
                ContractRewardDrop("item_willow_logs", "Willow Logs", "🪵", 15, 45, 80),
                ContractRewardDrop("item_maple_logs", "Maple Logs", "🪵", 10, 30, 60),
                ContractRewardDrop("item_yew_logs", "Yew Logs", "🪵", 6, 18, 40),
                ContractRewardDrop("item_magic_logs", "Magic Logs", "🪵", 3, 8, 20),
                ContractRewardDrop("item_birds_nest", "Bird's Nest", "🪺", 1, 3, 50)
            )
            OsrsSkill.FLETCHING -> listOf(
                ContractRewardDrop("item_feather", "Feathers", "🪶", 50, 150, 100),
                ContractRewardDrop("item_arrow_shaft", "Arrow Shafts", "🪵", 60, 180, 90),
                ContractRewardDrop("item_bowstring", "Bowstrings", "🧵", 10, 30, 75),
                ContractRewardDrop("item_iron_arrowtips", "Iron Arrowtips", "🏹", 20, 60, 70),
                ContractRewardDrop("item_steel_arrowtips", "Steel Arrowtips", "🏹", 15, 45, 55),
                ContractRewardDrop("item_mithril_arrowtips", "Mithril Arrowtips", "🏹", 10, 30, 40)
            )
            OsrsSkill.FISHING -> listOf(
                ContractRewardDrop("item_raw_trout", "Raw Trout", "🐟", 12, 35, 90),
                ContractRewardDrop("item_raw_salmon", "Raw Salmon", "🐟", 10, 30, 80),
                ContractRewardDrop("item_raw_lobster", "Raw Lobster", "🦞", 8, 24, 65),
                ContractRewardDrop("item_raw_swordfish", "Raw Swordfish", "🗡️", 6, 18, 50),
                ContractRewardDrop("item_raw_shark", "Raw Shark", "🦈", 3, 8, 30),
                ContractRewardDrop("item_seaweed", "Giant Seaweed", "🌿", 5, 15, 60)
            )
            OsrsSkill.COOKING -> listOf(
                ContractRewardDrop("item_cooked_trout", "Cooked Trout", "🐟", 15, 35, 95),
                ContractRewardDrop("item_cooked_salmon", "Cooked Salmon", "🐟", 12, 30, 85),
                ContractRewardDrop("item_cooked_lobster", "Cooked Lobster", "🦞", 8, 24, 70),
                ContractRewardDrop("item_cooked_swordfish", "Cooked Swordfish", "🗡️", 6, 18, 55),
                ContractRewardDrop("item_cooked_shark", "Cooked Shark", "🦈", 4, 10, 35),
                ContractRewardDrop("item_bread", "Warm Hearth Bread", "🍞", 10, 25, 80)
            )
            OsrsSkill.FIREMAKING -> listOf(
                ContractRewardDrop("item_oak_logs", "Oak Logs", "🪵", 25, 60, 95),
                ContractRewardDrop("item_willow_logs", "Willow Logs", "🪵", 20, 50, 80),
                ContractRewardDrop("item_maple_logs", "Maple Logs", "🪵", 15, 35, 60),
                ContractRewardDrop("item_yew_logs", "Yew Logs", "🪵", 8, 20, 40),
                ContractRewardDrop("item_ash", "Purified Ash", "💨", 30, 80, 85)
            )
            OsrsSkill.HERBLORE -> listOf(
                ContractRewardDrop("item_vial_of_water", "Vials of Water", "🧪", 20, 50, 100),
                ContractRewardDrop("item_clean_guam", "Clean Guam", "🌿", 10, 25, 85),
                ContractRewardDrop("item_clean_tarromin", "Clean Tarromin", "🌿", 8, 20, 75),
                ContractRewardDrop("item_clean_harralander", "Clean Harralander", "🌿", 8, 18, 70),
                ContractRewardDrop("item_clean_ranarr", "Clean Ranarr Weed", "🌿", 6, 12, 60),
                ContractRewardDrop("item_clean_irit", "Clean Irit", "🌿", 5, 10, 50),
                ContractRewardDrop("item_clean_avantoe", "Clean Avantoe", "🌿", 4, 8, 45),
                ContractRewardDrop("item_clean_kwuarm", "Clean Kwuarm", "🌿", 3, 6, 40),
                ContractRewardDrop("item_clean_torstol", "Clean Torstol", "🌿", 2, 4, 25),
                ContractRewardDrop("item_crushed_nest", "Crushed Bird Nest", "🪺", 2, 5, 50)
            )
            OsrsSkill.RUNECRAFT -> listOf(
                ContractRewardDrop("item_rune_essence", "Rune Essence", "🔮", 30, 80, 100),
                ContractRewardDrop("item_rune_air", "Air Runes", "💨", 50, 150, 90),
                ContractRewardDrop("item_rune_water", "Water Runes", "💧", 50, 150, 90),
                ContractRewardDrop("item_rune_earth", "Earth Runes", "🌱", 50, 150, 90),
                ContractRewardDrop("item_rune_fire", "Fire Runes", "🔥", 50, 150, 90),
                ContractRewardDrop("item_rune_chaos", "Chaos Runes", "🔮", 20, 50, 70),
                ContractRewardDrop("item_rune_nature", "Nature Runes", "🌿", 15, 40, 65),
                ContractRewardDrop("item_rune_law", "Law Runes", "⚖️", 15, 40, 65),
                ContractRewardDrop("item_rune_death", "Death Runes", "💀", 10, 30, 45),
                ContractRewardDrop("item_rune_blood", "Blood Runes", "🩸", 8, 20, 35)
            )
            OsrsSkill.CONSTRUCTION -> listOf(
                ContractRewardDrop("item_plank", "Normal Planks", "🪵", 20, 50, 90),
                ContractRewardDrop("item_oak_plank", "Oak Planks", "🪵", 15, 35, 80),
                ContractRewardDrop("item_teak_plank", "Teak Planks", "🪵", 10, 25, 60),
                ContractRewardDrop("item_mahogany_plank", "Mahogany Planks", "🪵", 6, 16, 40),
                ContractRewardDrop("item_nails", "Bronze Nails", "🔩", 50, 120, 85),
                ContractRewardDrop("item_iron_nails", "Iron Nails", "🔩", 40, 100, 75),
                ContractRewardDrop("item_steel_nails", "Steel Nails", "🔩", 30, 80, 65),
                ContractRewardDrop("item_limestone_brick", "Limestone Brick", "🧱", 5, 15, 55)
            )
            OsrsSkill.FARMING -> listOf(
                ContractRewardDrop("item_potato_seed", "Potato Seeds", "🌱", 10, 25, 90),
                ContractRewardDrop("item_onion_seed", "Onion Seeds", "🌱", 8, 20, 85),
                ContractRewardDrop("item_cabbage_seed", "Cabbage Seeds", "🌱", 8, 20, 80),
                ContractRewardDrop("item_tomato_seed", "Tomato Seeds", "🌱", 6, 15, 75),
                ContractRewardDrop("item_sweetcorn_seed", "Sweetcorn Seeds", "🌱", 5, 12, 65),
                ContractRewardDrop("item_strawberry_seed", "Strawberry Seeds", "🌱", 4, 10, 55),
                ContractRewardDrop("item_watermelon_seed", "Watermelon Seeds", "🌱", 3, 8, 45),
                ContractRewardDrop("item_guam_seed", "Guam Seeds", "🌱", 4, 8, 70),
                ContractRewardDrop("item_ranarr_seed", "Ranarr Seeds", "🌱", 1, 3, 35),
                ContractRewardDrop("item_oak_seed", "Oak Acorns", "🌰", 1, 3, 50),
                ContractRewardDrop("item_willow_seed", "Willow Seeds", "🌰", 1, 2, 40),
                ContractRewardDrop("item_maple_seed", "Maple Seeds", "🌰", 1, 2, 30),
                ContractRewardDrop("item_yew_seed", "Yew Seeds", "🌰", 1, 2, 20)
            )
            OsrsSkill.THIEVING -> listOf(
                ContractRewardDrop("item_lockpick", "Lockpick", "🗝️", 2, 5, 75),
                ContractRewardDrop("item_coins", "Stolen Purse (Coins)", "🪙", 150, 450, 100),
                ContractRewardDrop("item_silk", "Al Kharid Silk", "🧵", 2, 6, 70),
                ContractRewardDrop("item_bread", "Stolen Fresh Bread", "🍞", 5, 12, 85),
                ContractRewardDrop("item_uncut_sapphire", "Uncut Sapphire", "💎", 1, 3, 60),
                ContractRewardDrop("item_uncut_emerald", "Uncut Emerald", "💎", 1, 2, 45),
                ContractRewardDrop("item_uncut_ruby", "Uncut Ruby", "💎", 2, 5, 60)
            )
            OsrsSkill.AGILITY -> listOf(
                ContractRewardDrop("item_graceful_token", "Mark of Grace", "🟡", 2, 6, 85),
                ContractRewardDrop("item_energy_potion", "Energy Potion (4)", "🧪", 2, 4, 75),
                ContractRewardDrop("item_stamina_potion", "Stamina Potion (4)", "🧪", 1, 3, 50)
            )
            OsrsSkill.SAILING -> listOf(
                ContractRewardDrop("item_driftwood", "Sea Driftwood", "🪵", 10, 25, 80),
                ContractRewardDrop("item_sea_shell", "Iridescent Conch", "🐚", 2, 6, 70),
                ContractRewardDrop("item_seaweed", "Giant Seaweed", "🌿", 10, 25, 70)
            )
            OsrsSkill.DIVINATION -> listOf(
                ContractRewardDrop("item_divine_energy_pale", "Pale Energy", "✨", 40, 100, 95),
                ContractRewardDrop("item_divine_energy_sparkling", "Sparkling Energy", "✨", 25, 60, 80),
                ContractRewardDrop("item_divine_energy_radiant", "Radiant Energy", "✨", 15, 40, 60),
                ContractRewardDrop("item_divine_energy_elder", "Elder Energy", "✨", 8, 20, 35),
                ContractRewardDrop("item_memory_shard", "Memory Shard", "💎", 5, 15, 60),
                ContractRewardDrop("item_transmutation_catalyst", "Transmute Catalyst", "🔮", 2, 5, 40)
            )
            OsrsSkill.ADVENTURING -> listOf(
                ContractRewardDrop("item_ancient_relic_fragment", "Ancient Relic", "🏺", 2, 5, 70),
                ContractRewardDrop("item_dungeon_token", "Dungeon Token", "🪙", 15, 40, 90),
                ContractRewardDrop("item_shaman_charm", "Shamanic Charm", "🪶", 5, 12, 75),
                ContractRewardDrop("item_uncut_dragonstone", "Uncut Dragonstone", "💎", 1, 2, 30),
                ContractRewardDrop("item_stamina_potion", "Stamina Potion (4)", "🧪", 2, 4, 60)
            )
            OsrsSkill.SLAYER -> listOf(
                ContractRewardDrop("item_slayer_point_scroll", "Slayer Trophy Scroll", "📜", 1, 3, 80),
                ContractRewardDrop("item_rune_bar", "Rune Bar", "🧱", 3, 8, 60),
                ContractRewardDrop("item_rune_death", "Death Runes", "🔮", 25, 60, 80),
                ContractRewardDrop("item_rune_blood", "Blood Runes", "🔮", 15, 40, 60),
                ContractRewardDrop("item_uncut_dragonstone", "Uncut Dragonstone", "💎", 1, 2, 35)
            )
            OsrsSkill.HUNTER -> listOf(
                ContractRewardDrop("item_red_chinchompa", "Red Chinchompa", "🐾", 8, 20, 75),
                ContractRewardDrop("item_black_chinchompa", "Black Chinchompa", "🐾", 4, 12, 40),
                ContractRewardDrop("item_kebbit_fur", "Spotted Kebbit Fur", "🥋", 5, 12, 70),
                ContractRewardDrop("item_hunter_token", "Hunter Token", "🪙", 15, 35, 85),
                ContractRewardDrop("item_raw_bird_meat", "Raw Bird Meat", "🍗", 10, 25, 80)
            )
            else -> listOf(
                ContractRewardDrop("item_gold_bar", "Gold Bar", "🪙", 5, 12, 80),
                ContractRewardDrop("item_uncut_ruby", "Uncut Ruby", "💎", 2, 5, 60)
            )
        }
    }

    /**
     * Selects a favor task from the candidate options strictly filtered by the player's current level.
     * Guaranteed to never return a task that requires a level higher than [skillLevel].
     */
    private fun selectLevelAppropriateTask(
        options: List<FavorTaskOption>,
        skillLevel: Int
    ): SelectedFavorTask {
        val safeLevel = skillLevel.coerceAtLeast(1)
        val eligible = options.filter { it.minLevel <= safeLevel }
        val chosenOption = if (eligible.isNotEmpty()) {
            val maxLevel = eligible.maxOf { it.minLevel }
            val topTier = eligible.filter { it.minLevel >= (maxLevel - 25).coerceAtLeast(1) }
            topTier.random()
        } else {
            options.minByOrNull { it.minLevel } ?: options.first()
        }

        val qty = if (chosenOption.minQty == chosenOption.maxQty) {
            chosenOption.minQty
        } else {
            kotlin.random.Random.nextInt(chosenOption.minQty, chosenOption.maxQty + 1)
        }

        return SelectedFavorTask(
            taskTitle = chosenOption.titleGenerator(qty),
            targetEntityId = chosenOption.targetEntityId,
            icon = chosenOption.icon,
            targetQty = qty
        )
    }

    /**
     * Generates a favor specifically requested by an NPC, picking from their favored activities
     * and strictly tailoring the task to the player's skill level.
     */
    fun generateFavorForNpc(
        npc: ShamanVillagerNpc,
        skillLevelMap: Map<OsrsSkill, Int>,
        completedQuests: List<String>
    ): SkillContract {
        val candidateSkills = npc.favoredActivities.ifEmpty { listOf(OsrsSkill.SMITHING) }
        val selectedSkill = candidateSkills.random()
        val skillLevel = (skillLevelMap[selectedSkill] ?: 1).coerceAtLeast(1)
        return generateContractForSkill(
            skill = selectedSkill,
            skillLevel = skillLevel,
            completedQuests = completedQuests,
            specificNpc = npc,
            allSkillLevels = skillLevelMap
        )
    }

    /**
     * Generates a specific, level-tailored favor contract for a skill, assigned by an appropriate Shaman Tribe Villager.
     */
    fun generateContractForSkill(
        skill: OsrsSkill,
        skillLevel: Int,
        completedQuests: List<String>,
        specificNpc: ShamanVillagerNpc? = null,
        allSkillLevels: Map<OsrsSkill, Int> = emptyMap()
    ): SkillContract {
        val safeLevel = skillLevel.coerceAtLeast(1)
        val npc = specificNpc ?: NpcData.getPrimaryNpcForSkill(skill)
        val prefix = skill.name.lowercase()
        val rewardItemName = "${npc.name}'s Favor Box"
        val rewardItemId = "item_contract_reward_$prefix"

        val selected: SelectedFavorTask = when (skill) {
            OsrsSkill.SMITHING -> {
                val favorIsMining = kotlin.random.Random.nextBoolean()
                if (favorIsMining) {
                    val miningOptions = listOf(
                        FavorTaskOption(1, "item_copper_ore", { "Mine $it Copper Ore" }, "🪨", 12, 25),
                        FavorTaskOption(1, "item_tin_ore", { "Mine $it Tin Ore" }, "🪨", 12, 25),
                        FavorTaskOption(15, "item_iron_ore", { "Mine $it Iron Ore" }, "🪨", 12, 24),
                        FavorTaskOption(20, "item_silver_ore", { "Mine $it Silver Ore" }, "⛏️", 10, 20),
                        FavorTaskOption(30, "item_coal", { "Mine $it Coal" }, "⚫", 15, 30),
                        FavorTaskOption(40, "item_gold_ore", { "Mine $it Gold Ore" }, "🪙", 10, 20),
                        FavorTaskOption(55, "item_mithril_ore", { "Mine $it Mithril Ore" }, "⛏️", 8, 18),
                        FavorTaskOption(70, "item_adamantite_ore", { "Mine $it Adamantite Ore" }, "⛏️", 6, 14),
                        FavorTaskOption(85, "item_runite_ore", { "Mine $it Runite Ore" }, "⛏️", 4, 10),
                        FavorTaskOption(92, "item_amethyst", { "Mine $it Amethyst Crystals" }, "💎", 4, 8)
                    )
                    selectLevelAppropriateTask(miningOptions, safeLevel)
                } else {
                    val smithingOptions = listOf(
                        FavorTaskOption(1, "item_bronze_bar", { "Forge $it Bronze Bars" }, "🧱", 10, 22),
                        FavorTaskOption(1, "item_bronze_dagger", { "Forge $it Bronze Daggers" }, "🗡️", 10, 20),
                        FavorTaskOption(1, "item_nails", { "Forge $it Bronze Nails" }, "🔩", 30, 60),
                        FavorTaskOption(15, "item_iron_bar", { "Forge $it Iron Bars" }, "🧱", 10, 20),
                        FavorTaskOption(15, "item_iron_dagger", { "Forge $it Iron Daggers" }, "🗡️", 8, 18),
                        FavorTaskOption(15, "item_iron_nails", { "Forge $it Iron Nails" }, "🔩", 30, 60),
                        FavorTaskOption(30, "item_steel_bar", { "Forge $it Steel Bars" }, "🧱", 8, 18),
                        FavorTaskOption(30, "item_steel_sword", { "Forge $it Steel Swords" }, "⚔️", 6, 14),
                        FavorTaskOption(30, "item_steel_nails", { "Forge $it Steel Nails" }, "🔩", 30, 60),
                        FavorTaskOption(40, "item_gold_bar", { "Smelt $it Gold Bars" }, "🪙", 8, 16),
                        FavorTaskOption(50, "item_mithril_bar", { "Forge $it Mithril Bars" }, "🧱", 6, 14),
                        FavorTaskOption(50, "item_mithril_", { "Forge $it Mithril Weapons/Armor" }, "⚔️", 5, 12),
                        FavorTaskOption(50, "item_mithril_nails", { "Forge $it Opalite Nails" }, "🔩", 25, 50),
                        FavorTaskOption(70, "item_adamant_bar", { "Forge $it Adamant Bars" }, "🧱", 5, 12),
                        FavorTaskOption(70, "item_adamant_", { "Forge $it Adamantite Equipment" }, "⚔️", 4, 10),
                        FavorTaskOption(70, "item_adamant_nails", { "Forge $it Amethyst Nails" }, "🔩", 20, 45),
                        FavorTaskOption(85, "item_rune_bar", { "Forge $it Rune Bars" }, "🧱", 4, 8),
                        FavorTaskOption(85, "item_rune_", { "Forge $it Rune Equipment" }, "⚔️", 3, 7)
                    )
                    selectLevelAppropriateTask(smithingOptions, safeLevel)
                }
            }

            OsrsSkill.WOODCUTTING -> {
                val woodcuttingOptions = listOf(
                    FavorTaskOption(1, "item_logs", { "Chop $it Normal Logs" }, "🪵", 15, 30),
                    FavorTaskOption(15, "item_oak_logs", { "Chop $it Oak Logs" }, "🪵", 18, 32),
                    FavorTaskOption(30, "item_willow_logs", { "Chop $it Willow Logs" }, "🪵", 20, 36),
                    FavorTaskOption(35, "item_teak_logs", { "Chop $it Teak Logs" }, "🪵", 18, 34),
                    FavorTaskOption(45, "item_maple_logs", { "Chop $it Maple Logs" }, "🪵", 18, 32),
                    FavorTaskOption(50, "item_mahogany_logs", { "Chop $it Mahogany Logs" }, "🪵", 16, 30),
                    FavorTaskOption(60, "item_yew_logs", { "Chop $it Yew Logs" }, "🪵", 15, 28),
                    FavorTaskOption(75, "item_magic_logs", { "Chop $it Magic Logs" }, "🪵", 12, 22),
                    FavorTaskOption(90, "item_redwood_logs", { "Chop $it Redwood Logs" }, "🪵", 10, 20)
                )
                selectLevelAppropriateTask(woodcuttingOptions, safeLevel)
            }

            OsrsSkill.FLETCHING -> {
                val fletchingOptions = listOf(
                    FavorTaskOption(1, "whittling_any", { "Whittle $it Items on Whittling Bench" }, "🪓", 15, 30),
                    FavorTaskOption(1, "whittling_any", { "Fletch $it Feathered Arrows or Strings" }, "🏹", 20, 40),
                    FavorTaskOption(20, "whittling_any", { "Craft $it Oak Bows or Handles" }, "🪓", 15, 30),
                    FavorTaskOption(35, "whittling_any", { "Craft $it Willow Bows or Darts" }, "🏹", 18, 35),
                    FavorTaskOption(50, "whittling_any", { "Craft $it Maple Hunting Traps or Bows" }, "🪓", 20, 40),
                    FavorTaskOption(65, "whittling_any", { "Fletch $it Yew Bows or Crossbows" }, "🏹", 20, 45),
                    FavorTaskOption(80, "whittling_any", { "Craft $it Magic Whittling Masterworks" }, "✨", 25, 50)
                )
                selectLevelAppropriateTask(fletchingOptions, safeLevel)
            }

            OsrsSkill.FISHING -> {
                val fishingOptions = listOf(
                    FavorTaskOption(1, "item_raw_shrimps", { "Catch $it Raw Shrimps" }, "🦐", 15, 30),
                    FavorTaskOption(5, "item_raw_sardine", { "Catch $it Raw Sardines" }, "🐟", 15, 30),
                    FavorTaskOption(10, "item_raw_herring", { "Catch $it Raw Herring" }, "🐟", 16, 32),
                    FavorTaskOption(20, "item_raw_trout", { "Catch $it Raw Trout" }, "🐟", 18, 34),
                    FavorTaskOption(30, "item_raw_salmon", { "Catch $it Raw Salmon" }, "🐟", 18, 34),
                    FavorTaskOption(35, "item_raw_tuna", { "Catch $it Raw Tuna" }, "🐟", 16, 30),
                    FavorTaskOption(40, "item_raw_lobster", { "Catch $it Raw Lobsters" }, "🦞", 16, 30),
                    FavorTaskOption(50, "item_raw_swordfish", { "Catch $it Raw Swordfish" }, "🗡️", 15, 28),
                    FavorTaskOption(62, "item_raw_monkfish", { "Catch $it Raw Monkfish" }, "🐟", 14, 26),
                    FavorTaskOption(76, "item_raw_shark", { "Catch $it Raw Sharks" }, "🦈", 12, 22),
                    FavorTaskOption(82, "item_raw_anglerfish", { "Catch $it Raw Anglerfish" }, "🐟", 10, 20),
                    FavorTaskOption(91, "item_raw_manta_ray", { "Catch $it Raw Manta Rays" }, "🌊", 8, 16)
                )
                selectLevelAppropriateTask(fishingOptions, safeLevel)
            }

            OsrsSkill.COOKING -> {
                val fishingLevel = (allSkillLevels[OsrsSkill.FISHING] ?: 1).coerceAtLeast(1)
                val cookingOptions = listOf(
                    CookingFavorTaskOption(1, 1, "item_raw_shrimps", { "Cook $it Raw Shrimps" }, "🦐", 12, 25),
                    CookingFavorTaskOption(1, 1, "item_raw_chicken", { "Roast $it Chickens or Meats" }, "🍗", 10, 20),
                    CookingFavorTaskOption(1, 1, "item_bread_dough", { "Bake $it Warm Hearth Loaves" }, "🍞", 10, 20),
                    CookingFavorTaskOption(1, 5, "item_raw_sardine", { "Cook $it Raw Sardines" }, "🐟", 12, 25),
                    CookingFavorTaskOption(5, 10, "item_raw_herring", { "Cook $it Raw Herring" }, "🐟", 12, 25),
                    CookingFavorTaskOption(10, 16, "item_raw_mackerel", { "Cook $it Raw Mackerel" }, "🐟", 12, 24),
                    CookingFavorTaskOption(15, 20, "item_raw_trout", { "Cook $it Raw Trout" }, "🐟", 14, 28),
                    CookingFavorTaskOption(20, 25, "item_raw_pike", { "Cook $it Raw Pike" }, "🐟", 12, 24),
                    CookingFavorTaskOption(25, 30, "item_raw_salmon", { "Cook $it Raw Salmon" }, "🐟", 14, 28),
                    CookingFavorTaskOption(25, 1, "item_uncooked_stew", { "Prepare $it Hearth Stews" }, "🍲", 10, 20),
                    CookingFavorTaskOption(30, 35, "item_raw_tuna", { "Cook $it Raw Tuna" }, "🐟", 14, 26),
                    CookingFavorTaskOption(30, 1, "item_uncooked_pie", { "Bake $it Apple Pies" }, "🥧", 8, 18),
                    CookingFavorTaskOption(35, 1, "item_uncooked_pizza", { "Bake $it Hearth Pizzas" }, "🍕", 8, 16),
                    CookingFavorTaskOption(40, 40, "item_raw_lobster", { "Cook $it Raw Lobsters" }, "🦞", 12, 24),
                    CookingFavorTaskOption(43, 46, "item_raw_bass", { "Cook $it Raw Bass" }, "🐟", 10, 22),
                    CookingFavorTaskOption(45, 50, "item_raw_swordfish", { "Cook $it Raw Swordfish" }, "🗡️", 10, 22),
                    CookingFavorTaskOption(60, 1, "item_uncooked_curry", { "Cook $it Savory Curries" }, "🍛", 8, 16),
                    CookingFavorTaskOption(62, 62, "item_raw_monkfish", { "Cook $it Raw Monkfish" }, "🐟", 10, 20),
                    CookingFavorTaskOption(80, 76, "item_raw_shark", { "Cook $it Raw Sharks" }, "🦈", 8, 16),
                    CookingFavorTaskOption(82, 79, "item_raw_sea_turtle", { "Cook $it Raw Sea Turtles" }, "🐢", 8, 16),
                    CookingFavorTaskOption(84, 82, "item_raw_anglerfish", { "Cook $it Raw Anglerfish" }, "🐟", 6, 14),
                    CookingFavorTaskOption(90, 85, "item_raw_dark_crab", { "Cook $it Raw Dark Crabs" }, "🦀", 6, 12),
                    CookingFavorTaskOption(91, 91, "item_raw_manta_ray", { "Cook $it Raw Manta Rays" }, "🌊", 5, 10),
                    CookingFavorTaskOption(1, 1, "cooking_any", { "Cook $it Dishes at the Great Hearth" }, "🍳", 10, 20)
                )
                val eligible = cookingOptions.filter { it.reqCooking <= safeLevel && it.reqFishing <= fishingLevel }
                val chosen = if (eligible.isNotEmpty()) {
                    val maxLvl = eligible.maxOf { it.reqCooking }
                    val topTier = eligible.filter { it.reqCooking >= (maxLvl - 25).coerceAtLeast(1) }
                    topTier.random()
                } else {
                    cookingOptions.last()
                }
                val qty = if (chosen.minQty == chosen.maxQty) chosen.minQty else kotlin.random.Random.nextInt(chosen.minQty, chosen.maxQty + 1)
                SelectedFavorTask(
                    taskTitle = chosen.titleGenerator(qty),
                    targetEntityId = chosen.targetEntityId,
                    icon = chosen.icon,
                    targetQty = qty
                )
            }

            OsrsSkill.CONSTRUCTION -> {
                val constructionOptions = listOf(
                    FavorTaskOption(1, "item_plank", { "Mill $it Normal Planks" }, "🪵", 12, 25),
                    FavorTaskOption(1, "item_nails", { "Forge $it Bronze Nails" }, "🔩", 40, 80),
                    FavorTaskOption(1, "poh_furniture_any", { "Craft $it Hut Furniture Items" }, "🪑", 10, 20),
                    FavorTaskOption(10, "item_limestone_brick", { "Carve $it Limestone Bricks" }, "🧱", 12, 25),
                    FavorTaskOption(15, "item_oak_plank", { "Mill $it Oak Planks" }, "🪵", 12, 25),
                    FavorTaskOption(15, "item_iron_nails", { "Forge $it Iron Nails" }, "🔩", 40, 80),
                    FavorTaskOption(35, "item_teak_plank", { "Mill $it Teak Planks" }, "🪵", 10, 22),
                    FavorTaskOption(35, "item_steel_nails", { "Forge $it Steel Nails" }, "🔩", 40, 80),
                    FavorTaskOption(50, "item_mahogany_plank", { "Mill $it Mahogany Planks" }, "🪵", 8, 18),
                    FavorTaskOption(60, "poh_furniture_any", { "Construct $it Estate Furniture Pieces" }, "🪑", 8, 16)
                )
                selectLevelAppropriateTask(constructionOptions, safeLevel)
            }

            OsrsSkill.HERBLORE -> {
                val herbloreOptions = listOf(
                    FavorTaskOption(1, "item_crushed_nest", { "Crush $it Bird's Nests for Dust" }, "🪺", 10, 20),
                    FavorTaskOption(3, "potion_attack", { "Brew $it Attack Potions" }, "🧪", 10, 20),
                    FavorTaskOption(3, "item_grimy_guam", { "Clean & Crush $it Guam Herbs" }, "🌿", 12, 24),
                    FavorTaskOption(5, "item_grimy_marrentill", { "Clean & Crush $it Marrentill Herbs" }, "🌿", 12, 24),
                    FavorTaskOption(11, "item_grimy_tarromin", { "Clean & Crush $it Tarromin Herbs" }, "🌿", 12, 24),
                    FavorTaskOption(12, "potion_strength", { "Brew $it Strength Potions" }, "🧪", 10, 20),
                    FavorTaskOption(20, "item_grimy_harralander", { "Clean & Crush $it Harralander Herbs" }, "🌿", 12, 24),
                    FavorTaskOption(25, "item_grimy_ranarr", { "Clean & Crush $it Ranarr Weeds" }, "🌿", 10, 20),
                    FavorTaskOption(26, "potion_energy", { "Brew $it Energy Potions" }, "🧪", 10, 20),
                    FavorTaskOption(30, "potion_defence", { "Brew $it Defence Potions" }, "🧪", 10, 20),
                    FavorTaskOption(30, "item_grimy_toadflax", { "Clean & Crush $it Toadflax Herbs" }, "🌿", 10, 20),
                    FavorTaskOption(38, "potion_prayer", { "Brew $it Prayer Potions" }, "🧪", 8, 18),
                    FavorTaskOption(40, "item_grimy_irit", { "Clean & Crush $it Irit Leaves" }, "🌿", 10, 20),
                    FavorTaskOption(45, "potion_super_attack", { "Brew $it Super Attack Potions" }, "🧪", 8, 16),
                    FavorTaskOption(48, "item_grimy_avantoe", { "Crush $it Avantoe Leaves" }, "🌿", 10, 20),
                    FavorTaskOption(54, "item_grimy_kwuarm", { "Clean & Crush $it Kwuarm Herbs" }, "🌿", 10, 20),
                    FavorTaskOption(55, "potion_super_strength", { "Brew $it Super Strength Potions" }, "🧪", 8, 16),
                    FavorTaskOption(59, "item_grimy_snapdragon", { "Clean & Crush $it Snapdragon Herbs" }, "🌿", 8, 16),
                    FavorTaskOption(63, "potion_super_restore", { "Brew $it Super Restore Potions" }, "🧪", 6, 14),
                    FavorTaskOption(65, "item_grimy_cadantine", { "Clean & Crush $it Cadantine Herbs" }, "🌿", 8, 16),
                    FavorTaskOption(66, "potion_super_defence", { "Brew $it Super Defence Potions" }, "🧪", 6, 14),
                    FavorTaskOption(70, "item_grimy_dwarf_weed", { "Clean & Crush $it Dwarf Weed" }, "🌿", 8, 16),
                    FavorTaskOption(75, "item_grimy_torstol", { "Clean & Crush $it Torstol Herbs" }, "🌿", 6, 12),
                    FavorTaskOption(77, "item_stamina_potion", { "Brew $it Stamina Potions" }, "🧪", 6, 12),
                    FavorTaskOption(81, "potion_saradomin_brew", { "Brew $it Saradomin Brews" }, "🧪", 5, 10)
                )
                selectLevelAppropriateTask(herbloreOptions, safeLevel)
            }

            OsrsSkill.THIEVING -> {
                val thievingOptions = listOf(
                    FavorTaskOption(1, "man", { "Pickpocket $it Citizens" }, "🧔", 15, 30),
                    FavorTaskOption(5, "stall_bakery", { "Steal from $it Bakery Stalls" }, "🥖", 15, 30),
                    FavorTaskOption(10, "farmer", { "Pickpocket $it Farmers" }, "🧑‍🌾", 15, 30),
                    FavorTaskOption(20, "silk_merchant", { "Pickpocket $it Silk Merchants" }, "🧵", 15, 30),
                    FavorTaskOption(20, "stall_silk", { "Steal from $it Silk Stalls" }, "🧵", 15, 30),
                    FavorTaskOption(25, "street_urchin", { "Pickpocket $it Desert Urchins" }, "👦", 15, 30),
                    FavorTaskOption(35, "stall_fur", { "Steal from $it Fur Stalls" }, "🦊", 15, 30),
                    FavorTaskOption(38, "master_farmer", { "Pickpocket $it Master Farmers" }, "🧑‍🌾", 15, 30),
                    FavorTaskOption(40, "guard", { "Pickpocket $it City Guards" }, "🛡️", 15, 30),
                    FavorTaskOption(50, "stall_silver", { "Steal from $it Silver Stalls" }, "🪙", 12, 25),
                    FavorTaskOption(53, "desert_bandit", { "Pickpocket $it Desert Bandits" }, "🏜️", 15, 30),
                    FavorTaskOption(55, "ardougne_knight", { "Pickpocket $it Ardougne Knights" }, "⚔️", 15, 30),
                    FavorTaskOption(65, "stall_scimitar", { "Steal from $it Scimitar Stalls" }, "🗡️", 10, 20),
                    FavorTaskOption(70, "paladin", { "Pickpocket $it Paladins" }, "🏰", 12, 24),
                    FavorTaskOption(75, "stall_gem", { "Steal from $it Gem Stalls" }, "💎", 8, 18),
                    FavorTaskOption(75, "tzhaar", { "Pickpocket $it TzHaar Artisans" }, "🌋", 12, 24),
                    FavorTaskOption(80, "hero", { "Pickpocket $it Heroes" }, "👑", 10, 20),
                    FavorTaskOption(85, "elf", { "Pickpocket $it Prifddinas Elves" }, "🧝‍♀️", 8, 18)
                )
                selectLevelAppropriateTask(thievingOptions, safeLevel)
            }

            OsrsSkill.RUNECRAFT -> {
                val runecraftOptions = listOf(
                    FavorTaskOption(1, "item_rune_air", { "Craft $it Air Runes" }, "💨", 20, 50),
                    FavorTaskOption(2, "item_rune_mind", { "Craft $it Mind Runes" }, "🧠", 20, 50),
                    FavorTaskOption(5, "item_rune_water", { "Craft $it Water Runes" }, "💧", 20, 50),
                    FavorTaskOption(9, "item_rune_earth", { "Craft $it Earth Runes" }, "🌱", 20, 50),
                    FavorTaskOption(14, "item_rune_fire", { "Craft $it Fire Runes" }, "🔥", 20, 50),
                    FavorTaskOption(20, "item_rune_body", { "Craft $it Body Runes" }, "🛡️", 20, 50),
                    FavorTaskOption(27, "item_rune_cosmic", { "Craft $it Cosmic Runes" }, "✨", 15, 35),
                    FavorTaskOption(35, "item_rune_chaos", { "Craft $it Chaos Runes" }, "🔮", 15, 35),
                    FavorTaskOption(40, "item_rune_astral", { "Craft $it Astral Runes" }, "🌌", 15, 35),
                    FavorTaskOption(44, "item_rune_nature", { "Craft $it Nature Runes" }, "🌿", 15, 35),
                    FavorTaskOption(54, "item_rune_law", { "Craft $it Law Runes" }, "⚖️", 12, 30),
                    FavorTaskOption(65, "item_rune_death", { "Craft $it Death Runes" }, "💀", 12, 25),
                    FavorTaskOption(77, "item_rune_blood", { "Craft $it Blood Runes" }, "🩸", 10, 22),
                    FavorTaskOption(90, "item_rune_soul", { "Craft $it Soul Runes" }, "🔮", 8, 18),
                    FavorTaskOption(95, "item_rune_wrath", { "Craft $it Wrath Runes" }, "⚡", 6, 15)
                )
                selectLevelAppropriateTask(runecraftOptions, safeLevel)
            }

            OsrsSkill.DIVINATION -> {
                val divinationOptions = listOf(
                    FavorTaskOption(1, "divination_effigy", { "Infuse $it Pale Memory Effigies" }, "✨", 1, 3),
                    FavorTaskOption(25, "divination_effigy", { "Craft $it Sparkling Memory Effigies" }, "✨", 1, 3),
                    FavorTaskOption(50, "divination_effigy", { "Weave $it Radiant Memory Effigies" }, "✨", 1, 3),
                    FavorTaskOption(75, "divination_effigy", { "Shape $it Celestial Skill Effigies" }, "🌟", 1, 3)
                )
                selectLevelAppropriateTask(divinationOptions, safeLevel)
            }

            OsrsSkill.FARMING -> {
                val favorTypeRoll = kotlin.random.Random.nextInt(3)
                when {
                    // Tree planting (Requires level 25+)
                    favorTypeRoll == 0 && safeLevel >= 25 -> {
                        val treeOptions = listOf(
                            FavorTaskOption(25, "tree_oak", { "Plant & Grow 1 Oak Tree" }, "🌳", 1, 1),
                            FavorTaskOption(25, "tree_birch", { "Plant & Grow 1 Birch Tree" }, "🌳", 1, 1),
                            FavorTaskOption(25, "tree_apple", { "Plant & Grow 1 Apple Tree" }, "🍎", 1, 1),
                            FavorTaskOption(30, "tree_willow", { "Plant & Grow 1 Willow Tree" }, "🌳", 1, 1),
                            FavorTaskOption(33, "tree_cherry", { "Plant & Grow 1 Cherry Tree" }, "🍒", 1, 1),
                            FavorTaskOption(38, "tree_pine", { "Plant & Grow 1 Pine Tree" }, "🌲", 1, 1),
                            FavorTaskOption(39, "tree_apricot", { "Plant & Grow 1 Apricot Tree" }, "🍑", 1, 1),
                            FavorTaskOption(45, "tree_maple", { "Plant & Grow 1 Maple Tree" }, "🍁", 1, 1),
                            FavorTaskOption(48, "tree_peach", { "Plant & Grow 1 Peach Tree" }, "🍑", 1, 1),
                            FavorTaskOption(52, "tree_cedar", { "Plant & Grow 1 Cedar Tree" }, "🌲", 1, 1),
                            FavorTaskOption(57, "tree_palm", { "Plant & Grow 1 Palm Tree" }, "🌴", 1, 1),
                            FavorTaskOption(60, "tree_yew", { "Plant & Grow 1 Yew Tree" }, "🌳", 1, 1),
                            FavorTaskOption(64, "tree_sakura", { "Plant & Grow 1 Sakura Tree" }, "🌸", 1, 1),
                            FavorTaskOption(68, "tree_ironwood", { "Plant & Grow 1 Ironwood Tree" }, "🪵", 1, 1),
                            FavorTaskOption(72, "tree_coconut", { "Plant & Grow 1 Coconut Tree" }, "🥥", 1, 1),
                            FavorTaskOption(75, "tree_magic", { "Plant & Grow 1 Magic Tree" }, "✨", 1, 1),
                            FavorTaskOption(81, "tree_dragonfruit", { "Plant & Grow 1 Dragonfruit Tree" }, "🐉", 1, 1),
                            FavorTaskOption(85, "tree_redwood", { "Plant & Grow 1 Redwood Tree" }, "🪵", 1, 1),
                            FavorTaskOption(88, "tree_spirit", { "Plant & Grow 1 Spirit Tree" }, "🌀", 1, 1)
                        )
                        selectLevelAppropriateTask(treeOptions, safeLevel)
                    }

                    // Herb growing
                    favorTypeRoll == 1 -> {
                        val herbOptions = listOf(
                            FavorTaskOption(1, "item_clean_greenleaf", { "Grow $it Greenleaf Herbs" }, "🌿", 8, 18),
                            FavorTaskOption(8, "item_clean_meadow_mint", { "Grow $it Meadow Mint" }, "🌱", 8, 18),
                            FavorTaskOption(18, "item_clean_wild_thyme", { "Grow $it Wild Thyme" }, "🍃", 8, 18),
                            FavorTaskOption(28, "item_clean_lavender", { "Grow $it Lavender" }, "🪻", 8, 18),
                            FavorTaskOption(40, "item_clean_sunleaf", { "Grow $it Sunleaf Herbs" }, "🌿", 6, 15),
                            FavorTaskOption(55, "item_clean_ironleaf", { "Grow $it Ironleaf Herbs" }, "🍃", 6, 14),
                            FavorTaskOption(68, "item_clean_wintergreen", { "Grow $it Wintergreen" }, "🌿", 5, 12),
                            FavorTaskOption(78, "item_clean_silverleaf", { "Grow $it Silverleaf" }, "🌱", 5, 12),
                            FavorTaskOption(85, "item_clean_mystic_sage", { "Grow $it Mystic Sage" }, "✨", 4, 10),
                            FavorTaskOption(89, "item_clean_moonflower", { "Grow $it Moonflowers" }, "🪻", 4, 10),
                            FavorTaskOption(92, "item_clean_vervain", { "Grow $it Vervain Herbs" }, "🌸", 4, 8)
                        )
                        selectLevelAppropriateTask(herbOptions, safeLevel)
                    }

                    // Vegetable growing
                    else -> {
                        val vegOptions = listOf(
                            FavorTaskOption(1, "item_potato", { "Grow $it Potatoes" }, "🥔", 8, 18),
                            FavorTaskOption(5, "item_onion", { "Grow $it Onions" }, "🧅", 8, 18),
                            FavorTaskOption(10, "item_cabbage", { "Grow $it Cabbages" }, "🥬", 8, 18),
                            FavorTaskOption(15, "item_carrot", { "Grow $it Carrots" }, "🥕", 8, 18),
                            FavorTaskOption(20, "item_tomato", { "Grow $it Tomatoes" }, "🍅", 8, 18),
                            FavorTaskOption(35, "item_sweetcorn", { "Grow $it Sweetcorn" }, "🌽", 8, 18),
                            FavorTaskOption(45, "item_strawberry", { "Grow $it Strawberries" }, "🍓", 8, 16),
                            FavorTaskOption(52, "item_pumpkin", { "Grow $it Giant Pumpkins" }, "🎃", 6, 14),
                            FavorTaskOption(60, "item_watermelon", { "Grow $it Watermelons" }, "🍉", 6, 14)
                        )
                        selectLevelAppropriateTask(vegOptions, safeLevel)
                    }
                }
            }

            OsrsSkill.FIREMAKING -> {
                val favorIsTotem = kotlin.random.Random.nextBoolean()
                if (favorIsTotem) {
                    val totemOptions = listOf(
                        FavorTaskOption(1, "item_totem_golem_clay", { "Forge $it Clay Golem Totems" }, "🗿", 1, 3),
                        FavorTaskOption(1, "item_totem_spirit_wolf", { "Forge $it Spirit Wolf Totems" }, "🐺", 1, 3),
                        FavorTaskOption(4, "item_totem_dreadfowl", { "Forge $it Dreadfowl Totems" }, "🦅", 1, 3),
                        FavorTaskOption(15, "item_totem_golem_bronze", { "Forge $it Bronze Golem Totems" }, "🗿", 1, 3),
                        FavorTaskOption(19, "item_totem_desert_wyrm", { "Forge $it Desert Wyrm Totems" }, "🐛", 1, 3),
                        FavorTaskOption(30, "item_totem_golem_iron", { "Forge $it Iron Golem Totems" }, "🗿", 1, 3),
                        FavorTaskOption(36, "item_totem_bronze_minotaur", { "Forge $it Bronze Minotaur Totems" }, "🐂", 1, 3),
                        FavorTaskOption(50, "item_totem_golem_steel", { "Forge $it Steel Golem Totems" }, "🗿", 1, 3),
                        FavorTaskOption(52, "item_totem_spirit_terrorbird", { "Forge $it Spirit Terrorbird Totems" }, "🦤", 1, 2),
                        FavorTaskOption(67, "item_totem_war_tortoise", { "Forge $it War Tortoise Totems" }, "🐢", 1, 2),
                        FavorTaskOption(68, "item_totem_bunyip", { "Forge $it Bunyip Totems" }, "🐟", 1, 2),
                        FavorTaskOption(96, "item_totem_pack_yak", { "Forge $it Pack Yak Totems" }, "🦬", 1, 2),
                        FavorTaskOption(99, "item_totem_steel_titan", { "Forge $it Steel Titan Totems" }, "⚔️", 1, 2)
                    )
                    selectLevelAppropriateTask(totemOptions, safeLevel)
                } else {
                    val effigyOptions = listOf(
                        FavorTaskOption(1, "item_effigy_air", { "Craft $it Air Effigies" }, "💨", 2, 4),
                        FavorTaskOption(1, "item_effigy_mind", { "Craft $it Mind Effigies" }, "🧠", 2, 4),
                        FavorTaskOption(5, "item_effigy_water", { "Craft $it Water Effigies" }, "💧", 2, 4),
                        FavorTaskOption(10, "item_effigy_earth", { "Craft $it Earth Effigies" }, "🌱", 2, 4),
                        FavorTaskOption(14, "item_effigy_fire", { "Craft $it Fire Effigies" }, "🔥", 2, 4),
                        FavorTaskOption(20, "item_effigy_body", { "Craft $it Body Effigies" }, "🛡️", 2, 4),
                        FavorTaskOption(27, "item_effigy_cosmic", { "Craft $it Cosmic Effigies" }, "✨", 2, 4),
                        FavorTaskOption(35, "item_effigy_chaos", { "Craft $it Chaos Effigies" }, "🔮", 2, 4),
                        FavorTaskOption(40, "item_effigy_astral", { "Craft $it Astral Effigies" }, "🌌", 2, 4),
                        FavorTaskOption(44, "item_effigy_nature", { "Craft $it Nature Effigies" }, "🌿", 2, 4),
                        FavorTaskOption(54, "item_effigy_law", { "Craft $it Law Effigies" }, "⚖️", 2, 4),
                        FavorTaskOption(65, "item_effigy_death", { "Craft $it Death Effigies" }, "💀", 1, 3),
                        FavorTaskOption(77, "item_effigy_blood", { "Craft $it Blood Effigies" }, "🩸", 1, 3),
                        FavorTaskOption(90, "item_effigy_soul", { "Craft $it Soul Effigies" }, "🔮", 1, 3),
                        FavorTaskOption(95, "item_effigy_wrath", { "Craft $it Wrath Effigies" }, "⚡", 1, 3)
                    )
                    selectLevelAppropriateTask(effigyOptions, safeLevel)
                }
            }

            OsrsSkill.SLAYER -> {
                val slayerOptions = listOf(
                    FavorTaskOption(1, "slayer_beast", { "Slay $it Cave Crawlers or Spiders" }, "🕷️", 10, 25),
                    FavorTaskOption(20, "slayer_beast", { "Slay $it Rockslugs or Lizards" }, "🦎", 10, 25),
                    FavorTaskOption(40, "slayer_beast", { "Slay $it Basilisks or Infernal Mages" }, "🧙‍♂️", 10, 25),
                    FavorTaskOption(60, "slayer_beast", { "Slay $it Bloodvelds or Aberrant Spectres" }, "👻", 10, 25),
                    FavorTaskOption(75, "slayer_beast", { "Slay $it Gargoyles or Abyssal Demons" }, "😈", 8, 20),
                    FavorTaskOption(85, "slayer_beast", { "Slay $it Dark Beasts or Hydras" }, "🐉", 6, 15)
                )
                selectLevelAppropriateTask(slayerOptions, safeLevel)
            }

            OsrsSkill.HUNTER -> {
                val hunterOptions = listOf(
                    FavorTaskOption(1, "hunter_creature", { "Trap $it Crimson Swifts" }, "🐦", 8, 20),
                    FavorTaskOption(15, "hunter_creature", { "Net $it Ruby Harvest Butterflies" }, "🦋", 8, 20),
                    FavorTaskOption(29, "hunter_creature", { "Catch $it Swamp Lizards" }, "🦎", 8, 20),
                    FavorTaskOption(43, "hunter_creature", { "Snare $it Spotted Kebbits" }, "🐾", 8, 20),
                    FavorTaskOption(53, "hunter_creature", { "Trap $it Grey Chinchompas" }, "🐭", 8, 20),
                    FavorTaskOption(63, "hunter_creature", { "Trap $it Red Chinchompas" }, "🔴", 8, 18),
                    FavorTaskOption(73, "hunter_creature", { "Trap $it Black Chinchompas" }, "⚫", 6, 16)
                )
                selectLevelAppropriateTask(hunterOptions, safeLevel)
            }

            OsrsSkill.AGILITY -> {
                val agilityOptions = listOf(
                    FavorTaskOption(1, "agility", { "Traverse $it Gnome Course Laps" }, "👟", 12, 25),
                    FavorTaskOption(20, "agility", { "Traverse $it Al Kharid Rooftop Laps" }, "👟", 15, 30),
                    FavorTaskOption(40, "agility", { "Traverse $it Canifis Rooftop Laps" }, "👟", 15, 30),
                    FavorTaskOption(60, "agility", { "Traverse $it Seers Village Rooftop Laps" }, "👟", 18, 35),
                    FavorTaskOption(80, "agility", { "Traverse $it Rellekka Rooftop Laps" }, "👟", 20, 40)
                )
                selectLevelAppropriateTask(agilityOptions, safeLevel)
            }

            OsrsSkill.SAILING -> {
                // Captain Barnaby's unique parcel delivery favor system:
                // Delivers a sealed parcel from one tribal NPC to another (neither of which is Barnaby).
                val nonBarnabyVillagers = NpcData.VILLAGERS.filter {
                    it.id != "npc_barnaby" && it.id.removePrefix("npc_") != "barnaby"
                }
                val sourceNpc = nonBarnabyVillagers.random()
                val destNpc = nonBarnabyVillagers.filter { it.id != sourceNpc.id }.random()
                val cleanSource = sourceNpc.id.removePrefix("npc_")
                val cleanDest = destNpc.id.removePrefix("npc_")

                SelectedFavorTask(
                    taskTitle = "Deliver Parcel: From ${sourceNpc.name} ➡️ To ${destNpc.name}",
                    targetEntityId = "parcel:${cleanSource}:${cleanDest}",
                    icon = "📦",
                    targetQty = 1
                )
            }

            OsrsSkill.ADVENTURING -> {
                val adventuringOptions = listOf(
                    FavorTaskOption(1, "adventuring", { "Clear $it Catacomb Dungeon Chambers" }, "🗺️", 3, 8),
                    FavorTaskOption(25, "adventuring", { "Explore $it Ancient Crypt Chambers" }, "🗺️", 4, 10),
                    FavorTaskOption(50, "adventuring", { "Conquer $it Deep Dungeon Vaults" }, "⚔️", 5, 12),
                    FavorTaskOption(75, "adventuring", { "Complete $it Master Catacomb Raids" }, "👑", 6, 14)
                )
                selectLevelAppropriateTask(adventuringOptions, safeLevel)
            }

            else -> {
                SelectedFavorTask(
                    taskTitle = "Complete 10 ${skill.displayName} Tasks",
                    targetEntityId = "${prefix}_general",
                    icon = "⭐",
                    targetQty = 10
                )
            }
        }

        val baseRewardXp = if (skill == OsrsSkill.SAILING) {
            250L // Barnaby parcel delivery gives low XP as requested
        } else {
            (selected.targetQty * 25L * safeLevel.coerceAtLeast(10)).coerceIn(500L, 50000L)
        }
        val baseRewardGp = if (skill == OsrsSkill.SAILING) 500L else (selected.targetQty * 15L * safeLevel.coerceAtLeast(5)).coerceIn(200L, 25000L)
        val rewardFavorXp = kotlin.random.Random.nextLong(60L, 100L)

        return SkillContract(
            skill = skill,
            guildName = "${npc.name}'s Lodge",
            guildMaster = npc.name,
            taskTitle = selected.taskTitle,
            targetQty = selected.targetQty,
            currentQty = 0,
            targetEntityId = selected.targetEntityId,
            iconSymbol = selected.icon,
            rewardXp = baseRewardXp,
            rewardGp = baseRewardGp,
            rewardItemName = rewardItemName,
            rewardItemId = rewardItemId,
            rewardFavorXp = rewardFavorXp,
            npcId = npc.id,
            npcName = npc.name,
            npcRole = npc.role,
            npcEmoji = npc.avatarEmoji,
            npcLoreQuote = if (skill == OsrsSkill.SAILING && selected.targetEntityId.startsWith("parcel:")) {
                val parts = selected.targetEntityId.split(":")
                val sName = NpcData.findNpcById(parts.getOrNull(1) ?: "")?.name ?: "the sender"
                val dName = NpcData.findNpcById(parts.getOrNull(2) ?: "")?.name ?: "the recipient"
                "Ahoy! Urgent courier dispatch! Pick up the sealed parcel from $sName and deliver it safely to $dName!"
            } else {
                npc.greeting
            },
            favorTypeTitle = when (skill) {
                OsrsSkill.SAILING -> "Captain Barnaby's Parcel Delivery Favor"
                OsrsSkill.RUNECRAFT -> "Sedri's Rune-Making Favor"
                OsrsSkill.FIREMAKING -> "Sedri's Spirit Summoning Favor"
                else -> "${npc.name}'s ${npc.role} Favor"
            }
        )
    }

    /**
     * Checks if an existing favor is appropriate for the player's current level.
     * If the required level exceeds [skillLevel] or fishing level (for raw fish cooking), returns false.
     */
    fun isFavorLevelAppropriate(contract: SkillContract, skillLevel: Int, fishingLevel: Int = 1): Boolean {
        if (contract.skill == OsrsSkill.SAILING && !contract.targetEntityId.startsWith("parcel:")) {
            return false
        }
        if (contract.skill == OsrsSkill.ADVENTURING && (contract.npcId == "npc_barnaby" || contract.npcName.contains("Barnaby", ignoreCase = true))) {
            return false
        }
        val reqLevel = getRequiredLevelForFavorTarget(contract.targetEntityId, contract.taskTitle)
        if (skillLevel.coerceAtLeast(1) < reqLevel) return false
        if (contract.skill == OsrsSkill.COOKING) {
            val reqFishing = getRequiredFishingLevelForCookingTarget(contract.targetEntityId, contract.taskTitle)
            if (fishingLevel.coerceAtLeast(1) < reqFishing) return false
        }
        return true
    }

    /**
     * Returns the required Fishing level to obtain the raw fish for a cooking favor task.
     */
    fun getRequiredFishingLevelForCookingTarget(targetEntityId: String, taskTitle: String): Int {
        val id = targetEntityId.lowercase()
        val title = taskTitle.lowercase()
        return when {
            id == "item_raw_manta_ray" || title.contains("manta") -> 91
            id == "item_raw_dark_crab" || title.contains("dark crab") -> 85
            id == "item_raw_anglerfish" || title.contains("anglerfish") -> 82
            id == "item_raw_sea_turtle" || title.contains("sea turtle") -> 79
            id == "item_raw_shark" || title.contains("shark") -> 76
            id == "item_raw_monkfish" || title.contains("monkfish") -> 62
            id == "item_raw_swordfish" || title.contains("swordfish") -> 50
            id == "item_raw_bass" || title.contains("bass") -> 46
            id == "item_raw_lobster" || title.contains("lobster") -> 40
            id == "item_raw_tuna" || title.contains("tuna") -> 35
            id == "item_raw_salmon" || title.contains("salmon") -> 30
            id == "item_raw_pike" || title.contains("pike") -> 25
            id == "item_raw_trout" || title.contains("trout") -> 20
            id == "item_raw_mackerel" || title.contains("mackerel") -> 16
            id == "item_raw_herring" || title.contains("herring") -> 10
            id == "item_raw_sardine" || title.contains("sardine") -> 5
            id == "item_raw_shrimps" || title.contains("shrimp") -> 1
            else -> 1
        }
    }

    /**
     * Returns the minimum skill level required to perform a favor task based on its entity ID or title.
     */
    fun getRequiredLevelForFavorTarget(targetEntityId: String, taskTitle: String): Int {
        val id = targetEntityId.lowercase()
        val title = taskTitle.lowercase()
        return when {
            // Runecrafting
            id == "item_rune_wrath" || title.contains("wrath") -> 95
            id == "item_rune_soul" || title.contains("soul rune") -> 90
            id == "item_rune_blood" || title.contains("blood rune") -> 77
            id == "item_rune_death" || title.contains("death rune") -> 65
            id == "item_rune_law" || title.contains("law rune") -> 54
            id == "item_rune_nature" || title.contains("nature rune") -> 44
            id == "item_rune_astral" || title.contains("astral rune") -> 40
            id == "item_rune_chaos" || title.contains("chaos rune") -> 35
            id == "item_rune_cosmic" || title.contains("cosmic rune") -> 27
            id == "item_rune_body" || title.contains("body rune") -> 20
            id == "item_rune_fire" || title.contains("fire rune") -> 14
            id == "item_rune_earth" || title.contains("earth rune") -> 9
            id == "item_rune_water" || title.contains("water rune") -> 5
            id == "item_rune_mind" || title.contains("mind rune") -> 2

            // Mining & Smithing
            id == "item_amethyst" || title.contains("amethyst crystal") -> 92
            id == "item_runite_ore" || id == "item_rune_bar" || id.startsWith("item_rune_") || title.contains("rune") -> 85
            id == "item_adamantite_ore" || id == "item_adamant_bar" || id.startsWith("item_adamant_") || title.contains("adamant") -> 70
            id == "item_mithril_ore" || id == "item_mithril_bar" || id.startsWith("item_mithril_") || title.contains("mithril") || id == "item_mithril_nails" || title.contains("opalite") -> 50
            id == "item_gold_ore" || id == "item_gold_bar" || title.contains("gold") -> 40
            id == "item_coal" || id == "item_steel_bar" || id.startsWith("item_steel_") || title.contains("steel") || title.contains("coal") -> 30
            id == "item_silver_ore" || title.contains("silver ore") -> 20
            id == "item_iron_ore" || id == "item_iron_bar" || id.startsWith("item_iron_") || title.contains("iron") -> 15

            // Woodcutting & Trees
            id == "item_redwood_logs" || id == "tree_redwood" || title.contains("redwood") -> 85
            id == "tree_spirit" || title.contains("spirit tree") -> 88
            id == "tree_dragonfruit" || title.contains("dragonfruit") -> 81
            id == "item_magic_logs" || id == "tree_magic" || title.contains("magic tree") || title.contains("magic log") -> 75
            id == "tree_coconut" || title.contains("coconut") -> 72
            id == "tree_ironwood" || title.contains("ironwood") -> 68
            id == "tree_sakura" || title.contains("sakura") -> 64
            id == "item_yew_logs" || id == "tree_yew" || title.contains("yew") -> 60
            id == "tree_palm" || title.contains("palm tree") -> 57
            id == "tree_cedar" || title.contains("cedar") -> 52
            id == "tree_peach" || title.contains("peach") -> 48
            id == "item_maple_logs" || id == "tree_maple" || title.contains("maple") -> 45
            id == "tree_apricot" || title.contains("apricot") -> 39
            id == "tree_pine" || title.contains("pine") -> 38
            id == "item_teak_logs" || id == "item_teak_plank" || title.contains("teak") -> 35
            id == "tree_cherry" || title.contains("cherry") -> 33
            id == "item_willow_logs" || id == "tree_willow" || title.contains("willow") -> 30
            id == "tree_apple" || id == "tree_birch" || id == "tree_oak" || title.contains("apple tree") || title.contains("birch tree") || title.contains("oak tree") -> 25
            id == "item_oak_logs" || id == "item_oak_plank" || title.contains("oak") -> 15

            // Fishing
            id == "item_raw_manta_ray" || title.contains("manta") -> 91
            id == "item_raw_anglerfish" || title.contains("anglerfish") -> 82
            id == "item_raw_shark" || title.contains("shark") -> 76
            id == "item_raw_monkfish" || title.contains("monkfish") -> 62
            id == "item_raw_swordfish" || title.contains("swordfish") -> 50
            id == "item_raw_lobster" || title.contains("lobster") -> 40
            id == "item_raw_tuna" || title.contains("tuna") -> 35
            id == "item_raw_salmon" || title.contains("salmon") -> 30
            id == "item_raw_trout" || title.contains("trout") -> 20
            id == "item_raw_herring" || title.contains("herring") -> 10
            id == "item_raw_sardine" || title.contains("sardine") -> 5

            // Farming Herbs & Produce
            id == "item_clean_vervain" || title.contains("vervain") -> 92
            id == "item_clean_moonflower" || title.contains("moonflower") -> 89
            id == "item_clean_mystic_sage" || title.contains("mystic sage") -> 85
            id == "item_clean_silverleaf" || title.contains("silverleaf") -> 78
            id == "item_grimy_torstol" || id == "item_clean_torstol" || title.contains("torstol") -> 75
            id == "item_grimy_dwarf_weed" || id == "item_clean_dwarf_weed" || title.contains("dwarf weed") -> 70
            id == "item_clean_wintergreen" || title.contains("wintergreen") -> 68
            id == "item_grimy_cadantine" || id == "item_clean_cadantine" || title.contains("cadantine") -> 65
            id == "item_watermelon" || title.contains("watermelon") -> 60
            id == "item_grimy_snapdragon" || id == "item_clean_snapdragon" || title.contains("snapdragon") -> 59
            id == "item_clean_ironleaf" || title.contains("ironleaf") -> 55
            id == "item_grimy_kwuarm" || id == "item_clean_kwuarm" || title.contains("kwuarm") -> 54
            id == "item_pumpkin" || title.contains("pumpkin") -> 52
            id == "item_grimy_avantoe" || id == "item_clean_avantoe" || title.contains("avantoe") -> 48
            id == "item_strawberry" || title.contains("strawberry") -> 45
            id == "item_grimy_irit" || id == "item_clean_irit" || title.contains("irit") -> 40
            id == "item_clean_sunleaf" || title.contains("sunleaf") -> 40
            id == "item_sweetcorn" || title.contains("sweetcorn") -> 35
            id == "item_grimy_toadflax" || id == "item_clean_toadflax" || title.contains("toadflax") -> 30
            id == "item_clean_lavender" || title.contains("lavender") -> 28
            id == "item_grimy_ranarr" || id == "item_clean_ranarr" || title.contains("ranarr") -> 25
            id == "item_tomato" || title.contains("tomato") -> 20
            id == "item_grimy_harralander" || id == "item_clean_harralander" || title.contains("harralander") -> 20
            id == "item_clean_wild_thyme" || title.contains("wild thyme") -> 18
            id == "item_carrot" || title.contains("carrot") -> 15
            id == "item_grimy_tarromin" || id == "item_clean_tarromin" || title.contains("tarromin") -> 11
            id == "item_cabbage" || title.contains("cabbage") -> 10
            id == "item_clean_meadow_mint" || title.contains("meadow mint") -> 8
            id == "item_grimy_marrentill" || id == "item_clean_marrentill" || title.contains("marrentill") -> 5
            id == "item_onion" || title.contains("onion") -> 5
            id == "item_grimy_guam" || id == "item_clean_guam" || title.contains("guam") -> 3

            // Thieving
            id == "elf" || title.contains("elf") || title.contains("elves") -> 85
            id == "hero" || title.contains("hero") -> 80
            id == "stall_gem" || title.contains("gem stall") -> 75
            id == "tzhaar" || title.contains("tzhaar") -> 75
            id == "paladin" || title.contains("paladin") -> 70
            id == "stall_scimitar" || title.contains("scimitar stall") -> 65
            id == "ardougne_knight" || title.contains("ardougne knight") -> 55
            id == "desert_bandit" || title.contains("desert bandit") -> 53
            id == "stall_silver" || title.contains("silver stall") -> 50
            id == "guard" || title.contains("guard") -> 40
            id == "master_farmer" || title.contains("master farmer") -> 38
            id == "stall_fur" || title.contains("fur stall") -> 35
            id == "street_urchin" || title.contains("urchin") -> 25
            id == "stall_silk" || id == "silk_merchant" || title.contains("silk") -> 20
            id == "farmer" || title.contains("farmer") -> 10
            id == "stall_bakery" || title.contains("bakery") -> 5

            // Slayer & Hunter
            title.contains("dark beast") || title.contains("hydra") -> 85
            title.contains("gargoyle") || title.contains("abyssal demon") -> 75
            title.contains("black chinchompa") -> 73
            title.contains("red chinchompa") -> 63
            title.contains("bloodveld") || title.contains("spectre") -> 60
            title.contains("grey chinchompa") -> 53
            title.contains("spotted kebbit") -> 43
            title.contains("basilisk") || title.contains("infernal mage") -> 40
            title.contains("swamp lizard") -> 29
            title.contains("rockslug") || title.contains("desert lizard") -> 20
            title.contains("ruby harvest") -> 15

            // Herblore Potions
            id == "potion_saradomin_brew" || title.contains("saradomin brew") -> 81
            id == "item_stamina_potion" || title.contains("stamina") -> 77
            id == "potion_super_defence" || title.contains("super defence") -> 66
            id == "potion_super_restore" || title.contains("super restore") -> 63
            id == "potion_super_strength" || title.contains("super strength") -> 55
            id == "potion_super_attack" || title.contains("super attack") -> 45
            id == "potion_prayer" || title.contains("prayer") -> 38
            id == "potion_defence" || title.contains("defence potion") -> 30
            id == "potion_energy" || title.contains("energy potion") -> 26
            id == "potion_strength" || title.contains("strength potion") -> 12
            id == "potion_attack" || title.contains("attack potion") -> 3

            // Construction
            id == "item_mahogany_plank" || title.contains("mahogany plank") -> 50
            id == "item_teak_plank" || id == "item_steel_nails" || title.contains("teak plank") || title.contains("steel nails") -> 35
            id == "item_oak_plank" || id == "item_iron_nails" || title.contains("oak plank") || title.contains("iron nails") -> 15
            id == "item_limestone_brick" || title.contains("limestone") -> 10

            // Totems / Effigies
            id == "item_effigy_blood" || title.contains("blood effig") -> 77
            id == "item_effigy_death" || title.contains("death effig") -> 65
            id == "item_totem_golem_steel" || title.contains("steel golem") -> 50
            id == "item_effigy_nature" || title.contains("nature effig") -> 44
            id == "item_totem_golem_iron" || title.contains("iron golem") || id == "item_effigy_chaos" || title.contains("chaos effig") -> 30
            id == "item_effigy_cosmic" || title.contains("cosmic effig") -> 27
            id == "item_totem_golem_bronze" || title.contains("bronze golem") -> 15
            id == "item_effigy_earth" || title.contains("earth effig") -> 10
            id == "item_totem_dreadfowl" || title.contains("dreadfowl") -> 4

            else -> 1
        }
    }

    /**
     * Determines whether an action on [actionItemId] / [actionTitle] matches this contract's [targetEntityId].
     * Prevents cross-contamination where e.g. mining ore was progressing nail forging favors.
     */
    fun isContractTargetMatch(contract: SkillContract, actionItemId: String?, actionTitle: String? = null): Boolean {
        val target = contract.targetEntityId.trim().lowercase()
        if (target.isBlank() || target == "any" || target == "all") return true

        val action = (actionItemId ?: "").trim().lowercase()
        val title = (actionTitle ?: "").trim().lowercase()

        // Direct exact match
        if (action.isNotEmpty() && (action == target || action == "item_$target" || "item_$action" == target)) {
            return true
        }

        // Special handling per skill category
        return when (contract.skill) {
            OsrsSkill.SMITHING -> {
                if (target == "item_nails" || target == "item_bronze_nails") {
                    action == "item_nails" || action == "item_bronze_nails" || action == "bronze_nails" || action == "nails"
                } else if (target.contains("nails")) {
                    action == target || action.contains(target.removePrefix("item_"))
                } else if (target.endsWith("_bar")) {
                    action == target || action == target.removePrefix("item_")
                } else if (target.endsWith("_ore")) {
                    action == target || action == target.removePrefix("item_")
                } else {
                    action == target
                }
            }
            OsrsSkill.CONSTRUCTION -> {
                if (target == "poh_furniture_any") true
                else if (target == "item_nails" || target == "item_bronze_nails") {
                    action == "item_nails" || action == "item_bronze_nails" || action == "bronze_nails" || action == "nails"
                } else if (target.contains("nails")) {
                    action == target || action.contains(target.removePrefix("item_"))
                } else if (target.contains("plank")) {
                    action == target || action == target.removePrefix("item_")
                } else {
                    action == target
                }
            }
            OsrsSkill.COOKING -> {
                if (target == "cooking_any") true
                else {
                    val cleanTarget = target.removePrefix("item_").removePrefix("raw_").removePrefix("cooked_")
                    val cleanAction = action.removePrefix("item_").removePrefix("raw_").removePrefix("cooked_")
                    cleanAction.isNotEmpty() && (cleanAction == cleanTarget || cleanAction.contains(cleanTarget) || cleanTarget.contains(cleanAction))
                }
            }
            OsrsSkill.WOODCUTTING -> {
                val cleanTarget = target.removePrefix("item_").removePrefix("tree_").removeSuffix("_logs")
                val cleanAction = action.removePrefix("item_").removePrefix("tree_").removeSuffix("_logs")
                cleanAction.isNotEmpty() && (cleanAction == cleanTarget || action == target)
            }
            OsrsSkill.FISHING -> {
                val cleanTarget = target.removePrefix("item_").removePrefix("raw_")
                val cleanAction = action.removePrefix("item_").removePrefix("raw_")
                cleanAction.isNotEmpty() && (cleanAction == cleanTarget || action == target)
            }
            OsrsSkill.FARMING -> {
                val cleanTarget = target.removePrefix("item_").removePrefix("tree_").removePrefix("clean_").removePrefix("seed_")
                val cleanAction = action.removePrefix("item_").removePrefix("tree_").removePrefix("clean_").removePrefix("seed_")
                cleanAction.isNotEmpty() && (cleanAction == cleanTarget || action == target || (title.isNotEmpty() && title.contains(cleanTarget)))
            }
            OsrsSkill.HERBLORE -> {
                val cleanTarget = target.removePrefix("item_").removePrefix("grimy_").removePrefix("clean_").removePrefix("crushed_").removePrefix("potion_")
                val cleanAction = action.removePrefix("item_").removePrefix("grimy_").removePrefix("clean_").removePrefix("crushed_").removePrefix("potion_")
                cleanAction.isNotEmpty() && (cleanAction == cleanTarget || action == target)
            }
            OsrsSkill.THIEVING -> {
                val cleanTarget = target.removePrefix("npc_").removePrefix("stall_")
                val cleanAction = action.removePrefix("npc_").removePrefix("stall_")
                cleanAction.isNotEmpty() && (cleanAction == cleanTarget || action == target)
            }
            OsrsSkill.SLAYER -> {
                val cleanTarget = target.removePrefix("monster_")
                val cleanAction = action.removePrefix("monster_")
                cleanAction.isNotEmpty() && (cleanAction == cleanTarget || action == target)
            }
            OsrsSkill.HUNTER -> {
                val cleanTarget = target.removePrefix("creature_").removePrefix("item_")
                val cleanAction = action.removePrefix("creature_").removePrefix("item_")
                cleanAction.isNotEmpty() && (cleanAction == cleanTarget || action == target)
            }
            OsrsSkill.RUNECRAFT -> {
                val cleanTarget = target.removePrefix("item_").removePrefix("rune_")
                val cleanAction = action.removePrefix("item_").removePrefix("rune_")
                cleanAction.isNotEmpty() && (cleanAction == cleanTarget || action == target)
            }
            OsrsSkill.FIREMAKING -> {
                val cleanTarget = target.removePrefix("item_").removePrefix("totem_").removePrefix("effigy_")
                val cleanAction = action.removePrefix("item_").removePrefix("totem_").removePrefix("effigy_")
                cleanAction.isNotEmpty() && (cleanAction == cleanTarget || action == target)
            }
            else -> {
                action.isNotEmpty() && (action == target || action.contains(target) || target.contains(action))
            }
        }
    }
}
