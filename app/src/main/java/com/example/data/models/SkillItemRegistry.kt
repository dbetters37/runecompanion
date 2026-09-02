package com.example.data.models

object SkillItemRegistry {

    /**
     * Maps every item in the game to exactly ONE OsrsSkill.
     */
    fun getItemSkill(item: InventoryItem): OsrsSkill {
        // 1. Explicit bonusXpSkill
        if (item.bonusXpSkill != null) {
            return item.bonusXpSkill
        }

        val id = item.id.lowercase()
        val name = item.name.lowercase()

        // 2. Deterministic item rules
        return when {
            // WOODCUTTING
            item.category == ItemCategory.LOGS_WOOD ||
            id.contains("log") || name.contains("log") || name.contains("tree") ||
            name.contains("axe") || name.contains("hatchet") || id.contains("hatchet") ||
            name.contains("woodcutting") || name.contains("ent bark") -> OsrsSkill.WOODCUTTING

            // FORGING (Ores, Gems, Bars, Anvils)
            id.contains("ore") || name.contains("ore") ||
            id.contains("pickaxe") || name.contains("pickaxe") ||
            id.contains("essence") || name.contains("essence") ||
            id.contains("clay") || name.contains("clay") ||
            id.contains("uncut") || name.contains("uncut") ||
            id.contains("gem") || name.contains("gem") ||
            id.contains("granite") || name.contains("granite") ||
            id.contains("sandstone") || name.contains("amethyst") ||
            id.contains("bar") || name.contains("bar") ||
            id.contains("cannonball") || name.contains("cannonball") ||
            name.contains("anvil") || name.contains("forge") ||
            (item.category == ItemCategory.BARS_ORES) -> OsrsSkill.SMITHING

            // FISHING
            id.contains("raw_") || name.startsWith("raw ") ||
            id.contains("harpoon") || name.contains("harpoon") ||
            name.contains("fishing") || name.contains("bait") ||
            id.contains("feather") || id.contains("net") || name.contains("net") ||
            id.contains("lobster_pot") || name.contains("tackle") -> OsrsSkill.FISHING

            // COOKING
            item.category == ItemCategory.FOOD ||
            id.contains("cooked") || name.startsWith("cooked ") ||
            name.contains("bread") || name.contains("pie") || name.contains("stew") ||
            name.contains("chowder") || name.contains("cake") || name.contains("tonic") ||
            name.contains("elixir") || name.contains("nectar") || name.contains("sweets") ||
            name.contains("pizza") || name.contains("curry") || name.contains("wine") -> OsrsSkill.COOKING

            // FIREMAKING
            id.contains("tinderbox") || name.contains("tinderbox") ||
            id.contains("ashes") || name.contains("ashes") ||
            id.contains("pyre") || name.contains("fire") ||
            name.contains("torch") || name.contains("candle") || name.contains("lantern") -> OsrsSkill.FIREMAKING

            // HERBLORE
            item.category == ItemCategory.POTION ||
            id.contains("potion") || name.contains("potion") ||
            id.contains("herb") || name.contains("herb") ||
            id.contains("vial") || name.contains("vial") ||
            id.contains("brew") || name.contains("brew") ||
            name.contains("mortar") || name.contains("pestle") -> OsrsSkill.HERBLORE

            // FARMING
            item.category == ItemCategory.SEEDS || item.category == ItemCategory.HERBS_FARMING ||
            id.contains("seed") || name.contains("seed") ||
            name.contains("compost") || name.contains("sapling") ||
            name.contains("rake") || name.contains("watering can") || name.contains("secateurs") -> OsrsSkill.FARMING

            // RUNECRAFT
            item.category == ItemCategory.RUNES_MAGIC ||
            id.contains("rune") || name.contains("rune") ||
            id.contains("talisman") || name.contains("talisman") ||
            id.contains("tiara") || name.contains("tiara") -> OsrsSkill.RUNECRAFT

            // MAGIC
            id.contains("staff") || name.contains("staff") ||
            id.contains("wand") || name.contains("wand") ||
            id.contains("spell") || name.contains("incantation") ||
            id.contains("tome") || name.contains("tome") ||
            name.contains("teleport") || name.contains("magic") -> OsrsSkill.MAGIC

            // CONSTRUCTION
            item.category == ItemCategory.CONSTRUCTION ||
            id.contains("plank") || name.contains("plank") ||
            id.contains("nails") || name.contains("nails") ||
            name.contains("saw") || name.contains("hut") || name.contains("poh") -> OsrsSkill.CONSTRUCTION

            // MAGIC & OFFERINGS
            item.category == ItemCategory.BONES ||
            id.contains("bone") || name.contains("bone") ||
            id.contains("ensouled") || name.contains("altar") || id.contains("prayer") -> OsrsSkill.MAGIC

            // FLETCHING
            id.contains("bow") || name.contains("bow") ||
            id.contains("arrow") || name.contains("arrow") ||
            id.contains("crossbow") || name.contains("bolts") ||
            id.contains("leather") || name.contains("leather") ||
            id.contains("dart") || name.contains("dart") || id.contains("fletch") -> OsrsSkill.FLETCHING

            // AGILITY
            id.contains("graceful") || name.contains("graceful") ||
            id.contains("mark_of_grace") || name.contains("mark of grace") ||
            id.contains("agility") -> OsrsSkill.AGILITY

            // THIEVING
            id.contains("lockpick") || name.contains("lockpick") ||
            id.contains("coin_pouch") || name.contains("coin pouch") ||
            id.contains("ring") || name.contains("amulet") || name.contains("necklace") ||
            name.contains("rogue") || name.contains("stolen") -> OsrsSkill.THIEVING

            // SLAYER
            id.contains("slayer") || name.contains("slayer") ||
            name.contains("earmuffs") || name.contains("facemask") || name.contains("broad") -> OsrsSkill.SLAYER

            // HUNTER
            id.contains("trap") || name.contains("trap") ||
            id.contains("snare") || name.contains("snare") ||
            id.contains("chinchompa") || name.contains("impling") || name.contains("pelt") -> OsrsSkill.HUNTER

            // ATTACK / DEFENCE / RANGED (Equipment & Combat)
            item.equipmentSlot == EquipmentSlot.WEAPON -> {
                if (name.contains("bow") || name.contains("dart") || name.contains("crossbow")) OsrsSkill.RANGED
                else if (name.contains("staff") || name.contains("wand")) OsrsSkill.MAGIC
                else OsrsSkill.ATTACK
            }
            item.equipmentSlot == EquipmentSlot.BODY || item.equipmentSlot == EquipmentSlot.LEGS ||
            item.equipmentSlot == EquipmentSlot.HEAD || item.equipmentSlot == EquipmentSlot.SHIELD ||
            item.equipmentSlot == EquipmentSlot.BOOTS || item.equipmentSlot == EquipmentSlot.GLOVES ||
            item.equipmentSlot == EquipmentSlot.CAPE || item.equipmentSlot == EquipmentSlot.AMULET ||
            item.equipmentSlot == EquipmentSlot.RING -> OsrsSkill.DEFENCE

            // DIVINATION
            id.contains("divination") || name.contains("divination") ||
            id.contains("memory") || name.contains("memory") ||
            id.contains("energy") || name.contains("energy") ||
            id.contains("chronote") || name.contains("chronote") ||
            id.contains("divine") || name.contains("divine") ||
            id.contains("wisp") || name.contains("wisp") ||
            id.contains("effigy") || name.contains("effigy") ||
            id.contains("anima") || name.contains("anima") ||
            id.contains("rift") || name.contains("transmute") -> OsrsSkill.DIVINATION

            // SAILING
            id.contains("sailing") || name.contains("sailing") ||
            id.contains("boat") || name.contains("boat") ||
            id.contains("ship") || name.contains("ship") ||
            id.contains("anchor") || name.contains("anchor") ||
            id.contains("compass") || name.contains("compass") ||
            id.contains("sextant") || name.contains("sextant") ||
            id.contains("sail") || name.contains("sail") ||
            id.contains("rudder") || name.contains("salvage") ||
            id.contains("chart") || name.contains("chart") ||
            id.contains("island") || name.contains("island") -> OsrsSkill.SAILING

            // ADVENTURING
            id.contains("adventur") || name.contains("adventur") ||
            id.contains("dungeon") || name.contains("dungeon") ||
            id.contains("key") || name.contains("key") ||
            id.contains("badge") || name.contains("badge") ||
            id.contains("trophy") || name.contains("trophy") ||
            id.contains("league") || name.contains("league") ||
            id.contains("relic") || name.contains("relic") ||
            id.contains("scroll") || name.contains("scroll") ||
            id.contains("apex") || name.contains("apex") -> OsrsSkill.ADVENTURING

            // Category fallbacks
            item.category == ItemCategory.SKILL_TOOL -> OsrsSkill.SMITHING
            item.category == ItemCategory.TOY -> OsrsSkill.AGILITY
            item.category == ItemCategory.MISC -> OsrsSkill.ADVENTURING

            else -> OsrsSkill.HITPOINTS
        }
    }

    data class SkillItemEntry(
        val item: InventoryItem,
        val inventoryQty: Int,
        val bankQty: Int,
        val equippedQty: Int,
        val assignedSkill: OsrsSkill
    ) {
        val totalOwned: Int get() = inventoryQty + bankQty + equippedQty
        val isOwned: Boolean get() = totalOwned > 0
    }

    fun getItemsForSkill(
        skill: OsrsSkill,
        inventoryItems: List<InventoryItem>,
        bankItems: List<InventoryItem>,
        equippedItems: Map<EquipmentSlot, InventoryItem>
    ): List<SkillItemEntry> {
        val allKnownItemsMap = LinkedHashMap<String, InventoryItem>()

        // 1. All shop / default items
        DefaultItems.ALL_SHOP_ITEMS.forEach { item ->
            if (getItemSkill(item) == skill) {
                allKnownItemsMap[item.id] = item
            }
        }

        // 2. Inventory items
        inventoryItems.forEach { item ->
            if (getItemSkill(item) == skill && !allKnownItemsMap.containsKey(item.id)) {
                allKnownItemsMap[item.id] = item
            }
        }

        // 3. Bank items
        bankItems.forEach { item ->
            if (getItemSkill(item) == skill && !allKnownItemsMap.containsKey(item.id)) {
                allKnownItemsMap[item.id] = item
            }
        }

        // 4. Equipped items
        equippedItems.values.forEach { item ->
            if (getItemSkill(item) == skill && !allKnownItemsMap.containsKey(item.id)) {
                allKnownItemsMap[item.id] = item
            }
        }

        // Map each item to its inventory, bank, equipped counts
        val entries = allKnownItemsMap.values.map { item ->
            val invQty = inventoryItems.filter { it.id == item.id }.sumOf { it.quantity }
            val bnkQty = bankItems.filter { it.id == item.id }.sumOf { it.quantity }
            val eqpQty = if (equippedItems.values.any { it.id == item.id }) 1 else 0

            SkillItemEntry(
                item = item,
                inventoryQty = invQty,
                bankQty = bnkQty,
                equippedQty = eqpQty,
                assignedSkill = skill
            )
        }

        // Sort: Owned items first, then by totalOwned descending, then by name
        return entries.sortedWith(
            compareByDescending<SkillItemEntry> { it.isOwned }
                .thenByDescending { it.totalOwned }
                .thenBy { it.item.name }
        )
    }
}
