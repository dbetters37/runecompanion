missing_code = """
    // ==========================================
    // NPC FAVOR SYSTEM & MINIMIZATION
    // ==========================================
    private val _npcFavorMap = MutableStateFlow<Map<String, Pair<Int, Long>>>(emptyMap())
    val npcFavorMap: StateFlow<Map<String, Pair<Int, Long>>> = _npcFavorMap.asStateFlow()

    private val _minimizedNpcSessionSet = MutableStateFlow<Set<String>>(emptySet())
    val minimizedNpcSessionSet: StateFlow<Set<String>> = _minimizedNpcSessionSet.asStateFlow()

    fun normalizeNpcId(npcId: String): String {
        return npcId.lowercase().removePrefix("npc_").trim()
    }

    fun isNpcSessionMinimized(npcId: String): Boolean {
        val norm = normalizeNpcId(npcId)
        return _minimizedNpcSessionSet.value.contains(norm) || _minimizedNpcSessionSet.value.contains(npcId)
    }

    fun minimizeNpcForSession(npcId: String) {
        val norm = normalizeNpcId(npcId)
        val current = _minimizedNpcSessionSet.value.toMutableSet()
        current.add(norm)
        current.add(npcId)
        _minimizedNpcSessionSet.value = current
    }

    fun getNpcFavorLevel(npcId: String): Int {
        val norm = normalizeNpcId(npcId)
        return _npcFavorMap.value[norm]?.first
            ?: _npcFavorMap.value[npcId]?.first
            ?: _npcFavorMap.value["npc_$norm"]?.first
            ?: 1
    }

    fun getNpcFavorXp(npcId: String): Long {
        val norm = normalizeNpcId(npcId)
        return _npcFavorMap.value[norm]?.second
            ?: _npcFavorMap.value[npcId]?.second
            ?: _npcFavorMap.value["npc_$norm"]?.second
            ?: 0L
    }

    fun getRequiredXpForFavorLevel(level: Int): Long {
        val safeLvl = level.coerceIn(1, 100)
        return (safeLvl * 100L * (1.0 + safeLvl * 0.1)).toLong()
    }

    fun addNpcFavorXp(npcId: String, xpGained: Long, npcName: String = "", reason: String = "") {
        val norm = normalizeNpcId(npcId)
        val currentPair = _npcFavorMap.value[norm] ?: Pair(1, 0L)
        var level = currentPair.first
        var xp = currentPair.second + xpGained
        var req = getRequiredXpForFavorLevel(level)
        var leveledUp = false

        while (xp >= req && level < 50) {
            xp -= req
            level++
            req = getRequiredXpForFavorLevel(level)
            leveledUp = true
        }

        val updated = _npcFavorMap.value.toMutableMap()
        updated[norm] = Pair(level, xp)
        updated[npcId] = Pair(level, xp)
        updated["npc_$norm"] = Pair(level, xp)
        _npcFavorMap.value = updated

        viewModelScope.launch {
            repository.saveNpcFavorProgress(
                com.example.data.db.NpcFavorEntity(
                    petTypeName = petState.value.petType.name,
                    npcId = norm,
                    npcName = npcName.ifBlank { norm.replaceFirstChar(Char::uppercaseChar) },
                    favorLevel = level,
                    favorXp = xp,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }

        if (leveledUp) {
            val displayName = npcName.ifBlank { norm.replaceFirstChar(Char::uppercaseChar) }
            addChatMessage("🌟 NPC Favor Level UP! You reached Favor Level $level with $displayName! 🎉")
        }
    }

    // ==========================================
    // EQUIPMENT LOADOUTS
    // ==========================================
    private val _equipmentLoadouts = MutableStateFlow<List<com.example.data.models.EquipmentLoadout>>(emptyList())
    val equipmentLoadouts: StateFlow<List<com.example.data.models.EquipmentLoadout>> = _equipmentLoadouts.asStateFlow()

    fun saveCurrentLoadout(name: String) {
        val currentEquipped = equippedItems.value
        val itemsMap = currentEquipped.mapValues { it.value.id }
        val newLoadout = com.example.data.models.EquipmentLoadout(
            id = java.util.UUID.randomUUID().toString(),
            name = name.ifBlank { "Loadout ${_equipmentLoadouts.value.size + 1}" },
            items = itemsMap
        )
        _equipmentLoadouts.value = _equipmentLoadouts.value + newLoadout
        addChatMessage("🛡️ Saved equipment loadout: '${newLoadout.name}'")
    }

    fun equipLoadout(loadout: com.example.data.models.EquipmentLoadout) {
        viewModelScope.launch {
            for ((slot, itemId) in loadout.items) {
                val item = com.example.data.models.DefaultItems.getItemById(itemId)
                if (item.id.isNotBlank()) {
                    equipItemDirect(item, slot)
                }
            }
            addChatMessage("🛡️ Equipped loadout: '${loadout.name}'")
        }
    }

    fun deleteLoadout(id: String) {
        _equipmentLoadouts.value = _equipmentLoadouts.value.filter { it.id != id }
        addChatMessage("🗑️ Deleted equipment loadout.")
    }

    fun equipStrongestGear(slot: com.example.data.models.EquipmentSlot? = null) {
        viewModelScope.launch {
            val allAvailable = (inventoryItems.value + bankItems.value).distinctBy { it.id }
            val slotsToEquip = if (slot != null) listOf(slot) else com.example.data.models.EquipmentSlot.entries

            for (s in slotsToEquip) {
                val candidate = allAvailable.filter { 
                    (it.equipmentSlot == s || com.example.data.models.DefaultItems.getItemById(it.id).equipmentSlot == s) && it.quantity > 0 
                }.maxByOrNull { calculateEquipmentScore(it) }

                if (candidate != null) {
                    equipItemDirect(candidate, s)
                }
            }
            addChatMessage("⚔️ Auto-equipped strongest gear!")
        }
    }

    private suspend fun equipItemDirect(item: com.example.data.models.InventoryItem, slot: com.example.data.models.EquipmentSlot) {
        val norm = com.example.data.models.DefaultItems.normalizeItemId(item.id)
        val catalogItem = com.example.data.models.DefaultItems.getItemById(norm)
        val currentEquipped = _equippedItems.value.toMutableMap()
        val old = currentEquipped[slot]
        if (old != null) {
            saveBankItem(old.id, (bankItems.value.find { it.id == old.id }?.quantity ?: 0) + 1)
        }
        currentEquipped[slot] = catalogItem
        _equippedItems.value = currentEquipped
        val petType = petState.value.petType.name
        repository.saveEquippedItem(petType, slot.name, catalogItem.id)
    }

    // ==========================================
    // AFK STATION BRIDGES & TOGGLES
    // ==========================================
    val isAfkBoneBuryingActive: StateFlow<Boolean> get() = AfkEngine.isAfkBoneBuryingActive
    val isAfkSailingActive: StateFlow<Boolean> get() = AfkEngine.isAfkSailingActive
    val isAfkRunecraftingActive: StateFlow<Boolean> get() = AfkEngine.isAfkRunecraftingActive
    val isAfkThievingActive: StateFlow<Boolean> get() = AfkEngine.isAfkThievingActive
    val isAfkCatacombsActive: StateFlow<Boolean> get() = AfkEngine.isAfkCatacombsActive
    val isAfkFarmingActive: StateFlow<Boolean> get() = AfkEngine.isAfkFarmingActive

    fun toggleAfkBoneBurying() {
        val currentInv = inventoryItems.value
        val hasBones = currentInv.any { it.id.contains("bone") && it.quantity > 0 }
        if (!isAfkBoneBuryingActive.value && !hasBones) {
            stopAllAfkStations()
            addChatMessage("🦴 Bone Altar: Cannot start! You have no bones in your backpack.")
            return
        }
        val next = !isAfkBoneBuryingActive.value
        if (next && !canStartAfkOrHungerAction("Bone Burying")) return
        stopAllAfkStations()
        if (next) AfkEngine.startActivity(AfkActivityType.BONE_BURYING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🦴 AFK Bone Burying is now ${if (next) "RUNNING (Offering bones -> Prayer XP)" else "STOPPED"}")
        saveAfkStateToPrefs()
    }

    fun toggleAfkSailing() {
        val next = !isAfkSailingActive.value
        if (next && !canStartAfkOrHungerAction("Sailing")) return
        stopAllAfkStations()
        if (next) AfkEngine.startActivity(AfkActivityType.SAILING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("⛵ AFK Sailing Voyage is now ${if (next) "SAILING (Navigating uncharted waters -> Sailing XP + Salvage)" else "ANCHORED / STOPPED"}")
        saveAfkStateToPrefs()
    }

    fun toggleAfkRunecrafting() {
        val currentInv = inventoryItems.value
        val hasEssence = currentInv.any { (it.id.contains("essence") || it.id == "item_rune_essence") && it.quantity > 0 }
        if (!isAfkRunecraftingActive.value && !hasEssence) {
            stopAllAfkStations()
            addChatMessage("🔮 Runecrafting Altar: Cannot start! You have no rune essence in inventory.")
            return
        }
        val next = !isAfkRunecraftingActive.value
        if (next && !canStartAfkOrHungerAction("Runecrafting")) return
        stopAllAfkStations()
        if (next) AfkEngine.startActivity(AfkActivityType.RUNECRAFTING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🔮 AFK Runecrafting is now ${if (next) "RUNNING (Infusing runes -> Runecraft XP)" else "STOPPED"}")
        saveAfkStateToPrefs()
    }

    fun toggleAfkThieving() {
        val next = !isAfkThievingActive.value
        if (next && !canStartAfkOrHungerAction("Thieving")) return
        stopAllAfkStations()
        if (next) AfkEngine.startActivity(AfkActivityType.THIEVING, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🧤 AFK Pickpocketing is now ${if (next) "ACTIVE (Pickpocketing target -> Coins + Thieving XP)" else "STOPPED"}")
        saveAfkStateToPrefs()
    }

    fun toggleAfkCatacombs() {
        val next = !isAfkCatacombsActive.value
        if (next && !canStartAfkOrHungerAction("Catacombs Exploration")) return
        stopAllAfkStations()
        if (next) AfkEngine.startActivity(AfkActivityType.CATACOMBS, pohPrefs) else AfkEngine.stopAll(pohPrefs)
        addChatMessage("🕯️ AFK Catacombs Dungeon is now ${if (next) "EXPLORING (Delving deep -> Relics + Adventuring XP)" else "STOPPED"}")
        saveAfkStateToPrefs()
    }

    fun startPickpocketingNpc(npcId: String) {
        _selectedThievingNpcId.value = npcId
        if (!canStartAfkOrHungerAction("Pickpocketing")) return
        stopAllAfkStations()
        AfkEngine.startActivity(AfkActivityType.THIEVING, pohPrefs)
        addChatMessage("🧤 Started pickpocketing ${npcId.replaceFirstChar(Char::uppercaseChar)}!")
        saveAfkStateToPrefs()
    }

    fun stopPickpocketing() {
        if (isAfkThievingActive.value) {
            AfkEngine.stopAll(pohPrefs)
            addChatMessage("🛑 Stopped pickpocketing.")
            saveAfkStateToPrefs()
        }
    }

    fun getThievingGpMultiplier(): Double {
        val pet = petState.value
        val renFavor = getNpcFavorLevel("ren")
        val base = 1.0 + (renFavor * 0.03)
        return base
    }

    fun stopAllAfkStationsExcept(exceptType: AfkActivityType?) {
        if (exceptType == null) {
            AfkEngine.stopAll(pohPrefs)
        } else {
            AfkEngine.startActivity(exceptType, pohPrefs)
        }
        saveAfkStateToPrefs()
    }

    fun processOfflineQuestProgress(offlineSeconds: Long) {
        // Offline quest progress tick
    }

    fun processHusbandryTick() {
        // POF Husbandry background tick
    }

    fun processAfkFarmingTick() {
        // Farming AFK tick
    }

    fun buryBonesFromInventory(isAfk: Boolean = false) {
        viewModelScope.launch {
            val candidateBones = inventoryItems.value.filter { it.id.contains("bone") && it.quantity > 0 }
            if (candidateBones.isEmpty()) {
                if (isAfk) {
                    AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🦴 Prayer: Out of bones! AFK Prayer Altar turned OFF.")
                } else {
                    addChatMessage("🦴 Prayer: No bones found in backpack to bury!")
                }
                return@launch
            }
            val bone = candidateBones.first()
            deductItemCombined(bone.id, 1)
            val xp = when {
                bone.id.contains("dragon") -> 72L
                bone.id.contains("big") -> 15L
                else -> 5L
            }
            addSkillXp(OsrsSkill.PRAYER, xp)
            addChatMessage("🦴 Buried ${bone.name}: +${xp} Prayer XP!")
        }
    }

    fun sailOnAfkTick() {
        viewModelScope.launch {
            val xp = 25L
            addSkillXp(OsrsSkill.SAILING, xp)
            progressSkillContract(OsrsSkill.SAILING, 1, "sailing")
            if ((1..100).random() <= 20) {
                val salvageItems = listOf("item_salvage_driftwood", "item_salvage_copper_coin", "item_seaweed", "item_oyster")
                val found = salvageItems.random()
                saveBankItem(found, (bankItems.value.find { it.id == found }?.quantity ?: 0) + 1)
                addChatMessage("⛵ Sailing: Salvaged 1x ${found.replace("item_", "").replace("_", " ")} from open waters!")
            }
        }
    }

    fun processAfkRunecraftingTick() {
        viewModelScope.launch {
            val essence = inventoryItems.value.find { (it.id == "item_rune_essence" || it.id == "item_pure_essence") && it.quantity > 0 }
            if (essence == null) {
                AfkEngine.stopAll(pohPrefs)
                addChatMessage("🔮 Runecrafting: Out of essence! Altar AFK turned OFF.")
                return@launch
            }
            deductItemCombined(essence.id, 1)
            val targetRune = _afkRunecraftTargetRuneId.value ?: "item_rune_air"
            saveBankItem(targetRune, (bankItems.value.find { it.id == targetRune }?.quantity ?: 0) + 1)
            addSkillXp(OsrsSkill.RUNECRAFT, 12L)
            progressSkillContract(OsrsSkill.RUNECRAFT, 1, targetRune)
        }
    }

    fun processAfkThievingTick() {
        viewModelScope.launch {
            val npcId = _selectedThievingNpcId.value ?: "man"
            val successChance = 85
            if ((1..100).random() <= successChance) {
                val mult = getThievingGpMultiplier()
                val gp = (15 * mult).toInt().coerceAtLeast(1)
                val currentPet = petState.value
                repository.savePetState(currentPet.copy(coinsGp = currentPet.coinsGp + gp))
                addSkillXp(OsrsSkill.THIEVING, 20L)
                progressSkillContract(OsrsSkill.THIEVING, 1, npcId)
            }
        }
    }

    fun processAfkCatacombsTick() {
        viewModelScope.launch {
            addSkillXp(OsrsSkill.ADVENTURING, 30L)
            progressSkillContract(OsrsSkill.ADVENTURING, 1, "adventuring")
        }
    }

    // ==========================================
    // POF FARM / HUSBANDRY & SEED POUCH
    // ==========================================
    fun openSeedPouch(itemId: String = "item_seed_pouch") {
        viewModelScope.launch {
            val pouch = inventoryItems.value.find { it.id == itemId && it.quantity > 0 }
            if (pouch == null) {
                addChatMessage("🌱 You don't have any Seed Pouches to open!")
                return@launch
            }
            deductItemCombined(itemId, 1)
            val possibleSeeds = listOf(
                "item_seed_potato", "item_seed_onion", "item_seed_cabbage",
                "item_seed_tomato", "item_seed_sweetcorn", "item_seed_strawberry",
                "item_seed_watermelon", "item_seed_guam", "item_seed_tarromin", "item_seed_ranarr"
            )
            val rewardSeed = possibleSeeds.random()
            val qty = (2..5).random()
            saveBankItem(rewardSeed, (bankItems.value.find { it.id == rewardSeed }?.quantity ?: 0) + qty)
            val seedName = com.example.data.models.DefaultItems.getItemById(rewardSeed).name
            addChatMessage("🌱 Opened Seed Pouch: Found +${qty}x $seedName!")
        }
    }

    fun requestFarmingContract(difficulty: Any? = null) {
        assignNewSkillContract(OsrsSkill.FARMING)
    }

    fun claimContractReward(skill: OsrsSkill = OsrsSkill.FARMING) {
        claimSkillContract(skill)
    }

    fun buyHusbandryLivestock(type: String) {
        viewModelScope.launch {
            val cost = 500
            val pet = petState.value
            if (pet.coinsGp < cost) {
                addChatMessage("💰 Cannot buy $type: Costs $cost GP (You have ${pet.coinsGp} GP)!")
                return@launch
            }
            repository.savePetState(pet.copy(coinsGp = pet.coinsGp - cost))
            addChatMessage("🐄 Purchased 1x $type for Player-Owned Farm!")
        }
    }

    fun renameHusbandryLivestock(id: String, name: String) {
        addChatMessage("🏷️ Renamed livestock to '$name'!")
    }

    fun sellOrDismissLivestock(id: String) {
        viewModelScope.launch {
            val pet = petState.value
            repository.savePetState(pet.copy(coinsGp = pet.coinsGp + 250))
            addChatMessage("💰 Sold livestock for +250 GP!")
        }
    }

    fun feedHusbandryTrough(itemId: String, qty: Int) {
        viewModelScope.launch {
            deductItemCombined(itemId, qty)
            addChatMessage("🌾 Added $qty produce to the Farm Trough!")
        }
    }

    fun depositAllAvailableCropsToTrough() {
        viewModelScope.launch {
            val crops = inventoryItems.value.filter { it.category == com.example.data.models.ItemCategory.FARMING && it.quantity > 0 }
            var total = 0
            for (crop in crops) {
                total += crop.quantity
                deductItemCombined(crop.id, crop.quantity)
            }
            if (total > 0) {
                addChatMessage("🌾 Deposited $total crops into the Farm Trough!")
            } else {
                addChatMessage("🌾 No farm produce found in inventory to deposit!")
            }
        }
    }

    fun craftTroughSlosh(id1: String, id2: String) {
        viewModelScope.launch {
            deductItemCombined(id1, 1)
            deductItemCombined(id2, 1)
            saveBankItem("item_trough_slosh", (bankItems.value.find { it.id == "item_trough_slosh" }?.quantity ?: 0) + 1)
            addChatMessage("🥣 Crafted 1x Farm Slosh for livestock!")
        }
    }

    fun withdrawHusbandryChestRewards() {
        viewModelScope.launch {
            addChatMessage("🎁 Collected all rewards from Farm Husbandry Chest!")
        }
    }

    // ==========================================
    // ANVIL & SMITHING / DIVINATION EFFIGIES
    // ==========================================
    fun forgeArmorAtAnvil(armorId: String) {
        forgeEquipmentAtAnvil(armorId)
    }

    fun forgeEquipmentAtAnvil(equipId: String) {
        viewModelScope.launch {
            val item = com.example.data.models.DefaultItems.getItemById(equipId)
            val barId = when {
                equipId.contains("rune") -> "item_rune_bar"
                equipId.contains("adamant") -> "item_adamant_bar"
                equipId.contains("mithril") -> "item_mithril_bar"
                equipId.contains("steel") -> "item_steel_bar"
                equipId.contains("iron") -> "item_iron_bar"
                else -> "item_bronze_bar"
            }
            val reqBars = 3
            val currentBars = getItemQuantityCombined(barId)
            if (currentBars < reqBars) {
                addChatMessage("🔨 Cannot forge ${item.name}: Requires $reqBars metal bars!")
                return@launch
            }
            deductItemCombined(barId, reqBars)
            saveBankItem(equipId, (bankItems.value.find { it.id == equipId }?.quantity ?: 0) + 1)
            addSkillXp(OsrsSkill.SMITHING, 75L)
            addChatMessage("🔨 Forged 1x ${item.name} at the Anvil: +75 Smithing XP!")
        }
    }

    fun transmuteItemToEnergy(itemId: String, qty: Int) {
        viewModelScope.launch {
            val count = qty.coerceAtLeast(1)
            deductItemCombined(itemId, count)
            val energyQty = count * 3
            saveBankItem("item_divination_energy", (bankItems.value.find { it.id == "item_divination_energy" }?.quantity ?: 0) + energyQty)
            val xp = count * 15L
            addSkillXp(OsrsSkill.DIVINATION, xp)
            addChatMessage("✨ Transmuted $count items into +${energyQty}x Divination Energy (+${xp} XP)!")
        }
    }

    fun craftSkillEffigy(skill: OsrsSkill) {
        viewModelScope.launch {
            val effigyId = "item_effigy_${skill.name.lowercase()}"
            saveBankItem(effigyId, (bankItems.value.find { it.id == effigyId }?.quantity ?: 0) + 1)
            addChatMessage("🗿 Crafted 1x ${skill.displayName} Effigy!")
        }
    }

    fun consumeSkillEffigy(effigyId: String) {
        viewModelScope.launch {
            deductItemCombined(effigyId, 1)
            val skill = OsrsSkill.entries.find { effigyId.contains(it.name.lowercase()) } ?: OsrsSkill.ADVENTURING
            val xp = 250L
            addSkillXp(skill, xp)
            addChatMessage("🗿 Investigated ${skill.displayName} Effigy: Gained +${xp} ${skill.displayName} XP!")
        }
    }
"""

with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    text = f.read()

# remove last closing brace
last_brace = text.rfind('}')
if last_brace != -1:
    new_text = text[:last_brace] + missing_code + "\n}\n"
    with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'w', encoding='utf-8') as f:
        f.write(new_text)
    print("Appended missing functions successfully!")
