with open("app/src/main/java/com/example/viewmodel/PetViewModel.kt", "r", encoding="utf-8") as f:
    text = f.read()

target = """            // 20% slower gathering: 80% catch chance per fishing attempt
            val catchSuccessRoll = (1..100).random()
            if (catchSuccessRoll > 80) {
                val escapeXp = (xp * 0.25f).toLong().coerceAtLeast(4L)
                addXpAndNotify(
                    skill = OsrsSkill.FISHING,
                    amount = escapeXp,
                    gpReward = 0L,
                    logTitle = "Fish Slipped Away",
                    logDesc = "The fish nibbled the hook and slipped off. (+${escapeXp} Fishing XP)"
                )
                if (!isAfk) {
                    addChatMessage("🎣 The fish nibbled and slipped off your hook! (20% escape rate)")
                }
                return@launch
            }

            val finbarLvl = npcFavorMap.value["finbar"]?.first ?: getNpcFavorLevel("finbar")
            var fishYield = 1
            if ((1..100).random() <= finbarLvl) {
                fishYield += 1
                addChatMessage("✨ [Finbar's Favor Perk (+${finbarLvl}%)]: Double fish caught! (+1 extra ${fishToCatch.name}) 🐟🌊")
            }
            val existing = inventoryItems.value.find { it.id == rawId }
            val newQty = (existing?.quantity ?: 0) + fishYield
            saveInventoryItem(rawId, newQty)

            if (fishToCatch.bonusSecondItemId != null && fishToCatch.bonusSecondItemQty > 0) {
                val secId = fishToCatch.bonusSecondItemId!!
                val secExisting = inventoryItems.value.find { it.id == secId }
                val secNewQty = (secExisting?.quantity ?: 0) + fishToCatch.bonusSecondItemQty
                saveInventoryItem(secId, secNewQty)
                val secName = fishToCatch.bonusSecondItemName ?: secId
                addChatMessage("✨ Bonus Catch: +${fishToCatch.bonusSecondItemQty}x $secName ${fishToCatch.bonusSecondItemEmoji ?: ""}!")
            }

            addXpAndNotify(
                skill = OsrsSkill.FISHING,
                amount = xp,
                gpReward = 20L,
                logTitle = "Caught ${fishToCatch.name}",
                logDesc = "Caught 1x $rawName (${fishToCatch.dropChancePercent}% drop chance) in ${currentArea?.name ?: "Shaman Pool"}!"
            )
            addChatMessage("🎣 Caught 1x $rawName (${fishToCatch.dropChancePercent}% drop chance) in ${currentArea?.name ?: "Spirit Pool"}!")
            progressSkillContract(OsrsSkill.FISHING, 1, fishToCatch.id)"""

replacement = """            val finbarLvl = npcFavorMap.value["finbar"]?.first ?: getNpcFavorLevel("finbar")
            var fishYield = 1
            val rolledBonus = (1..100).random() <= finbarLvl
            if (rolledBonus) {
                fishYield += 1
                addChatMessage("✨ [Finbar's Favor Perk (+${finbarLvl}%)]: Double fish caught! (+1 extra ${fishToCatch.name}) 🐟🌊")
            }
            val existing = inventoryItems.value.find { it.id == rawId }
            val newQty = (existing?.quantity ?: 0) + fishYield
            saveInventoryItem(rawId, newQty)

            if (fishToCatch.bonusSecondItemId != null && fishToCatch.bonusSecondItemQty > 0) {
                val secId = fishToCatch.bonusSecondItemId!!
                val secExisting = inventoryItems.value.find { it.id == secId }
                val secNewQty = (secExisting?.quantity ?: 0) + fishToCatch.bonusSecondItemQty
                saveInventoryItem(secId, secNewQty)
                val secName = fishToCatch.bonusSecondItemName ?: secId
                addChatMessage("✨ Bonus Catch: +${fishToCatch.bonusSecondItemQty}x $secName ${fishToCatch.bonusSecondItemEmoji ?: ""}!")
            }

            val catchTitle = if (fishYield > 1) "Caught 2x ${fishToCatch.name} (Double Fish!)" else "Caught ${fishToCatch.name}"
            val catchLogDesc = if (fishYield > 1) {
                "Caught 2x $rawName (✨ Finbar +${finbarLvl}% Extra Fish Perk!) in ${currentArea?.name ?: "Shaman Pool"}!"
            } else {
                "Caught 1x $rawName (${fishToCatch.dropChancePercent}% drop chance) in ${currentArea?.name ?: "Shaman Pool"}!"
            }

            addXpAndNotify(
                skill = OsrsSkill.FISHING,
                amount = xp,
                gpReward = 20L,
                logTitle = catchTitle,
                logDesc = catchLogDesc
            )
            val mainChatMsg = if (fishYield > 1) {
                "🎣 Caught 2x $rawName (✨ Finbar +${finbarLvl}% Double Fish) in ${currentArea?.name ?: "Spirit Pool"}!"
            } else {
                "🎣 Caught 1x $rawName (${fishToCatch.dropChancePercent}% drop chance) in ${currentArea?.name ?: "Spirit Pool"}!"
            }
            addChatMessage(mainChatMsg)
            progressSkillContract(OsrsSkill.FISHING, fishYield, fishToCatch.id)"""

assert target in text, "Target block not found"
text = text.replace(target, replacement, 1)

with open("app/src/main/java/com/example/viewmodel/PetViewModel.kt", "w", encoding="utf-8") as f:
    f.write(text)

print("Updated fishAtPohPond successfully")
