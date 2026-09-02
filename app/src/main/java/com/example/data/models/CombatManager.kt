package com.example.data.models

/**
 * Data class representing a detailed breakdown of combat stat calculations.
 */
data class CombatBreakdown(
    val attackLevel: Int,
    val defenceLevel: Int,
    val hitpointsLevel: Int,
    val rangedLevel: Int,
    val magicLevel: Int,
    val baseStat: Double,
    val meleeStat: Double,
    val rangeStat: Double,
    val mageStat: Double,
    val maxOffense: Double,
    val exactCombatLevel: Double,
    val combatLevel: Int
)

/**
 * Centralized CombatManager object serving as the single source of truth
 * for all Combat Level calculations and stat breakdowns across the entire application
 * (Skills Grid, Shaman Path / Trainer League, Quests, Adventuring, Slayer, and ViewModel).
 */
object CombatManager {

    /**
     * Standard OSRS Combat Level formula:
     * Base = 0.25 * (Defence + Hitpoints)
     * Melee = 0.325 * (Attack * 2)
     * Ranged = 0.325 * ((Ranged * 3) / 2)
     * Magic = 0.325 * ((Magic * 3) / 2)
     * MaxOffense = max(Melee, Ranged, Magic)
     * CombatLevel = floor(Base + MaxOffense), minimum 3
     */
    fun calculateCombatLevel(skillXpMap: Map<OsrsSkill, Long>): Int {
        val att = OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.ATTACK] ?: 0L)
        val def = OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.DEFENCE] ?: 0L)
        val hp = OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.HITPOINTS] ?: 0L)
        val rng = OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.RANGED] ?: 0L)
        val mag = OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.MAGIC] ?: 0L)
        return calculateCombatLevel(att, def, hp, rng, mag)
    }

    fun calculateCombatLevel(att: Int, def: Int, hp: Int, rng: Int, mag: Int): Int {
        val baseStat = 0.25 * (def + hp)
        val meleeStat = 0.325 * (att * 2)
        val rangeStat = 0.325 * ((rng * 3) / 2)
        val mageStat = 0.325 * ((mag * 3) / 2)

        val maxOffense = maxOf(meleeStat, maxOf(rangeStat, mageStat))
        return (baseStat + maxOffense).toInt().coerceAtLeast(3)
    }

    fun getCombatBreakdown(skillXpMap: Map<OsrsSkill, Long>): CombatBreakdown {
        val att = OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.ATTACK] ?: 0L)
        val def = OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.DEFENCE] ?: 0L)
        val hp = OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.HITPOINTS] ?: 0L)
        val rng = OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.RANGED] ?: 0L)
        val mag = OsrsXpCalculator.getLevelForXp(skillXpMap[OsrsSkill.MAGIC] ?: 0L)
        return getCombatBreakdown(att, def, hp, rng, mag)
    }

    fun getCombatBreakdown(att: Int, def: Int, hp: Int, rng: Int, mag: Int): CombatBreakdown {
        val baseStat = 0.25 * (def + hp)
        val meleeStat = 0.325 * (att * 2)
        val rangeStat = 0.325 * ((rng * 3) / 2)
        val mageStat = 0.325 * ((mag * 3) / 2)

        val maxOffense = maxOf(meleeStat, maxOf(rangeStat, mageStat))
        val exact = baseStat + maxOffense
        val combatLevel = exact.toInt().coerceAtLeast(3)

        return CombatBreakdown(
            attackLevel = att,
            defenceLevel = def,
            hitpointsLevel = hp,
            rangedLevel = rng,
            magicLevel = mag,
            baseStat = baseStat,
            meleeStat = meleeStat,
            rangeStat = rangeStat,
            mageStat = mageStat,
            maxOffense = maxOffense,
            exactCombatLevel = exact,
            combatLevel = combatLevel
        )
    }

    fun hasCombatRequirement(petCombatLevel: Int, requiredCombatLevel: Int): Boolean {
        return petCombatLevel >= requiredCombatLevel
    }
}
