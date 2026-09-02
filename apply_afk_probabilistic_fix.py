with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update chopTrees
old_chop = """        val choppableTrees = currentGrove.choppableTrees
        val effectiveTargetTreeId = targetTreeId ?: _selectedTreeId.value
        val selectedTree: com.example.data.models.GroveTree? = if (effectiveTargetTreeId != null) {
            val target = choppableTrees.find { it.id == effectiveTargetTreeId }
            if (target != null) {
                if (wcLvl < target.reqLevel) {
                    addChatMessage("🔒 You need Level ${target.reqLevel} Woodcutting to chop ${target.name} (You are Level $wcLvl)!")
                    if (isAfk) AfkEngine.stopAll(pohPrefs)
                    return
                }
                target
            } else {
                val unlockedTrees = choppableTrees.filter { wcLvl >= it.reqLevel }
                if (unlockedTrees.isEmpty()) {
                    if (isAfk) AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🔒 No unlocked trees available in ${currentGrove.name} for your Woodcutting level ($wcLvl)!")
                    return
                }
                val totalWeight = unlockedTrees.sumOf { it.dropChancePercent }
                val roll = (1..totalWeight.coerceAtLeast(1)).random()
                var cumulative = 0
                var chosen = unlockedTrees.first()
                for (tree in unlockedTrees) {
                    cumulative += tree.dropChancePercent
                    if (roll <= cumulative) {
                        chosen = tree
                        break
                    }
                }
                chosen
            }
        } else {
            val unlockedTrees = choppableTrees.filter { wcLvl >= it.reqLevel }
            if (unlockedTrees.isEmpty()) {
                if (isAfk) AfkEngine.stopAll(pohPrefs)
                addChatMessage("🔒 No unlocked trees available in ${currentGrove.name} for your Woodcutting level ($wcLvl)!")
                return
            }
            val totalWeight = unlockedTrees.sumOf { it.dropChancePercent }
            val roll = (1..totalWeight.coerceAtLeast(1)).random()
            var cumulative = 0
            var chosen = unlockedTrees.first()
            for (tree in unlockedTrees) {
                cumulative += tree.dropChancePercent
                if (roll <= cumulative) {
                    chosen = tree
                    break
                }
            }
            chosen
        }"""

new_chop = """        val choppableTrees = currentGrove.choppableTrees
        val unlockedTrees = choppableTrees.filter { wcLvl >= it.reqLevel }
        if (unlockedTrees.isEmpty()) {
            if (isAfk) AfkEngine.stopAll(pohPrefs)
            addChatMessage("🔒 No unlocked trees available in ${currentGrove.name} for your Woodcutting level ($wcLvl)!")
            return
        }

        val selectedTree: com.example.data.models.GroveTree? = if (!isAfk && targetTreeId != null) {
            val target = choppableTrees.find { it.id == targetTreeId }
            if (target != null && wcLvl < target.reqLevel) {
                addChatMessage("🔒 You need Level ${target.reqLevel} Woodcutting to chop ${target.name} (You are Level $wcLvl)!")
                return
            }
            target ?: unlockedTrees.first()
        } else {
            val totalWeight = unlockedTrees.sumOf { it.dropChancePercent.coerceAtLeast(1) }
            val roll = (1..totalWeight.coerceAtLeast(1)).random()
            var cumulative = 0
            var chosen = unlockedTrees.first()
            for (tree in unlockedTrees) {
                cumulative += tree.dropChancePercent.coerceAtLeast(1)
                if (roll <= cumulative) {
                    chosen = tree
                    break
                }
            }
            chosen
        }"""

assert old_chop in content, "old_chop not found"
content = content.replace(old_chop, new_chop, 1)

# 2. Update fishAtPohPond
old_fish = """            val fishToCatch: com.example.data.models.SpiritFish? = if (targetFishId != null) {
                val target = currentArea?.catchableFish?.find { it.id == targetFishId }
                    ?: com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS
                        .flatMap { it.catchableFish }
                        .find { it.id == targetFishId }
                if (target != null && fishingLvl < target.reqLevel) {
                    addChatMessage("🔒 Requires Level ${target.reqLevel} Fishing for ${target.name} (You are Level $fishingLvl)!")
                    if (isAfk) AfkEngine.stopAll(pohPrefs)
                    return@launch
                }
                target
            } else {
                val poolFish = currentArea?.catchableFish ?: com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS.first().catchableFish
                val unlockedFish = poolFish.filter { fishingLvl >= it.reqLevel }
                if (unlockedFish.isEmpty()) {
                    if (isAfk) AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🔒 No unlocked fish available in ${currentArea?.name ?: "this pond"} for your Fishing level ($fishingLvl)!")
                    return@launch
                }
                val totalWeight = unlockedFish.sumOf { it.dropChancePercent }
                val roll = (1..totalWeight.coerceAtLeast(1)).random()
                var cumulative = 0
                var chosen = unlockedFish.first()
                for (f in unlockedFish) {
                    cumulative += f.dropChancePercent
                    if (roll <= cumulative) {
                        chosen = f
                        break
                    }
                }
                chosen
            }"""

new_fish = """            val poolFish = currentArea?.catchableFish ?: com.example.data.models.AdventuringStoryData.SPIRIT_POOL_AREAS.first().catchableFish
            val unlockedFish = poolFish.filter { fishingLvl >= it.reqLevel }
            if (unlockedFish.isEmpty()) {
                if (isAfk) AfkEngine.stopAll(pohPrefs)
                addChatMessage("🔒 No unlocked fish available in ${currentArea?.name ?: "this pond"} for your Fishing level ($fishingLvl)!")
                return@launch
            }

            val fishToCatch: com.example.data.models.SpiritFish? = if (!isAfk && targetFishId != null) {
                val target = poolFish.find { it.id == targetFishId }
                if (target != null && fishingLvl < target.reqLevel) {
                    addChatMessage("🔒 Requires Level ${target.reqLevel} Fishing for ${target.name} (You are Level $fishingLvl)!")
                    return@launch
                }
                target ?: unlockedFish.first()
            } else {
                val totalWeight = unlockedFish.sumOf { it.dropChancePercent.coerceAtLeast(1) }
                val roll = (1..totalWeight.coerceAtLeast(1)).random()
                var cumulative = 0
                var chosen = unlockedFish.first()
                for (f in unlockedFish) {
                    cumulative += f.dropChancePercent.coerceAtLeast(1)
                    if (roll <= cumulative) {
                        chosen = f
                        break
                    }
                }
                chosen
            }"""

assert old_fish in content, "old_fish not found"
content = content.replace(old_fish, new_fish, 1)

# 3. Update mineAtPohQuarry
old_mine = """            val choppableOres = currentArea.minerals
            val isRuneVault = currentArea.id == "quarry_rune_essence_vault"

            val selectedMineral: com.example.data.models.GemologyMineral? = if (isRuneVault) {
                if (targetOreId != null && targetOreId != "item_rune_essence") {
                    val tapped = choppableOres.find { it.id == targetOreId }
                    if (tapped != null && smithLvl < tapped.reqLevel) {
                        addChatMessage("🔒 Requires Level ${tapped.reqLevel} Forging for ${tapped.name} (You are Level $smithLvl)!")
                        if (isAfk) AfkEngine.stopAll(pohPrefs)
                        return@launch
                    }
                    tapped ?: choppableOres.first()
                } else {
                    val unlockedGems = choppableOres.filter { it.isGem && smithLvl >= it.reqLevel }
                    val roll = (1..100).random()
                    if (roll <= 99 || unlockedGems.isEmpty()) {
                        choppableOres.first { it.id == "item_rune_essence" }
                    } else {
                        unlockedGems.random()
                    }
                }
            } else if (targetOreId != null) {
                val target = choppableOres.find { it.id == targetOreId }
                    ?: com.example.data.models.AdventuringStoryData.GEMOLOGY_AREAS.flatMap { it.minerals }.find { it.id == targetOreId }
                if (target != null && smithLvl < target.reqLevel) {
                    addChatMessage("🔒 Requires Level ${target.reqLevel} Forging for ${target.name} (You are Level $smithLvl)!")
                    if (isAfk) AfkEngine.stopAll(pohPrefs)
                    return@launch
                }
                target
            } else {
                val unlockedMinerals = choppableOres.filter { smithLvl >= it.reqLevel }
                if (unlockedMinerals.isEmpty()) {
                    if (isAfk) AfkEngine.stopAll(pohPrefs)
                    addChatMessage("🔒 No unlocked minerals available in ${currentArea.name} for your Forging level ($smithLvl)!")
                    return@launch
                }
                val totalWeight = unlockedMinerals.sumOf { it.dropChancePercent }
                val roll = (1..totalWeight.coerceAtLeast(1)).random()
                var cumulative = 0
                var chosen = unlockedMinerals.first()
                for (m in unlockedMinerals) {
                    cumulative += m.dropChancePercent
                    if (roll <= cumulative) {
                        chosen = m
                        break
                    }
                }
                chosen
            }"""

new_mine = """            val choppableOres = currentArea.minerals
            val unlockedMinerals = choppableOres.filter { smithLvl >= it.reqLevel }
            if (unlockedMinerals.isEmpty()) {
                if (isAfk) AfkEngine.stopAll(pohPrefs)
                addChatMessage("🔒 No unlocked minerals available in ${currentArea.name} for your Forging level ($smithLvl)!")
                return@launch
            }

            val selectedMineral: com.example.data.models.GemologyMineral? = if (!isAfk && targetOreId != null) {
                val target = choppableOres.find { it.id == targetOreId }
                if (target != null && smithLvl < target.reqLevel) {
                    addChatMessage("🔒 Requires Level ${target.reqLevel} Forging for ${target.name} (You are Level $smithLvl)!")
                    return@launch
                }
                target ?: unlockedMinerals.first()
            } else {
                val totalWeight = unlockedMinerals.sumOf { it.dropChancePercent.coerceAtLeast(1) }
                val roll = (1..totalWeight.coerceAtLeast(1)).random()
                var cumulative = 0
                var chosen = unlockedMinerals.first()
                for (m in unlockedMinerals) {
                    cumulative += m.dropChancePercent.coerceAtLeast(1)
                    if (roll <= cumulative) {
                        chosen = m
                        break
                    }
                }
                chosen
            }"""

assert old_mine in content, "old_mine not found"
content = content.replace(old_mine, new_mine, 1)

# 4. Update processOfflineAfkProgress for Woodcutting, Fishing, Mining
old_offline_wc = """                            val targetTreeId = _selectedTreeId.value
                            val eligibleTrees = area.choppableTrees.filter { wcLvl >= it.reqLevel }
                            val tree = if (targetTreeId != null) {
                                area.choppableTrees.find { it.id == targetTreeId && wcLvl >= it.reqLevel }
                                    ?: eligibleTrees.firstOrNull()
                            } else {
                                val totalWeight = eligibleTrees.sumOf { it.dropChancePercent }
                                val roll = (1..totalWeight.coerceAtLeast(1)).random()
                                var cumulative = 0
                                var chosen = eligibleTrees.firstOrNull()
                                for (t in eligibleTrees) {
                                    cumulative += t.dropChancePercent
                                    if (roll <= cumulative) {
                                        chosen = t
                                        break
                                    }
                                }
                                chosen
                            }"""

new_offline_wc = """                            val eligibleTrees = area.choppableTrees.filter { wcLvl >= it.reqLevel }
                            if (eligibleTrees.isEmpty()) {
                                stoppedReason = "No unlocked trees available to chop in ${area.name}!"
                                break
                            }
                            val totalWeight = eligibleTrees.sumOf { it.dropChancePercent.coerceAtLeast(1) }
                            val roll = (1..totalWeight.coerceAtLeast(1)).random()
                            var cumulative = 0
                            var tree: com.example.data.models.GroveTree = eligibleTrees.first()
                            for (t in eligibleTrees) {
                                cumulative += t.dropChancePercent.coerceAtLeast(1)
                                if (roll <= cumulative) {
                                    tree = t
                                    break
                                }
                            }"""

assert old_offline_wc in content, "old_offline_wc not found"
content = content.replace(old_offline_wc, new_offline_wc, 1)

old_offline_fish = """                            val targetFishId = _selectedFishId.value
                            val eligibleFish = area.catchableFish.filter { fLvl >= it.reqLevel }
                            val fish = if (targetFishId != null) {
                                area.catchableFish.find { it.id == targetFishId && fLvl >= it.reqLevel }
                                    ?: eligibleFish.firstOrNull()
                            } else {
                                eligibleFish.firstOrNull()
                            }"""

new_offline_fish = """                            val eligibleFish = area.catchableFish.filter { fLvl >= it.reqLevel }
                            if (eligibleFish.isEmpty()) {
                                stoppedReason = "No unlocked fish available to catch in ${area.name}!"
                                break
                            }
                            val totalWeight = eligibleFish.sumOf { it.dropChancePercent.coerceAtLeast(1) }
                            val roll = (1..totalWeight.coerceAtLeast(1)).random()
                            var cumulative = 0
                            var fish: com.example.data.models.SpiritFish = eligibleFish.first()
                            for (f in eligibleFish) {
                                cumulative += f.dropChancePercent.coerceAtLeast(1)
                                if (roll <= cumulative) {
                                    fish = f
                                    break
                                }
                            }"""

assert old_offline_fish in content, "old_offline_fish not found"
content = content.replace(old_offline_fish, new_offline_fish, 1)

old_offline_mining = """                            val targetOreId = _selectedOreId.value
                            val eligibleMinerals = area.minerals.filter { mLvl >= it.reqLevel }
                            val mineral = if (targetOreId != null) {
                                area.minerals.find { it.id == targetOreId && mLvl >= it.reqLevel }
                                    ?: eligibleMinerals.firstOrNull()
                            } else {
                                eligibleMinerals.firstOrNull()
                            }"""

new_offline_mining = """                            val eligibleMinerals = area.minerals.filter { mLvl >= it.reqLevel }
                            if (eligibleMinerals.isEmpty()) {
                                stoppedReason = "No unlocked minerals available to mine in ${area.name}!"
                                break
                            }
                            val totalWeight = eligibleMinerals.sumOf { it.dropChancePercent.coerceAtLeast(1) }
                            val roll = (1..totalWeight.coerceAtLeast(1)).random()
                            var cumulative = 0
                            var mineral: com.example.data.models.GemologyMineral = eligibleMinerals.first()
                            for (m in eligibleMinerals) {
                                cumulative += m.dropChancePercent.coerceAtLeast(1)
                                if (roll <= cumulative) {
                                    mineral = m
                                    break
                                }
                            }"""

assert old_offline_mining in content, "old_offline_mining not found"
content = content.replace(old_offline_mining, new_offline_mining, 1)

with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)

print("Successfully updated PetViewModel.kt with probabilistic gathering across all areas!")
