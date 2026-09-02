package com.example.data.models

enum class EquipmentSlot(val displayName: String, val iconSymbol: String) {
    HEAD("Head", "🪖"),
    CAPE("Cape", "📜"),
    AMULET("Amulet", "📿"),
    WEAPON("Weapon", "🗡️"),
    BODY("Body", "🛡️"),
    SHIELD("Shield", "🛡️"),
    LEGS("Legs", "🦵"),
    GLOVES("Gloves", "🧤"),
    BOOTS("Boots", "👢"),
    RING("Ring", "💍"),
    AMMO("Ammo", "➹"),
    AXE("Axe", "🪓")
}

data class EquippedItem(
    val slot: EquipmentSlot,
    val item: InventoryItem
)

data class EquipmentStats(
    val totalCombatPower: Int = 0,
    val totalDefencePower: Int = 0,
    val slayerXpBonusPercent: Int = 0,
    val hunterXpBonusPercent: Int = 0
)
