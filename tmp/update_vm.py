import os

vm_path = "app/src/main/java/com/example/viewmodel/PetViewModel.kt"
with open(vm_path, "r", encoding="utf-8") as f:
    text = f.read()

# 1. Update loadPofState and savePofState
old_pof_block = """    // POF Companion Farming Guild State Persistence
    private fun loadPofState(petTypeName: String = PetType.BABY_BLACK_DRAGON.name): com.example.data.models.PlayerOwnedFarmState {
        val key = "pof_state_json_$petTypeName"
        val jsonStr = pofPrefs.getString(key, null)
            ?: (if (petTypeName == PetType.BABY_BLACK_DRAGON.name) pofPrefs.getString("pof_state_json", null) else null)
            ?: return com.example.data.models.PlayerOwnedFarmState()
        return try {
            val root = JSONObject(jsonStr)
            val scarecrow = root.optBoolean("scarecrowBuilt", false)
            val compostLevel = root.optInt("compostBinLevel", 1)
            val compostBuckets = root.optInt("compostBucketsCount", 10)
            val totalHarvested = root.optInt("totalCropsHarvested", 0)
            val totalContracts = root.optInt("totalContractsCompleted", 0)

            val plotsArray = root.optJSONArray("plots") ?: JSONArray()
            val plotsList = mutableListOf<com.example.data.models.FarmPlotState>()
            for (i in 0 until plotsArray.length()) {
                val pObj = plotsArray.getJSONObject(i)
                val pIdx = pObj.optInt("plotIndex", i)
                val cropName = pObj.optString("cropType", "")
                val cropType = if (cropName.isNotEmpty()) {
                    try { com.example.data.models.FarmCropType.valueOf(cropName) } catch (e: Exception) { null }
                } else null
                val plantedMs = pObj.optLong("plantedTimestampMs", 0L)
                val isWatered = pObj.optBoolean("isWatered", false)
                val isComposted = pObj.optBoolean("isComposted", false)
                plotsList.add(
                    com.example.data.models.FarmPlotState(
                        plotIndex = pIdx,
                        cropType = cropType,
                        plantedTimestampMs = plantedMs,
                        isWatered = isWatered,
                        isComposted = isComposted
                    )
                )
            }

            if (plotsList.isEmpty()) {
                plotsList.addAll(com.example.data.models.PlayerOwnedFarmState().plots)
            } else {
                while (plotsList.size < 12) {
                    plotsList.add(com.example.data.models.FarmPlotState(plotIndex = plotsList.size))
                }
            }

            com.example.data.models.PlayerOwnedFarmState(
                plots = plotsList.take(12),
                scarecrowBuilt = scarecrow,
                compostBinLevel = compostLevel,
                compostBucketsCount = compostBuckets,
                totalCropsHarvested = totalHarvested,
                totalContractsCompleted = totalContracts
            )
        } catch (e: Exception) {
            com.example.data.models.PlayerOwnedFarmState()
        }
    }

    private fun savePofState(petTypeName: String, state: com.example.data.models.PlayerOwnedFarmState) {
        try {
            val root = JSONObject()
            root.put("scarecrowBuilt", state.scarecrowBuilt)
            root.put("compostBinLevel", state.compostBinLevel)
            root.put("compostBucketsCount", state.compostBucketsCount)
            root.put("totalCropsHarvested", state.totalCropsHarvested)
            root.put("totalContractsCompleted", state.totalContractsCompleted)

            val plotsArray = JSONArray()
            state.plots.forEach { p ->
                val pObj = JSONObject()
                pObj.put("plotIndex", p.plotIndex)
                pObj.put("cropType", p.cropType?.name ?: "")
                pObj.put("plantedTimestampMs", p.plantedTimestampMs)
                pObj.put("isWatered", p.isWatered)
                pObj.put("isComposted", p.isComposted)
                plotsArray.put(pObj)
            }
            root.put("plots", plotsArray)

            val key = "pof_state_json_$petTypeName"
            pofPrefs.edit().putString(key, root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""

new_pof_block = """    // POF Companion Farming & Animal Husbandry State Persistence
    private fun loadPofState(petTypeName: String = PetType.BABY_BLACK_DRAGON.name): com.example.data.models.PlayerOwnedFarmState {
        val key = "pof_state_json_$petTypeName"
        val jsonStr = pofPrefs.getString(key, null)
            ?: (if (petTypeName == PetType.BABY_BLACK_DRAGON.name) pofPrefs.getString("pof_state_json", null) else null)
            ?: return com.example.data.models.PlayerOwnedFarmState()
        return try {
            val root = JSONObject(jsonStr)
            val scarecrow = root.optBoolean("scarecrowBuilt", false)
            val compostLevel = root.optInt("compostBinLevel", 1)
            val compostBuckets = root.optInt("compostBucketsCount", 10)
            val totalHarvested = root.optInt("totalCropsHarvested", 0)
            val totalContracts = root.optInt("totalContractsCompleted", 0)

            val plotsArray = root.optJSONArray("plots") ?: JSONArray()
            val plotsList = mutableListOf<com.example.data.models.FarmPlotState>()
            for (i in 0 until plotsArray.length()) {
                val pObj = plotsArray.getJSONObject(i)
                val pIdx = pObj.optInt("plotIndex", i)
                val cropName = pObj.optString("cropType", "")
                val cropType = if (cropName.isNotEmpty()) {
                    try { com.example.data.models.FarmCropType.valueOf(cropName) } catch (e: Exception) { null }
                } else null
                val plantedMs = pObj.optLong("plantedTimestampMs", 0L)
                val isWatered = pObj.optBoolean("isWatered", false)
                val isComposted = pObj.optBoolean("isComposted", false)
                plotsList.add(
                    com.example.data.models.FarmPlotState(
                        plotIndex = pIdx,
                        cropType = cropType,
                        plantedTimestampMs = plantedMs,
                        isWatered = isWatered,
                        isComposted = isComposted
                    )
                )
            }

            if (plotsList.isEmpty()) {
                plotsList.addAll(com.example.data.models.PlayerOwnedFarmState().plots)
            } else {
                while (plotsList.size < 12) {
                    plotsList.add(com.example.data.models.FarmPlotState(plotIndex = plotsList.size))
                }
            }

            // Parse Animal Husbandry State
            val husbandryObj = root.optJSONObject("husbandryState")
            val troughFood = husbandryObj?.optInt("troughFoodPct", 100) ?: 100
            val storedCompost = husbandryObj?.optInt("storedCompost", 0) ?: 0
            val storedProduceMap = mutableMapOf<String, Int>()
            val produceObj = husbandryObj?.optJSONObject("storedProduce")
            if (produceObj != null) {
                produceObj.keys().forEach { k ->
                    storedProduceMap[k] = produceObj.optInt(k, 0)
                }
            }
            val animalsList = mutableListOf<com.example.data.models.FarmAnimalInstance>()
            val animalsArray = husbandryObj?.optJSONArray("animals")
            if (animalsArray != null) {
                for (i in 0 until animalsArray.length()) {
                    val aObj = animalsArray.getJSONObject(i)
                    val instId = aObj.optString("instanceId", "animal_$i")
                    val tId = aObj.optString("typeId", "livestock_chicken")
                    val cName = aObj.optString("customName", "Animal")
                    val pTime = aObj.optLong("purchasedTimestampMs", System.currentTimeMillis())
                    val lTime = aObj.optLong("lastProducedTimestampMs", System.currentTimeMillis())
                    animalsList.add(
                        com.example.data.models.FarmAnimalInstance(
                            instanceId = instId,
                            typeId = tId,
                            customName = cName,
                            purchasedTimestampMs = pTime,
                            lastProducedTimestampMs = lTime
                        )
                    )
                }
            }

            com.example.data.models.PlayerOwnedFarmState(
                plots = plotsList.take(12),
                scarecrowBuilt = scarecrow,
                compostBinLevel = compostLevel,
                compostBucketsCount = compostBuckets,
                totalCropsHarvested = totalHarvested,
                totalContractsCompleted = totalContracts,
                husbandryState = com.example.data.models.AnimalHusbandryState(
                    animals = animalsList,
                    troughFoodPct = troughFood,
                    storedCompost = storedCompost,
                    storedProduce = storedProduceMap
                )
            )
        } catch (e: Exception) {
            com.example.data.models.PlayerOwnedFarmState()
        }
    }

    private fun savePofState(petTypeName: String, state: com.example.data.models.PlayerOwnedFarmState) {
        try {
            val root = JSONObject()
            root.put("scarecrowBuilt", state.scarecrowBuilt)
            root.put("compostBinLevel", state.compostBinLevel)
            root.put("compostBucketsCount", state.compostBucketsCount)
            root.put("totalCropsHarvested", state.totalCropsHarvested)
            root.put("totalContractsCompleted", state.totalContractsCompleted)

            val plotsArray = JSONArray()
            state.plots.forEach { p ->
                val pObj = JSONObject()
                pObj.put("plotIndex", p.plotIndex)
                pObj.put("cropType", p.cropType?.name ?: "")
                pObj.put("plantedTimestampMs", p.plantedTimestampMs)
                pObj.put("isWatered", p.isWatered)
                pObj.put("isComposted", p.isComposted)
                plotsArray.put(pObj)
            }
            root.put("plots", plotsArray)

            // Save Animal Husbandry State
            val husbandryObj = JSONObject()
            husbandryObj.put("troughFoodPct", state.husbandryState.troughFoodPct)
            husbandryObj.put("storedCompost", state.husbandryState.storedCompost)
            val produceJson = JSONObject()
            state.husbandryState.storedProduce.forEach { (k, v) -> produceJson.put(k, v) }
            husbandryObj.put("storedProduce", produceJson)
            val animalsArray = JSONArray()
            state.husbandryState.animals.forEach { a ->
                val aObj = JSONObject()
                aObj.put("instanceId", a.instanceId)
                aObj.put("typeId", a.typeId)
                aObj.put("customName", a.customName)
                aObj.put("purchasedTimestampMs", a.purchasedTimestampMs)
                aObj.put("lastProducedTimestampMs", a.lastProducedTimestampMs)
                animalsArray.put(aObj)
            }
            husbandryObj.put("animals", animalsArray)
            root.put("husbandryState", husbandryObj)

            val key = "pof_state_json_$petTypeName"
            pofPrefs.edit().putString(key, root.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }"""

if old_pof_block in text:
    text = text.replace(old_pof_block, new_pof_block)

# 2. Call processHusbandryTick() in ticker
old_ticker_call = """                _tickerTrigger.value = System.currentTimeMillis()
                saveAfkStateToPrefs()"""

new_ticker_call = """                _tickerTrigger.value = System.currentTimeMillis()
                saveAfkStateToPrefs()
                processHusbandryTick()"""

if old_ticker_call in text:
    text = text.replace(old_ticker_call, new_ticker_call)

# 3. Add Animal Husbandry methods after openSeedPouch
old_after_seed = """            val summaryText = resultSummary.joinToString(", ")
            addChatMessage("🎁 Opened $pouchName! Extracted: $summaryText!")
        }
    }"""

husbandry_methods = """            val summaryText = resultSummary.joinToString(", ")
            addChatMessage("🎁 Opened $pouchName! Extracted: $summaryText!")
        }
    }

    // ==========================================
    // ANIMAL HUSBANDRY LIVESTOCK & PEN SYSTEM
    // ==========================================

    fun buyHusbandryLivestock(type: com.example.data.models.LivestockType) {
        viewModelScope.launch {
            val farmingXp = skillXpMap.value[OsrsSkill.FARMING] ?: 0L
            val farmingLvl = OsrsXpCalculator.getLevelForXp(farmingXp)
            val hunterXp = skillXpMap.value[OsrsSkill.HUNTER] ?: 0L
            val hunterLvl = OsrsXpCalculator.getLevelForXp(hunterXp)
            val conXp = skillXpMap.value[OsrsSkill.CONSTRUCTION] ?: 0L
            val conLvl = OsrsXpCalculator.getLevelForXp(conXp)

            if (farmingLvl < 65 || hunterLvl < 40 || conLvl < 50) {
                addChatMessage("🔒 Animal Husbandry requires Level 65 Agriculture, Level 40 Beast Taming, and Level 50 Hut-Keeping!")
                return@launch
            }

            if (farmingLvl < type.reqFarmingLevel) {
                addChatMessage("🔒 You need Level ${type.reqFarmingLevel} Agriculture to purchase a ${type.speciesName}!")
                return@launch
            }

            val maxCap = com.example.data.models.calculateMaxLivestockCapacity(farmingLvl)
            val currentAnimals = _pofState.value.husbandryState.animals
            if (currentAnimals.size >= maxCap) {
                addChatMessage("⚠️ Pen is at maximum capacity ($maxCap animals)! Increase your Agriculture level to expand your pen space.")
                return@launch
            }

            val currentCoins = petState.value.coinsGp
            if (currentCoins < type.buyCostGp) {
                addChatMessage("⚠️ Not enough GP! A ${type.speciesName} costs ${type.buyCostGp} GP (You have: $currentCoins GP).")
                return@launch
            }

            // Deduct Coins
            repository.updatePetCoins(petState.value.petType.name, currentCoins - type.buyCostGp)

            val newAnimal = com.example.data.models.FarmAnimalInstance(
                instanceId = java.util.UUID.randomUUID().toString(),
                typeId = type.id,
                customName = "${type.defaultName} #${currentAnimals.size + 1}",
                purchasedTimestampMs = System.currentTimeMillis(),
                lastProducedTimestampMs = System.currentTimeMillis()
            )

            val updatedAnimals = currentAnimals + newAnimal
            val updatedHusbandry = _pofState.value.husbandryState.copy(animals = updatedAnimals)
            updatePofState(_pofState.value.copy(husbandryState = updatedHusbandry))

            addChatMessage("🏡 Animal Husbandry: Purchased a ${type.speciesName} ${type.emoji} for ${type.buyCostGp} GP! Named: '${newAnimal.customName}'. [Pen: ${updatedAnimals.size}/$maxCap]")
        }
    }

    fun renameHusbandryLivestock(instanceId: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        val currentAnimals = _pofState.value.husbandryState.animals
        val updatedList = currentAnimals.map {
            if (it.instanceId == instanceId) it.copy(customName = trimmed.take(24)) else it
        }
        val updatedHusbandry = _pofState.value.husbandryState.copy(animals = updatedList)
        updatePofState(_pofState.value.copy(husbandryState = updatedHusbandry))
        addChatMessage("✏️ Renamed farm animal to '$trimmed'!")
    }

    fun sellOrDismissLivestock(instanceId: String) {
        viewModelScope.launch {
            val currentAnimals = _pofState.value.husbandryState.animals
            val target = currentAnimals.find { it.instanceId == instanceId } ?: return@launch
            val refundGp = (target.type.buyCostGp * 0.4).toLong()
            val updatedList = currentAnimals.filter { it.instanceId != instanceId }
            val updatedHusbandry = _pofState.value.husbandryState.copy(animals = updatedList)
            updatePofState(_pofState.value.copy(husbandryState = updatedHusbandry))
            repository.updatePetCoins(petState.value.petType.name, petState.value.coinsGp + refundGp)
            addChatMessage("🏡 Sold ${target.customName} (${target.type.speciesName}) for $refundGp GP back to the livestock trader.")
        }
    }

    fun feedHusbandryTrough(itemId: String, qty: Int = 1) {
        viewModelScope.launch {
            if (qty <= 0) return@launch
            val available = getItemQuantityCombined(itemId)
            if (available < qty) {
                val itemDef = DefaultItems.getItemById(itemId)
                addChatMessage("⚠️ You do not have $qty x ${itemDef.name} to fill the trough!")
                return@launch
            }

            val curState = _pofState.value.husbandryState
            if (curState.troughFoodPct >= com.example.data.models.AnimalHusbandryState.MAX_TROUGH_CAPACITY) {
                addChatMessage("🌾 The livestock trough is already completely full (1000% capacity)!")
                return@launch
            }

            val gainPerItem = when {
                itemId == "item_trough_slosh" -> 150
                itemId.contains("slosh") -> 150
                itemId.contains("corn") || itemId.contains("cabbage") || itemId.contains("potato") -> 40
                itemId.contains("carrot") || itemId.contains("tomato") || itemId.contains("strawberry") -> 35
                itemId.contains("watermelon") || itemId.contains("pumpkin") -> 60
                itemId.contains("grain") || itemId.contains("bread") || itemId.contains("trout") -> 30
                else -> 25
            }

            val neededCap = com.example.data.models.AnimalHusbandryState.MAX_TROUGH_CAPACITY - curState.troughFoodPct
            val actualItemsToUse = minOf(qty, (neededCap + gainPerItem - 1) / gainPerItem).coerceAtLeast(1)

            deductItemCombined(itemId, actualItemsToUse)
            val addedHunger = actualItemsToUse * gainPerItem
            val newTrough = (curState.troughFoodPct + addedHunger).coerceAtMost(com.example.data.models.AnimalHusbandryState.MAX_TROUGH_CAPACITY)

            val updatedHusbandry = curState.copy(troughFoodPct = newTrough)
            updatePofState(_pofState.value.copy(husbandryState = updatedHusbandry))

            val itemDef = DefaultItems.getItemById(itemId)
            addChatMessage("🌾 Deposited $actualItemsToUse x ${itemDef.name} ${itemDef.iconEmoji} into the livestock trough! (Trough: $newTrough% / 1000%)")
        }
    }

    fun isTroughSloshRecipeUnlocked(): Boolean {
        // Quest #3 is quest_goblin_diplomacy ("Wildland Chieftain Reconciliation")
        return completedQuestIds.value.contains("quest_goblin_diplomacy")
    }

    fun craftTroughSlosh(itemId1: String, itemId2: String) {
        viewModelScope.launch {
            if (!isTroughSloshRecipeUnlocked()) {
                addChatMessage("🔒 Trough Slosh recipe is locked! Complete Quest #3 'Wildland Chieftain Reconciliation' to unlock this recipe.")
                return@launch
            }

            val qty1 = getItemQuantityCombined(itemId1)
            val qty2 = getItemQuantityCombined(itemId2)

            if (itemId1 == itemId2) {
                if (qty1 < 2) {
                    val def = DefaultItems.getItemById(itemId1)
                    addChatMessage("⚠️ You need at least 2x ${def.name} to brew Trough Slosh!")
                    return@launch
                }
                deductItemCombined(itemId1, 2)
            } else {
                if (qty1 < 1 || qty2 < 1) {
                    val def1 = DefaultItems.getItemById(itemId1)
                    val def2 = DefaultItems.getItemById(itemId2)
                    addChatMessage("⚠️ You need 1x ${def1.name} and 1x ${def2.name} to brew Trough Slosh!")
                    return@launch
                }
                deductItemCombined(itemId1, 1)
                deductItemCombined(itemId2, 1)
            }

            val existing = inventoryItems.value.find { it.id == "item_trough_slosh" }?.quantity ?: 0
            saveInventoryItem("item_trough_slosh", existing + 1)

            val def1 = DefaultItems.getItemById(itemId1)
            val def2 = DefaultItems.getItemById(itemId2)
            addChatMessage("🍲 Mashed ${def1.name} + ${def2.name} together into 1x Trough Slosh 🍲! (+150% Trough Feed for livestock)")
        }
    }

    fun withdrawHusbandryChestRewards() {
        viewModelScope.launch {
            val curHusbandry = _pofState.value.husbandryState
            val produce = curHusbandry.storedProduce
            val compost = curHusbandry.storedCompost

            val totalItems = produce.values.sum() + compost
            if (totalItems <= 0) {
                addChatMessage("📦 Barn Collection Chest is empty! Keep the trough supplied with feed and animals will steadily produce materials.")
                return@launch
            }

            val summaryList = mutableListOf<String>()
            produce.forEach { (itemId, qty) ->
                if (qty > 0) {
                    val curInv = inventoryItems.value.find { it.id == itemId }?.quantity ?: 0
                    saveInventoryItem(itemId, curInv + qty)
                    val itemDef = DefaultItems.getItemById(itemId)
                    summaryList.add("$qty x ${itemDef.iconEmoji} ${itemDef.name}")
                }
            }

            val newCompostBuckets = _pofState.value.compostBucketsCount + compost
            if (compost > 0) {
                summaryList.add("$compost x 💩 Compost Buckets")
            }

            val updatedHusbandry = curHusbandry.copy(
                storedProduce = emptyMap(),
                storedCompost = 0
            )
            updatePofState(_pofState.value.copy(
                compostBucketsCount = newCompostBuckets,
                husbandryState = updatedHusbandry
            ))

            addChatMessage("📦 Collected from Barn Chest: ${summaryList.joinToString(", ")}!")
        }
    }

    fun processHusbandryTick() {
        val curPof = _pofState.value
        val curHusbandry = curPof.husbandryState
        val animals = curHusbandry.animals
        if (animals.isEmpty() || curHusbandry.troughFoodPct <= 0) return

        val now = System.currentTimeMillis()
        var foodAvailable = curHusbandry.troughFoodPct
        var compostProduced = 0
        val produceMap = curHusbandry.storedProduce.toMutableMap()
        var anyProduced = false

        val updatedAnimals = animals.map { animal ->
            val cycleSec = animal.type.produceCycleSeconds
            val cycleMs = cycleSec * 1000L
            val elapsed = now - animal.lastProducedTimestampMs
            if (elapsed >= cycleMs && foodAvailable > 0) {
                val cycles = (elapsed / cycleMs).toInt()
                val actualCycles = minOf(cycles, foodAvailable)
                if (actualCycles > 0) {
                    foodAvailable -= actualCycles
                    compostProduced += actualCycles
                    val prodId = animal.type.produceItemId
                    produceMap[prodId] = (produceMap[prodId] ?: 0) + actualCycles
                    anyProduced = true
                    val remainderMs = elapsed % cycleMs
                    animal.copy(lastProducedTimestampMs = now - remainderMs)
                } else animal
            } else animal
        }

        if (anyProduced) {
            val newHusbandry = curHusbandry.copy(
                animals = updatedAnimals,
                troughFoodPct = foodAvailable,
                storedCompost = curHusbandry.storedCompost + compostProduced,
                storedProduce = produceMap
            )
            updatePofState(curPof.copy(husbandryState = newHusbandry))
        }
    }"""

if old_after_seed in text:
    text = text.replace(old_after_seed, husbandry_methods)

# 4. Check feedPet and feedPetFromFoodBag for trough slosh
old_feed_pet = """    fun feedPet(item: InventoryItem) {
        val now = System.currentTimeMillis()
        if (now - lastFeedTimestamp < 350L) return
        lastFeedTimestamp = now

        viewModelScope.launch {"""

new_feed_pet = """    fun feedPet(item: InventoryItem) {
        val now = System.currentTimeMillis()
        if (now - lastFeedTimestamp < 350L) return
        lastFeedTimestamp = now

        viewModelScope.launch {
            if (item.id == "item_trough_slosh" || item.name.contains("Trough Slosh", ignoreCase = true)) {
                addChatMessage("⚠️ Trough Slosh is livestock feed! It can only be fed to animals in the Animal Husbandry trough, not your companion.")
                return@launch
            }"""

if old_feed_pet in text:
    text = text.replace(old_feed_pet, new_feed_pet)

with open(vm_path, "w", encoding="utf-8") as f:
    f.write(text)

print("Applied changes to PetViewModel.kt successfully.")
