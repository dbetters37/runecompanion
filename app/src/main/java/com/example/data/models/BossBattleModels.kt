package com.example.data.models

/**
 * Status of an active boss battle encounter.
 */
enum class BossCombatStatus {
    IDLE,
    IN_PROGRESS,
    VICTORY,
    DEFEAT
}

/**
 * Represents an individual scripted move executed by a Boss during a specific turn of combat.
 */
data class BossScriptedMove(
    val turnNumber: Int,
    val moveName: String,
    val emoji: String,
    val baseDamage: Int,
    val specialEffect: String = "NONE", // NONE, CRITICAL, LIFESTEAL, HEAL, SHIELD_PIERCE, MULTI_HIT, CHARGE_UP
    val shieldPierce: Boolean = false,
    val telegraphWarning: String,
    val bossRoarQuote: String,
    val description: String
)

/**
 * Full scripted combat routine and visual theme for a Boss Monster.
 */
data class BossBattleScript(
    val bossId: String,
    val bossName: String,
    val phaseName: String = "Standard Phase",
    val standardRotation: List<BossScriptedMove>,
    val enragedRotation: List<BossScriptedMove> = emptyList(),
    val bgStartColor: Long = 0xFF2B1B17,
    val bgEndColor: Long = 0xFF120B08,
    val ambientParticleColor: Long = 0xFFFFD700
)

/**
 * Pre-defined script rotations for all 13 OSRS-inspired Boss Encounters.
 */
object BossBattleScripts {

    fun getScriptForBoss(boss: BossMonster): BossBattleScript {
        return ALL_SCRIPTS[boss.id] ?: createFallbackScript(boss)
    }

    private fun createFallbackScript(boss: BossMonster): BossBattleScript {
        val dmg = (boss.reqCombatLevel * 0.8).toInt().coerceAtLeast(25)
        val turns = listOf(
            BossScriptedMove(
                turnNumber = 1,
                moveName = "${boss.name} Opening Strike",
                emoji = boss.iconSymbol,
                baseDamage = dmg,
                specialEffect = "NONE",
                shieldPierce = false,
                telegraphWarning = "⚔️ Turn 1: Prepares a heavy standard attack.",
                bossRoarQuote = "'You dare challenge me?!'",
                description = "Direct physical strike against your companion."
            ),
            BossScriptedMove(
                turnNumber = 2,
                moveName = "${boss.name} Shield Shatter",
                emoji = "⚡",
                baseDamage = (dmg * 1.25).toInt(),
                specialEffect = "SHIELD_PIERCE",
                shieldPierce = true,
                telegraphWarning = "⚠️ Turn 2: Charging a devastating SHIELD-PIERCING burst!",
                bossRoarQuote = "'Your puny defense means nothing!'",
                description = "Ignores player shield and directly deals damage."
            ),
            BossScriptedMove(
                turnNumber = 3,
                moveName = "${boss.name} Vitality Drain",
                emoji = "🩸",
                baseDamage = dmg,
                specialEffect = "LIFESTEAL",
                shieldPierce = false,
                telegraphWarning = "🩸 Turn 3: Siphons life force from the attacker.",
                bossRoarQuote = "'Your strength becomes mine!'",
                description = "Deals damage and restores boss health."
            ),
            BossScriptedMove(
                turnNumber = 4,
                moveName = "${boss.name} Cataclysmic Nova",
                emoji = "💥",
                baseDamage = (dmg * 1.5).toInt(),
                specialEffect = "CRITICAL",
                shieldPierce = false,
                telegraphWarning = "💥 Turn 4: Ultimate Critical Cataclysm charging up!",
                bossRoarQuote = "'Perish in the abyss!'",
                description = "Massive critical strike causing heavy damage."
            )
        )
        return BossBattleScript(
            bossId = boss.id,
            bossName = boss.name,
            standardRotation = turns,
            enragedRotation = turns.map { it.copy(baseDamage = (it.baseDamage * 1.3).toInt()) }
        )
    }

    val ALL_SCRIPTS: Map<String, BossBattleScript> = mapOf(
        // 1. Burrowing Void Titan (giant_mole)
        "giant_mole" to BossBattleScript(
            bossId = "giant_mole",
            bossName = "Burrowing Void Titan",
            bgStartColor = 0xFF3D2E1E,
            bgEndColor = 0xFF1A130C,
            ambientParticleColor = 0xFFCD853F,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Subterranean Burrow",
                    emoji = "🪨",
                    baseDamage = 22,
                    specialEffect = "HEAL",
                    shieldPierce = false,
                    telegraphWarning = "🪨 Turn 1: Burrowing beneath bedrock (+Heals 30 HP & strikes)",
                    bossRoarQuote = "*The earth rumbles violently as the Titan tunnels underground!*",
                    description = "Digs deep into the cavern bed, recovering health while tossing stone debris."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Crust Shatter Eruption",
                    emoji = "💥",
                    baseDamage = 38,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 2: Erupting from the earth! (PIERCES SHIELDS)",
                    bossRoarQuote = "*ROOOAAR! The Titan bursts through the cavern floor!*",
                    description = "Sudden explosive upward tremor completely bypassing defense shields."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Jagged Bedrock Barrage",
                    emoji = "🌪️",
                    baseDamage = 28,
                    specialEffect = "MULTI_HIT",
                    shieldPierce = false,
                    telegraphWarning = "🌪️ Turn 3: Hurls spinning sharp bedrock stalactites.",
                    bossRoarQuote = "*Hurls jagged bedrock slabs across the chamber!*",
                    description = "Fires a flurry of crushing stone projectiles."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Seismic Sledge Slam",
                    emoji = "🗿",
                    baseDamage = 46,
                    specialEffect = "CRITICAL",
                    shieldPierce = false,
                    telegraphWarning = "💥 Turn 4: Heavy Seismic Sledge Slam! (1.4x CRITICAL DMG)",
                    bossRoarQuote = "*CRUUSHHH! The Titan slams its colossal paws downward!*",
                    description = "Full weight crushing slam dealing heavy critical damage."
                )
            ),
            enragedRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Enraged Cavern Collapse",
                    emoji = "🌋",
                    baseDamage = 45,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ ENRAGED Turn 1: Shatters roof! (SHIELD PIERCE 45 DMG)",
                    bossRoarQuote = "*The entire ceiling collapses under the Titan's rage!*",
                    description = "Enraged subterranean quake raining boulders from above."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Titan Bloodthirsty Rampage",
                    emoji = "🐾",
                    baseDamage = 52,
                    specialEffect = "CRITICAL",
                    shieldPierce = false,
                    telegraphWarning = "💥 ENRAGED Turn 2: Brutal Claw Thrash (CRITICAL 52 DMG)",
                    bossRoarQuote = "*Claws tear through the air with ferocious speed!*",
                    description = "Frenzied claw assault striking with devastating momentum."
                )
            )
        ),

        // 2. Astral Flame Drake (king_black_dragon)
        "king_black_dragon" to BossBattleScript(
            bossId = "king_black_dragon",
            bossName = "Astral Flame Drake",
            bgStartColor = 0xFF4A1515,
            bgEndColor = 0xFF1C0808,
            ambientParticleColor = 0xFFFF4500,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Astral Triple Breath",
                    emoji = "🔥",
                    baseDamage = 34,
                    specialEffect = "MULTI_HIT",
                    shieldPierce = false,
                    telegraphWarning = "🔥 Turn 1: 3-Headed Fire, Ice & Toxic Breath barrage!",
                    bossRoarQuote = "*Three heads hiss simultaneously, charging cosmic flames!*",
                    description = "Unleashes multi-elemental breath from all three heads."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Wyrm Tail Cleave",
                    emoji = "🐉",
                    baseDamage = 30,
                    specialEffect = "NONE",
                    shieldPierce = false,
                    telegraphWarning = "🐉 Turn 2: Sweeping heavy armored tail strike.",
                    bossRoarQuote = "*Whips its massive barbed dragon tail in a broad arc!*",
                    description = "Sweeping physical tail strike buffeting your companion."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Astral Scale Fortification",
                    emoji = "🛡️",
                    baseDamage = 26,
                    specialEffect = "HEAL",
                    shieldPierce = false,
                    telegraphWarning = "🛡️ Turn 3: Astral Scale Shield (+Restores 45 HP & attacks)",
                    bossRoarQuote = "*Cosmic energy glimmers across its obsidian dragon scales!*",
                    description = "Hardens astral armor, restoring vitality and reflecting force."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Supernova Drake Inferno",
                    emoji = "☀️",
                    baseDamage = 56,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 4: SUPERNOVA INFERNO (Bypasses all Shields!)",
                    bossRoarQuote = "*THE HEAVENS BURN! A concentrated supernova beam unleashes!*",
                    description = "Piercing solar plasma beam that completely bypasses shields."
                )
            ),
            enragedRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Dragonfire Cataclysm",
                    emoji = "🔥",
                    baseDamage = 62,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ ENRAGED Turn 1: True Dragonfire (PIERCES SHIELD 62 DMG)",
                    bossRoarQuote = "*The Drake's eyes burn with furious celestial fire!*",
                    description = "Cataclysmic dragonfire consuming everything in the lair."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Apex Predator Decapitation",
                    emoji = "⚡",
                    baseDamage = 68,
                    specialEffect = "CRITICAL",
                    shieldPierce = false,
                    telegraphWarning = "💥 ENRAGED Turn 2: Apex Wyrm Jaw Strike (CRITICAL 68 DMG)",
                    bossRoarQuote = "*All three heads strike in a synchronized execution snap!*",
                    description = "Lethal triple bite causing immense critical trauma."
                )
            )
        ),

        // 3. Crypt Sentinel Warlord (barrows_brothers)
        "barrows_brothers" to BossBattleScript(
            bossId = "barrows_brothers",
            bossName = "Crypt Sentinel Warlord",
            bgStartColor = 0xFF2A2A38,
            bgEndColor = 0xFF0F0F1A,
            ambientParticleColor = 0xFF9370DB,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Dharok's Desperate Cleave",
                    emoji = "🪓",
                    baseDamage = 44,
                    specialEffect = "CRITICAL",
                    shieldPierce = false,
                    telegraphWarning = "🪓 Turn 1: Nether Greataxe Overhand Chop (CRITICAL DMG)",
                    bossRoarQuote = "'Feel the sorrow of the fallen brother!'",
                    description = "Crushing great-axe overhead execution cleave."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Ahrim's Blighted Curse",
                    emoji = "🧙‍♂️",
                    baseDamage = 35,
                    specialEffect = "NONE",
                    shieldPierce = false,
                    telegraphWarning = "🧙‍♂️ Turn 2: Shadow Necromancy Curse blast.",
                    bossRoarQuote = "'Your vitality wanes in the crypt!'",
                    description = "Shadow spell weakening your pet's spirit."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Karil's Piercing Shadow Bolt",
                    emoji = "🏹",
                    baseDamage = 42,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 3: Repeating Crossbow Bolt (SHIELD PIERCE)",
                    bossRoarQuote = "*TWANG! A volley of shadow bolts slips through your guard!*",
                    description = "Swift piercing spectral bolt that ignores shield."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Guthan's Soul Vampirism",
                    emoji = "🗡️",
                    baseDamage = 38,
                    specialEffect = "LIFESTEAL",
                    shieldPierce = false,
                    telegraphWarning = "🩸 Turn 4: Infused Warspear (LIFESTEAL - Drains HP)",
                    bossRoarQuote = "'The crypt feeds upon the living!'",
                    description = "Warspear thrust siphoning your companion's health."
                )
            )
        ),

        // 4. Abyssal Leviathan Kings (dagannoth_kings)
        "dagannoth_kings" to BossBattleScript(
            bossId = "dagannoth_kings",
            bossName = "Abyssal Leviathan Kings",
            bgStartColor = 0xFF102830,
            bgEndColor = 0xFF081418,
            ambientParticleColor = 0xFF00CED1,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Rex's Crushing Stomp",
                    emoji = "🗡️",
                    baseDamage = 36,
                    specialEffect = "NONE",
                    shieldPierce = false,
                    telegraphWarning = "🗡️ Turn 1: King Rex charges with a heavy ground stomp.",
                    bossRoarQuote = "*King Rex roars and stomps with heavy spiked claws!*",
                    description = "Heavy melee crush from King Rex."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Prime's Water Surge Spikes",
                    emoji = "🏹",
                    baseDamage = 40,
                    specialEffect = "NONE",
                    shieldPierce = false,
                    telegraphWarning = "🏹 Turn 2: King Prime aims high-velocity aquatic darts.",
                    bossRoarQuote = "*King Prime fires a volley of pressurized water spine arrows!*",
                    description = "Rapid piercing aquatic spine volley."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Supreme's Deep Sea Maelstrom",
                    emoji = "🪄",
                    baseDamage = 44,
                    specialEffect = "LIFESTEAL",
                    shieldPierce = false,
                    telegraphWarning = "🩸 Turn 3: King Supreme casts Abyssal Siphon Magic.",
                    bossRoarQuote = "*King Supreme channels dark oceanic magic!*",
                    description = "Concentrated tidal whirlpool siphoning stamina."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Triumvirate Tidal Cataclysm",
                    emoji = "👑",
                    baseDamage = 58,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 4: TRIUMVIRATE WAVE (3 Kings Combined • PIERCES SHIELD)",
                    bossRoarQuote = "*ALL THREE KINGS ROAR IN UNISON AS A TIDAL WAVE CRASHES!*",
                    description = "Massive combined three-king tidal surge ignoring shields."
                )
            )
        ),

        // 5. Venomous Serpent Sovereign (zulrah)
        "zulrah" to BossBattleScript(
            bossId = "zulrah",
            bossName = "Venomous Serpent Sovereign",
            bgStartColor = 0xFF143018,
            bgEndColor = 0xFF081A0C,
            ambientParticleColor = 0xFF00FF7F,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Corrosive Venom Cloud",
                    emoji = "🧪",
                    baseDamage = 38,
                    specialEffect = "NONE",
                    shieldPierce = false,
                    telegraphWarning = "🧪 Turn 1: Spits virulent venom cloud over the platform.",
                    bossRoarQuote = "*HISS! Green acidic toxic mist fills the shrine!*",
                    description = "Acidic poisonous mist blanketing the battle ground."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Tanzanite Snakeling Swarm",
                    emoji = "🐍",
                    baseDamage = 45,
                    specialEffect = "MULTI_HIT",
                    shieldPierce = false,
                    telegraphWarning = "🐍 Turn 2: Snakeling swarm hatches and bites in frenzy!",
                    bossRoarQuote = "*A nest of venomous snakelings erupts and attacks!*",
                    description = "Multi-hit strike from newly hatched serpent minions."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Magma Form Blood Siphon",
                    emoji = "🩸",
                    baseDamage = 40,
                    specialEffect = "LIFESTEAL",
                    shieldPierce = false,
                    telegraphWarning = "🩸 Turn 3: Switches to Magma form (LIFESTEAL Blood Strike)",
                    bossRoarQuote = "*Scales turn blazing crimson as Zulrah strikes!*",
                    description = "Lifesteal attack siphoning vitality to heal serpent HP."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Jad Phase Toxic Annihilation",
                    emoji = "☣️",
                    baseDamage = 64,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 4: JAD PHASE RAPID SHOT (Shield Pierce 64 DMG!)",
                    bossRoarQuote = "*RAPID FIRE! Alternating Magic and Ranged venom blasts!*",
                    description = "Lethal alternating projectile barrage ignoring defense shields."
                )
            )
        ),

        // 6. Frostbite Revenant Wyrm (vorkath)
        "vorkath" to BossBattleScript(
            bossId = "vorkath",
            bossName = "Frostbite Revenant Wyrm",
            bgStartColor = 0xFF14243A,
            bgEndColor = 0xFF081220,
            ambientParticleColor = 0xFF00BFFF,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Glacial Ice Breath",
                    emoji = "❄️",
                    baseDamage = 42,
                    specialEffect = "NONE",
                    shieldPierce = false,
                    telegraphWarning = "❄️ Turn 1: Frost breath chilling the cavern atmosphere.",
                    bossRoarQuote = "*The air temperature plummets below zero!*",
                    description = "Freezing ice blast coating the arena in frost."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Zombified Spawn Blast",
                    emoji = "💣",
                    baseDamage = 50,
                    specialEffect = "CRITICAL",
                    shieldPierce = false,
                    telegraphWarning = "💣 Turn 2: Summons exploding zombified spawn (CRITICAL DMG)!",
                    bossRoarQuote = "*A frantic undead spawn rushes forward and detonates!*",
                    description = "Explosive minion bomb dealing heavy critical impact."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Draconic Acid Rain Pools",
                    emoji = "🐉",
                    baseDamage = 38,
                    specialEffect = "HEAL",
                    shieldPierce = false,
                    telegraphWarning = "🐉 Turn 3: Acid pools rain down (+Heals 40 Wyrm HP)",
                    bossRoarQuote = "*Vorkath takes flight, raining pools of caustic acid!*",
                    description = "Rains burning acid while resting to restore HP."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Frostbite Fireball of Death",
                    emoji = "☠️",
                    baseDamage = 72,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 4: ONE-HIT FIREBALL OF DEATH (SHIELD PIERCE 72 DMG)",
                    bossRoarQuote = "*A massive, glowing undead fireball descends from the sky!*",
                    description = "Cataclysmic high-altitude fireball bypassing all shields."
                )
            )
        ),

        // 7. Iron Titan Warlord (general_graardor)
        "general_graardor" to BossBattleScript(
            bossId = "general_graardor",
            bossName = "Iron Titan Warlord",
            bgStartColor = 0xFF362818,
            bgEndColor = 0xFF1A1208,
            ambientParticleColor = 0xFFFF8C00,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Iron Fortress Slam",
                    emoji = "🛡️",
                    baseDamage = 44,
                    specialEffect = "NONE",
                    shieldPierce = false,
                    telegraphWarning = "🛡️ Turn 1: Warlord raises iron gauntlet for a direct slam.",
                    bossRoarQuote = "'Break their bones for the High General!'",
                    description = "Heavy armored gauntlet punch."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Minion Crossfire Volley",
                    emoji = "💥",
                    baseDamage = 48,
                    specialEffect = "MULTI_HIT",
                    shieldPierce = false,
                    telegraphWarning = "💥 Turn 2: Sergeants Steelwill & Grimspike open fire!",
                    bossRoarQuote = "*Goblin sergeants volley arrows and magic spikes!*",
                    description = "Multi-target minion crossfire supporting the warlord."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Reinforced Warplate",
                    emoji = "🧱",
                    baseDamage = 32,
                    specialEffect = "HEAL",
                    shieldPierce = false,
                    telegraphWarning = "🧱 Turn 3: Warplate Fortify (+50 HP restored)",
                    bossRoarQuote = "*Graardor adjusts his heavy Bandos warplate!*",
                    description = "Repairs armor plates and restores combat vitality."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Warlord's Earthbreaker",
                    emoji = "⚡",
                    baseDamage = 70,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 4: EARTHBREAKER (Shatters Shields • 70 DMG)",
                    bossRoarQuote = "'BANDOS SMASH!! The room shakes violently!'",
                    description = "Massive overhead smash shattering defense shields."
                )
            )
        ),

        // 8. Celestial Light Sentinel (commander_zilyana)
        "commander_zilyana" to BossBattleScript(
            bossId = "commander_zilyana",
            bossName = "Celestial Light Sentinel",
            bgStartColor = 0xFF2A2840,
            bgEndColor = 0xFF141220,
            ambientParticleColor = 0xFFFFD700,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Holy Dawn Greatsword",
                    emoji = "⚔️",
                    baseDamage = 46,
                    specialEffect = "NONE",
                    shieldPierce = false,
                    telegraphWarning = "⚔️ Turn 1: Swift golden sword slash.",
                    bossRoarQuote = "'In the name of the divine light, surrender!'",
                    description = "Graceful high-speed holy greatsword thrust."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Celestial Eagle Volley",
                    emoji = "🏹",
                    baseDamage = 50,
                    specialEffect = "MULTI_HIT",
                    shieldPierce = false,
                    telegraphWarning = "🏹 Turn 2: Multi-shot luminous light arrow volley.",
                    bossRoarQuote = "*Fires a barrage of radiant arrows with blinding speed!*",
                    description = "Volley of light arrows whistling through the air."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Archangel Holy Aura",
                    emoji = "✨",
                    baseDamage = 35,
                    specialEffect = "HEAL",
                    shieldPierce = false,
                    telegraphWarning = "✨ Turn 3: Archangel Light Prayer (+60 HP Heal)",
                    bossRoarQuote = "'Light, grant me renewed strength to banish darkness!'",
                    description = "Channels holy prayer aura to restore vitality."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Judgment of the Heavens",
                    emoji = "🌟",
                    baseDamage = 74,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 4: HEAVEN'S PILLAR (Pierces Shield • 74 DMG)",
                    bossRoarQuote = "'MAY SACRED LIGHT PURIFY YOUR SOUL!'",
                    description = "Pillar of solar holy wrath disintegrating shields."
                )
            )
        ),

        // 9. Kraken of the Abyss (kraken)
        "kraken" to BossBattleScript(
            bossId = "kraken",
            bossName = "Kraken of the Abyss",
            bgStartColor = 0xFF0C2434,
            bgEndColor = 0xFF04101A,
            ambientParticleColor = 0xFF20B2AA,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Tentacle Whip Thrash",
                    emoji = "🐙",
                    baseDamage = 40,
                    specialEffect = "NONE",
                    shieldPierce = false,
                    telegraphWarning = "🐙 Turn 1: 4 tentacles whip across the waters.",
                    bossRoarQuote = "*Massive suction tentacles rise from the deep water!*",
                    description = "Physical tentacle lash against the boat."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Abyssal Whirlpool Pull",
                    emoji = "🌊",
                    baseDamage = 46,
                    specialEffect = "CRITICAL",
                    shieldPierce = false,
                    telegraphWarning = "🌊 Turn 2: Swirling whirlpool dragging player down (CRITICAL)",
                    bossRoarQuote = "*The ocean currents spin into a ferocious vortex!*",
                    description = "Crushing deep-sea water vortex pressure."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Blinding Ink Siphon",
                    emoji = "🪄",
                    baseDamage = 42,
                    specialEffect = "LIFESTEAL",
                    shieldPierce = false,
                    telegraphWarning = "🩸 Turn 3: Dark ink cloud siphoning health (LIFESTEAL)",
                    bossRoarQuote = "*Spews jet-black mystic ink, draining player vigor!*",
                    description = "Siphons spirit energy through arcane ink spray."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Deep Trench Maelstrom",
                    emoji = "⚡",
                    baseDamage = 66,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 4: DEEP TRENCH CATACLYSM (Shield Pierce 66 DMG)",
                    bossRoarQuote = "*THE OCEAN ROARS AS THE ENTIRE SEA COLLAPSES DOWNWARD!*",
                    description = "Overwhelming tidal pressure bypassing defense barriers."
                )
            )
        ),

        // 10. Inferno Dreadhound (cerberus)
        "cerberus" to BossBattleScript(
            bossId = "cerberus",
            bossName = "Inferno Dreadhound",
            bgStartColor = 0xFF4A1010,
            bgEndColor = 0xFF1F0404,
            ambientParticleColor = 0xFFFF4500,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Triple Hellfire Maw",
                    emoji = "🔥",
                    baseDamage = 48,
                    specialEffect = "MULTI_HIT",
                    shieldPierce = false,
                    telegraphWarning = "🔥 Turn 1: Three heads lunge with flaming jaws!",
                    bossRoarQuote = "*All three heads snarl fiercely and snap their jaws!*",
                    description = "Multi-hit bite from the three hellhound heads."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "River of Souls Phantoms",
                    emoji = "👻",
                    baseDamage = 54,
                    specialEffect = "CRITICAL",
                    shieldPierce = false,
                    telegraphWarning = "👻 Turn 2: 'Aarrroooo!' Summoning Tormented Souls (CRITICAL)!",
                    bossRoarQuote = "'ARROOOOOOO! The ghosts of the River of Souls advance!'",
                    description = "Three spectral ghosts drain health and energy."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Lava Pool Eruption",
                    emoji = "🌋",
                    baseDamage = 44,
                    specialEffect = "HEAL",
                    shieldPierce = false,
                    telegraphWarning = "🌋 Turn 3: Blazing magma erupts (+55 Dreadhound HP)",
                    bossRoarQuote = "*Molten lava pools burst through the iron grates!*",
                    description = "Basks in magma flames to restore vitality."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Hellhound Enraged Blast",
                    emoji = "🐕",
                    baseDamage = 78,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 4: HELLFIRE BLAST (Shield Pierce 78 DMG!)",
                    bossRoarQuote = "*A massive wave of pure underworld hellfire erupts!*",
                    description = "Underworld flame nova burning through all shields."
                )
            )
        ),

        // 11. Void Shift Revenant (phantom_muspah)
        "phantom_muspah" to BossBattleScript(
            bossId = "phantom_muspah",
            bossName = "Void Shift Revenant",
            bgStartColor = 0xFF2A1040,
            bgEndColor = 0xFF10041C,
            ambientParticleColor = 0xFFBA55D3,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Shadow Stalker Shift",
                    emoji = "👻",
                    baseDamage = 50,
                    specialEffect = "NONE",
                    shieldPierce = false,
                    telegraphWarning = "👻 Turn 1: Teleports behind you in Melee Form.",
                    bossRoarQuote = "*Muspah dissolves into shadow mist and reappears behind you!*",
                    description = "Surprise shadow strike from behind."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Void Spike Barrage",
                    emoji = "🏹",
                    baseDamage = 56,
                    specialEffect = "MULTI_HIT",
                    shieldPierce = false,
                    telegraphWarning = "🏹 Turn 2: Shifts to Ranged Form (Spike Storm)",
                    bossRoarQuote = "*Muspah shifts into Ranged Form, hurling void spikes!*",
                    description = "Flurry of dark crystalline ice spikes."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Soul Siphon Shield",
                    emoji = "🔮",
                    baseDamage = 48,
                    specialEffect = "LIFESTEAL",
                    shieldPierce = false,
                    telegraphWarning = "🩸 Turn 3: Soul Siphon (Restores HP while attacking)",
                    bossRoarQuote = "*Dark energy pulses outward, feeding upon your spirit!*",
                    description = "Soul-drain tendrils restoring Muspah's health."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Void Realm Singularity",
                    emoji = "🌌",
                    baseDamage = 82,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 4: VOID SINGULARITY (Shield Pierce 82 DMG!)",
                    bossRoarQuote = "*A miniature black hole collapses inward on the battlefield!*",
                    description = "Space-time void distortion crushing through defense shields."
                )
            )
        ),

        // 12. Bloodwood Executioner (vardorvis)
        "vardorvis" to BossBattleScript(
            bossId = "vardorvis",
            bossName = "Bloodwood Executioner",
            bgStartColor = 0xFF421010,
            bgEndColor = 0xFF1C0505,
            ambientParticleColor = 0xFFFF1493,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Executioner's Cleave",
                    emoji = "🩸",
                    baseDamage = 52,
                    specialEffect = "NONE",
                    shieldPierce = false,
                    telegraphWarning = "🩸 Turn 1: Sweeping heavy broadsword cleave.",
                    bossRoarQuote = "*Swings heavy twin blood-stained blades!*",
                    description = "Vicious slashing strike from Vardorvis."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Whirling Flying Axes",
                    emoji = "🪓",
                    baseDamage = 58,
                    specialEffect = "MULTI_HIT",
                    shieldPierce = false,
                    telegraphWarning = "🪓 Turn 2: Throws spinning axes across the arena!",
                    bossRoarQuote = "*Two spinning razor axes fly outward in circular arcs!*",
                    description = "Twin spinning axes cutting across the battle line."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Stranglewood Bloodthirst",
                    emoji = "🩸",
                    baseDamage = 50,
                    specialEffect = "LIFESTEAL",
                    shieldPierce = false,
                    telegraphWarning = "🩸 Turn 3: Bloodthirst Aura (LIFESTEAL Drain)",
                    bossRoarQuote = "*Vardorvis drinks in the fresh blood to rejuvenate!*",
                    description = "Drains life force to mend battle wounds."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Decapitation Surge",
                    emoji = "💀",
                    baseDamage = 88,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 4: DECAPITATION SURGE (Shield Pierce 88 DMG!)",
                    bossRoarQuote = "*'DIE IN THE STRANGLEWOOD!' A devastating execution swing!*",
                    description = "Unstoppable high-velocity strike piercing all defenses."
                )
            )
        ),

        // 13. Ethereal Calamity Behemoth (corporeal_beast)
        "corporeal_beast" to BossBattleScript(
            bossId = "corporeal_beast",
            bossName = "Ethereal Calamity Behemoth",
            bgStartColor = 0xFF281036,
            bgEndColor = 0xFF100418,
            ambientParticleColor = 0xFFDA70D6,
            standardRotation = listOf(
                BossScriptedMove(
                    turnNumber = 1,
                    moveName = "Ethereal Trample",
                    emoji = "🦬",
                    baseDamage = 56,
                    specialEffect = "NONE",
                    shieldPierce = false,
                    telegraphWarning = "🦬 Turn 1: Tramples forward with colossal spectral hooves.",
                    bossRoarQuote = "*The ground groans under the Behemoth's immense spirit weight!*",
                    description = "Crushing spectral charge."
                ),
                BossScriptedMove(
                    turnNumber = 2,
                    moveName = "Dark Core Spirit Bomb",
                    emoji = "⚡",
                    baseDamage = 62,
                    specialEffect = "MULTI_HIT",
                    shieldPierce = false,
                    telegraphWarning = "⚡ Turn 2: Spawns jumping Dark Core spirit orb!",
                    bossRoarQuote = "*The Dark Core leaps out, bombarding from the air!*",
                    description = "High-velocity spirit energy projectile."
                ),
                BossScriptedMove(
                    turnNumber = 3,
                    moveName = "Spirit Core Vampirism",
                    emoji = "💫",
                    baseDamage = 55,
                    specialEffect = "LIFESTEAL",
                    shieldPierce = false,
                    telegraphWarning = "🩸 Turn 3: Dark Core Siphon (+Restores 70 HP)",
                    bossRoarQuote = "*The Core latches on, draining pure life essence!*",
                    description = "Siphons massive vitality to restore the behemoth's health."
                ),
                BossScriptedMove(
                    turnNumber = 4,
                    moveName = "Apocalyptic Calamity Roar",
                    emoji = "👑",
                    baseDamage = 96,
                    specialEffect = "SHIELD_PIERCE",
                    shieldPierce = true,
                    telegraphWarning = "⚠️ Turn 4: APOCALYPTIC ROAR (Shield Pierce 96 DMG!)",
                    bossRoarQuote = "*A ROAR OF COSMIC DESTRUCTION RIPS THROUGH THE CAVERN!*",
                    description = "Cataclysmic sonic wave completely obliterating defense shields."
                )
            )
        )
    )
}
