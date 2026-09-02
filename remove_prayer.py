import os

def update_file(path, replacements):
    with open(path, "r", encoding="utf-8") as f:
        text = f.read()
    for old, new in replacements:
        text = text.replace(old, new)
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)
    print(f"Updated {path}")

# 1. MainScreen.kt
update_file("app/src/main/java/com/example/ui/MainScreen.kt", [
    ("\"🦴 Bone Burying (Prayer)\"", "\"🦴 Bone Burying (Magic)\"")
])

# 2. OfferingPouchView.kt
update_file("app/src/main/java/com/example/ui/components/OfferingPouchView.kt", [
    ("Prayer & Magic XP", "Spirit & Magic XP"),
    ("Prayer dedication", "Magic & Spirit dedication"),
    ("basePrayerXp", "baseSpiritXp"),
    ("totalPrayerXp", "totalSpiritXp"),
    ("Prayer XP", "Spirit XP")
])

# 3. OsrsInventoryTab.kt
update_file("app/src/main/java/com/example/ui/components/OsrsInventoryTab.kt", [
    ("recipe.boostedSkill == OsrsSkill.PRAYER ||\n", ""),
    ("recipe.boostedSkill == OsrsSkill.PRAYER || ", "")
])

# 4. OsrsSkillGrid.kt
update_file("app/src/main/java/com/example/ui/components/OsrsSkillGrid.kt", [
    ("OsrsSkill.PRAYER, ", ""),
    (", OsrsSkill.PRAYER", ""),
    ("OsrsSkill.PRAYER,\n", "")
])

# 5. PetDisplayView.kt
update_file("app/src/main/java/com/example/ui/components/PetDisplayView.kt", [
    ("\"bone_burying\" to AfkActivityMetaData(\"bone_burying\", \"Bone Burying\", \"🦴\", \"Prayer\"),",
     "\"bone_burying\" to AfkActivityMetaData(\"bone_burying\", \"Bone Burying\", \"🦴\", \"Magic\"),")
])

# 6. PohHouseTab.kt
update_file("app/src/main/java/com/example/ui/components/PohHouseTab.kt", [
    ("Prayer XP", "Magic XP"),
    ("Prayer & Bone Burying", "Magic & Bone Offering"),
    ("Prayer Point retention", "Magic Power retention"),
    ("HP, Prayer, Energy", "HP, Magic, Energy")
])

# 7. ContractsTab.kt
update_file("app/src/main/java/com/example/ui/tabs/ContractsTab.kt", [
    (", OsrsSkill.PRAYER", ""),
    ("OsrsSkill.PRAYER, ", "")
])

# 8. EncyclopediaTab.kt
update_file("app/src/main/java/com/example/ui/tabs/EncyclopediaTab.kt", [
    ("OsrsSkill.PRAYER,\n", ""),
    ("OsrsSkill.PRAYER, ", "")
])

# 9. ThievingTab.kt
update_file("app/src/main/java/com/example/ui/tabs/ThievingTab.kt", [
    ("Prayer Potions", "Magic Potions")
])

# 10. MagicData.kt
update_file("app/src/main/java/com/example/data/models/MagicData.kt", [
    ("Prayer & Magic XP", "Spirit & Magic XP")
])

# 11. EquipmentData.kt
update_file("app/src/main/java/com/example/data/models/EquipmentData.kt", [
    ("Replacing retired Prayer skill", "Replacing Divination & Spirit focus")
])

# 12. HerbloreData.kt
update_file("app/src/main/java/com/example/data/models/HerbloreData.kt", [
    ("Restores Prayer points", "Restores Magic points")
])

# 13. PetViewModel.kt
update_file("app/src/main/java/com/example/viewmodel/PetViewModel.kt", [
    ("if (listeners[OsrsSkill.PRAYER] == true &&", "if (listeners[OsrsSkill.DIVINATION] == true &&"),
    ("skill = OsrsSkill.PRAYER,", "skill = OsrsSkill.MAGIC,"),
    ("activeSkill = OsrsSkill.PRAYER", "activeSkill = OsrsSkill.MAGIC"),
    ("val prayerXp =", "val spiritXp ="),
    ("accumulatedXp += prayerXp", "accumulatedXp += spiritXp"),
    ("addChatMessage(\"⚠️ AFK Prayer: Out of bones to offer!\")", "addChatMessage(\"⚠️ AFK Magic Offering: Out of bones to offer!\")"),
    ("val prayerXp = ((spec.basePrayerXp * spec.rarity.xpMultiplier) * quantity).toLong()",
     "val magicOfferingXp = ((spec.baseSpiritXp * spec.rarity.xpMultiplier) * quantity).toLong()"),
    ("if (prayerXp > 0) {", "if (magicOfferingXp > 0) {"),
    ("amount = prayerXp,", "amount = magicOfferingXp,"),
    ("logDesc = \"Offered $quantity x ${item.name} to the spirits (+$prayerXp Prayer XP, +$gpReward GP)\"",
     "logDesc = \"Offered $quantity x ${item.name} to the spirits (+$magicOfferingXp Magic XP, +$gpReward GP)\""),
    ("addChatMessage(\"✨ Offering Pouch: Sacrificed $quantity x ${item.name}! (+${prayerXp + magicXp} XP, +$gpReward GP)\")",
     "addChatMessage(\"✨ Offering Pouch: Sacrificed $quantity x ${item.name}! (+${magicOfferingXp + magicXp} Magic XP, +$gpReward GP)\")"),
    ("var totalPrayerXp = 0L", "var totalSpiritXp = 0L"),
    ("totalPrayerXp += ((spec.basePrayerXp * spec.rarity.xpMultiplier) * qty).toLong()",
     "totalSpiritXp += ((spec.baseSpiritXp * spec.rarity.xpMultiplier) * qty).toLong()"),
    ("if (totalPrayerXp > 0) {", "if (totalSpiritXp > 0) {"),
    ("amount = totalPrayerXp,", "amount = totalSpiritXp,"),
    ("logDesc = \"Offered $totalCount ${category.label} items to the spirits! (+$totalPrayerXp Prayer XP, +$totalGp GP)\"",
     "logDesc = \"Offered $totalCount ${category.label} items to the spirits! (+$totalSpiritXp Magic XP, +$totalGp GP)\""),
    ("addChatMessage(\"🔥 Grand Offering: Offered $totalCount items in ${category.label}! (+${totalPrayerXp + magicXp} XP, +$totalGp GP)\")",
     "addChatMessage(\"🔥 Grand Offering: Offered $totalCount items in ${category.label}! (+${totalSpiritXp + magicXp} Magic XP, +$totalGp GP)\")"),
    ("addChatMessage(\"🔥 Grand Offering: Offered $totalCount items in ${category.label}! (+${totalPrayerXp + totalMagicXp} XP, +$totalGp GP)\")",
     "addChatMessage(\"🔥 Grand Offering: Offered $totalCount items in ${category.label}! (+${totalSpiritXp + totalMagicXp} Magic XP, +$totalGp GP)\")"),
    ("card.skill == OsrsSkill.PRAYER -> OsrsSkill.PRAYER to \"Prayer\"",
     "card.skill == OsrsSkill.MAGIC -> OsrsSkill.MAGIC to \"Incantations\""),
    ("// --- PRAYER & AFK BONE BURYING ---", "// --- MAGIC & AFK BONE OFFERING ---"),
    ("addChatMessage(\"🦴 Prayer: Cannot start AFK Bone Burying - No bones in inventory or bank!\")",
     "addChatMessage(\"🦴 Magic Offering: Cannot start AFK Bone Offering - No bones in inventory or bank!\")"),
    ("addChatMessage(\"🦴 Prayer: You don't have any bones in your inventory or bank to bury!\")",
     "addChatMessage(\"🦴 Magic Offering: You don't have any bones in your inventory or bank to offer!\")"),
    ("val basePrayerXp = when (targetBoneId) {", "val baseBoneXp = when (targetBoneId) {"),
    ("val finalPrayerXp = (basePrayerXp * altarMultiplier).toLong()", "val finalBoneXp = (baseBoneXp * altarMultiplier).toLong()"),
    ("// Award Prayer XP", "// Award Magic XP"),
    ("amount = finalPrayerXp,", "amount = finalBoneXp,"),
    ("logDesc = \"$actionText -> Gained +$finalPrayerXp Prayer XP!\"",
     "logDesc = \"$actionText -> Gained +$finalBoneXp Magic XP!\""),
    ("addChatMessage(\"🦴 PRAYER: $actionText! +${finalPrayerXp} Prayer XP earned!$altarTag\")",
     "addChatMessage(\"🦴 MAGIC OFFERING: $actionText! +${finalBoneXp} Magic XP earned!$altarTag\")")
])

print("Completed script.")
