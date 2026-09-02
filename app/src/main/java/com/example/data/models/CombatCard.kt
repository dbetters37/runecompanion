package com.example.data.models

enum class CardType {
    ATTACK, DEFENSE, MAGIC, CONSUMABLE, ULTIMATE, BUFF
}

data class CombatCard(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val energyCost: Int, // 0 for non-combat skill cards, 1-3 for combat skill cards
    val cardType: CardType,
    val stance: String = "ALL", // "MELEE", "RANGED", "MAGIC", or "ALL"
    val baseDamage: Int = 0,
    val baseShield: Int = 0,
    val baseHeal: Int = 0,
    val nextAttackBuff: Int = 0, // Bonus damage added to player's NEXT attack
    val description: String = "",
    val skill: OsrsSkill = OsrsSkill.ATTACK,
    val reqLevel: Int = 1
) {
    fun isCombatDamageCard(): Boolean = baseDamage > 0 || cardType == CardType.ATTACK || cardType == CardType.MAGIC || cardType == CardType.ULTIMATE
    fun isCombatSkillCard(): Boolean = skill in listOf(
        OsrsSkill.ATTACK, OsrsSkill.DEFENCE,
        OsrsSkill.RANGED, OsrsSkill.MAGIC, OsrsSkill.HITPOINTS
    )

    fun isCombatCardForStance(stance: String): Boolean {
        val upperStance = stance.uppercase()
        return when (upperStance) {
            "MELEE" -> {
                // Hand-combat (Attack), Warding (Defence), Life Energy (Hitpoints)
                skill == OsrsSkill.ATTACK || skill == OsrsSkill.DEFENCE || skill == OsrsSkill.HITPOINTS || cardType == CardType.ATTACK || cardType == CardType.DEFENSE
            }
            "RANGED" -> {
                // Blow Darts (Ranged), Eating (Cooking / Herblore / Consumable), Life Energy (Hitpoints)
                skill == OsrsSkill.RANGED || skill == OsrsSkill.COOKING || skill == OsrsSkill.HERBLORE || skill == OsrsSkill.HITPOINTS || cardType == CardType.CONSUMABLE || (cardType == CardType.ATTACK && (this.stance == "RANGED" || this.stance == "ALL"))
            }
            "MAGIC" -> {
                // Incantation (Magic), Warding (Defence), Life Energy (Hitpoints)
                skill == OsrsSkill.MAGIC || skill == OsrsSkill.DEFENCE || skill == OsrsSkill.HITPOINTS || cardType == CardType.MAGIC || cardType == CardType.DEFENSE
            }
            else -> isCombatDamageCard() || isCombatSkillCard()
        }
    }
}

object DefaultCombatCards {
    private fun c(
        id: String, title: String, emoji: String, cost: Int, type: CardType,
        stance: String = "ALL", dmg: Int = 0, shd: Int = 0, heal: Int = 0,
        buff: Int = 0, desc: String, skill: OsrsSkill, lvl: Int
    ) = CombatCard(
        id = id, title = title, iconEmoji = emoji, energyCost = cost, cardType = type,
        stance = stance, baseDamage = dmg, baseShield = shd, baseHeal = heal,
        nextAttackBuff = buff, description = desc, skill = skill, reqLevel = lvl
    )

    val ATTACK_CARDS = listOf(
        c("atk_1", "Quick Strike", "⚔️", 1, CardType.ATTACK, "MELEE", dmg = 18, desc = "Fast strike dealing 18 Melee damage.", skill = OsrsSkill.ATTACK, lvl = 1),
        c("atk_5", "Bronze Thrust", "🗡️", 1, CardType.ATTACK, "MELEE", dmg = 22, desc = "Bronze thrust dealing 22 Melee damage.", skill = OsrsSkill.ATTACK, lvl = 5),
        c("atk_10", "Iron Slice", "⚔️", 1, CardType.ATTACK, "MELEE", dmg = 26, desc = "Sharp iron slice dealing 26 damage.", skill = OsrsSkill.ATTACK, lvl = 10),
        c("atk_15", "Heavy Cleave", "🪓", 2, CardType.ATTACK, "MELEE", dmg = 36, desc = "Powerful blow dealing 36 heavy damage.", skill = OsrsSkill.ATTACK, lvl = 15),
        c("atk_20", "Steel Flurry", "⚔️", 2, CardType.ATTACK, "MELEE", dmg = 40, desc = "Rapid steel blades dealing 40 damage.", skill = OsrsSkill.ATTACK, lvl = 20),
        c("atk_25", "Power Slam", "💪", 2, CardType.ATTACK, "MELEE", dmg = 45, desc = "Muscle-powered strike dealing 45 damage.", skill = OsrsSkill.ATTACK, lvl = 25),
        c("atk_30", "Mithril Jab", "🗡️", 2, CardType.ATTACK, "MELEE", dmg = 50, desc = "Mithril precision strike dealing 50 damage.", skill = OsrsSkill.ATTACK, lvl = 30),
        c("atk_35", "Earthshaker", "💥", 2, CardType.ATTACK, "MELEE", dmg = 56, desc = "Slam the ground dealing 56 heavy damage.", skill = OsrsSkill.ATTACK, lvl = 35),
        c("atk_40", "Adamant Lunge", "⚔️", 2, CardType.ATTACK, "MELEE", dmg = 62, desc = "Heavy adamant lunge dealing 62 damage.", skill = OsrsSkill.ATTACK, lvl = 40),
        c("atk_50", "Rune Uppercut", "🗡️", 2, CardType.ATTACK, "MELEE", dmg = 72, desc = "Crushing rune strike dealing 72 damage.", skill = OsrsSkill.ATTACK, lvl = 50),
        c("atk_60", "Titan Punch", "🥊", 3, CardType.ULTIMATE, "MELEE", dmg = 85, desc = "Titan strike dealing 85 crushing damage!", skill = OsrsSkill.ATTACK, lvl = 60),
        c("atk_70", "Dragon Scimitar Slash", "🐉", 3, CardType.ULTIMATE, "MELEE", dmg = 98, desc = "Dragon scimitar slash dealing 98 damage!", skill = OsrsSkill.ATTACK, lvl = 70),
        c("atk_75", "Dragon Claw Flurry", "🐉✨", 3, CardType.ULTIMATE, "MELEE", dmg = 110, desc = "Lethal dragon claws dealing 110 crushing damage!", skill = OsrsSkill.ATTACK, lvl = 75),
        c("atk_80", "Abyssal Whip Flick", "🩸", 3, CardType.ULTIMATE, "MELEE", dmg = 122, desc = "Abyssal whip flick dealing 122 damage!", skill = OsrsSkill.ATTACK, lvl = 80),
        c("atk_85", "Godsword Overhead", "⚔️✨", 3, CardType.ULTIMATE, "MELEE", dmg = 138, desc = "Godsword overhead strike dealing 138 damage!", skill = OsrsSkill.ATTACK, lvl = 85),
        c("atk_90", "Elder Maul Shatter", "🔨", 3, CardType.ULTIMATE, "MELEE", dmg = 152, desc = "Elder maul shatter dealing 152 damage!", skill = OsrsSkill.ATTACK, lvl = 90),
        c("atk_99", "Master Execution", "👑⚔️", 3, CardType.ULTIMATE, "MELEE", dmg = 175, desc = "Master execution dealing 175 lethal damage!", skill = OsrsSkill.ATTACK, lvl = 99)
    )

    val DEFENCE_CARDS = listOf(
        c("def_1", "Shield Bash", "🛡️", 1, CardType.DEFENSE, "MELEE", dmg = 12, shd = 15, desc = "Deal 12 damage & gain +15 Shield.", skill = OsrsSkill.DEFENCE, lvl = 1),
        c("def_5", "Bronze Guard", "🛡️", 1, CardType.DEFENSE, "ALL", shd = 18, desc = "Raise bronze shield to absorb +18 incoming damage.", skill = OsrsSkill.DEFENCE, lvl = 5),
        c("def_10", "Wooden Deflector", "🪵", 1, CardType.DEFENSE, "ALL", shd = 22, desc = "Block strikes gaining +22 Shield Block.", skill = OsrsSkill.DEFENCE, lvl = 10),
        c("def_15", "Iron Parry", "🧱", 1, CardType.DEFENSE, "ALL", shd = 28, desc = "Raise shield to absorb +28 incoming damage.", skill = OsrsSkill.DEFENCE, lvl = 15),
        c("def_20", "Steel Wall", "🧱", 2, CardType.DEFENSE, "ALL", shd = 35, desc = "Impenetrable steel barrier granting +35 Shield.", skill = OsrsSkill.DEFENCE, lvl = 20),
        c("def_25", "Turtle Stance", "🐢", 2, CardType.DEFENSE, "ALL", shd = 42, desc = "Hunker down gaining +42 Shield Block.", skill = OsrsSkill.DEFENCE, lvl = 25),
        c("def_30", "Mithril Aegis", "🛡️✨", 2, CardType.DEFENSE, "ALL", shd = 48, desc = "Forged mithril ward granting +48 Shield.", skill = OsrsSkill.DEFENCE, lvl = 30),
        c("def_35", "Reflective Guard", "🪞", 2, CardType.DEFENSE, "ALL", shd = 54, desc = "Mirror incoming hits gaining +54 Shield.", skill = OsrsSkill.DEFENCE, lvl = 35),
        c("def_40", "Adamant Bulwark", "🏰", 2, CardType.DEFENSE, "ALL", shd = 62, desc = "Fortified adamant defense granting +62 Shield.", skill = OsrsSkill.DEFENCE, lvl = 40),
        c("def_50", "Fortress Wall", "🏰✨", 2, CardType.DEFENSE, "ALL", shd = 72, desc = "Fortress defense granting +72 Shield Block.", skill = OsrsSkill.DEFENCE, lvl = 50),
        c("def_60", "Dragon Plate Barrier", "🐉🛡️", 2, CardType.DEFENSE, "ALL", shd = 85, desc = "Dragon plate barrier granting +85 Shield.", skill = OsrsSkill.DEFENCE, lvl = 60),
        c("def_70", "Barrows Torag Block", "💀🛡️", 3, CardType.DEFENSE, "ALL", shd = 98, desc = "Ancient Torag armor granting +98 Shield.", skill = OsrsSkill.DEFENCE, lvl = 70),
        c("def_75", "Spirit Shield Ward", "👻", 3, CardType.DEFENSE, "ALL", shd = 110, desc = "Ethereal spirit shield granting +110 Shield.", skill = OsrsSkill.DEFENCE, lvl = 75),
        c("def_80", "Justiciar Bastion", "👑🛡️", 3, CardType.DEFENSE, "ALL", shd = 125, desc = "Holy Justiciar armor granting +125 Shield.", skill = OsrsSkill.DEFENCE, lvl = 80),
        c("def_85", "Elysian Divine Shield", "✨🛡️", 3, CardType.DEFENSE, "ALL", shd = 140, desc = "Elysian absorption granting +140 Shield.", skill = OsrsSkill.DEFENCE, lvl = 85),
        c("def_90", "Obsidian Citadel", "🌋🛡️", 3, CardType.DEFENSE, "ALL", shd = 158, desc = "Obsidian citadel granting +158 Shield.", skill = OsrsSkill.DEFENCE, lvl = 90),
        c("def_99", "Immortal Wall", "💎🛡️", 3, CardType.DEFENSE, "ALL", shd = 180, desc = "Invincible fortress granting +180 Shield!", skill = OsrsSkill.DEFENCE, lvl = 99)
    )

    val RANGED_CARDS = listOf(
        c("rng_1", "Precision Arrow", "🏹", 1, CardType.ATTACK, "RANGED", dmg = 20, desc = "Accurate shot dealing 20 Ranged damage.", skill = OsrsSkill.RANGED, lvl = 1),
        c("rng_5", "Shortbow Snap", "🏹", 1, CardType.ATTACK, "RANGED", dmg = 24, desc = "Quick snap dealing 24 Ranged damage.", skill = OsrsSkill.RANGED, lvl = 5),
        c("rng_10", "Oak Piercer", "🎯", 1, CardType.ATTACK, "RANGED", dmg = 28, desc = "Oak arrow dealing 28 Ranged damage.", skill = OsrsSkill.RANGED, lvl = 10),
        c("rng_15", "Venom Dart", "🎯", 1, CardType.ATTACK, "RANGED", dmg = 18, buff = 12, desc = "Deal 18 damage & buff next attack +12 Dmg.", skill = OsrsSkill.RANGED, lvl = 15),
        c("rng_20", "Willow Shot", "🏹", 2, CardType.ATTACK, "RANGED", dmg = 36, desc = "Willow arrow dealing 36 Ranged damage.", skill = OsrsSkill.RANGED, lvl = 20),
        c("rng_25", "Crossbow Bolt", "⚙️", 2, CardType.ATTACK, "RANGED", dmg = 42, desc = "Heavy bolt dealing 42 Ranged damage.", skill = OsrsSkill.RANGED, lvl = 25),
        c("rng_30", "Maple Snipe", "🎯", 2, CardType.ATTACK, "RANGED", dmg = 48, desc = "Maple bow snipe dealing 48 Ranged damage.", skill = OsrsSkill.RANGED, lvl = 30),
        c("rng_35", "Double Shot", "🏹🏹", 2, CardType.ATTACK, "RANGED", dmg = 55, desc = "Two rapid arrows dealing 55 total damage.", skill = OsrsSkill.RANGED, lvl = 35),
        c("rng_40", "Yew Longshot", "🎯", 2, CardType.ATTACK, "RANGED", dmg = 62, desc = "Long range shot dealing 62 Ranged damage.", skill = OsrsSkill.RANGED, lvl = 40),
        c("rng_50", "Magic Bow Volley", "✨🏹", 2, CardType.ATTACK, "RANGED", dmg = 74, desc = "Enchanted volley dealing 74 Ranged damage.", skill = OsrsSkill.RANGED, lvl = 50),
        c("rng_60", "Dark Bow Volley", "🏹💥", 3, CardType.ULTIMATE, "RANGED", dmg = 88, desc = "Dark bow double dragon volley dealing 88 damage!", skill = OsrsSkill.RANGED, lvl = 60),
        c("rng_70", "Headshot", "🎯✨", 3, CardType.ULTIMATE, "RANGED", dmg = 102, desc = "Lethal aim dealing 102 Ranged damage!", skill = OsrsSkill.RANGED, lvl = 70),
        c("rng_75", "Toxic Blowpipe Spray", "🐍", 3, CardType.ULTIMATE, "RANGED", dmg = 115, desc = "Toxic dart spray dealing 115 Ranged damage!", skill = OsrsSkill.RANGED, lvl = 75),
        c("rng_80", "Twisted Bow Piercer", "🏹✨", 3, CardType.ULTIMATE, "RANGED", dmg = 130, desc = "Twisted bow strike dealing 130 Ranged damage!", skill = OsrsSkill.RANGED, lvl = 80),
        c("rng_85", "Dragon Bolt Envenom", "🐉🎯", 3, CardType.ULTIMATE, "RANGED", dmg = 145, desc = "Dragon dragonstone bolt dealing 145 damage!", skill = OsrsSkill.RANGED, lvl = 85),
        c("rng_90", "Celestial Arrow", "🌌", 3, CardType.ULTIMATE, "RANGED", dmg = 160, desc = "Cosmic arrow dealing 160 Ranged damage!", skill = OsrsSkill.RANGED, lvl = 90),
        c("rng_99", "Master Marksman", "👑🎯", 3, CardType.ULTIMATE, "RANGED", dmg = 180, desc = "Perfect sniper shot dealing 180 lethal damage!", skill = OsrsSkill.RANGED, lvl = 99)
    )

    val MAGIC_CARDS = listOf(
        c("mag_1", "Fireball", "🔥", 1, CardType.MAGIC, "MAGIC", dmg = 22, desc = "Hurl a fireball dealing 22 Magic damage.", skill = OsrsSkill.MAGIC, lvl = 1),
        c("mag_5", "Wind Strike", "💨", 1, CardType.MAGIC, "MAGIC", dmg = 25, desc = "Gust of wind dealing 25 Magic damage.", skill = OsrsSkill.MAGIC, lvl = 5),
        c("mag_10", "Water Bolt", "💧", 1, CardType.MAGIC, "MAGIC", dmg = 30, desc = "Water surge dealing 30 Magic damage.", skill = OsrsSkill.MAGIC, lvl = 10),
        c("mag_15", "Earth Blast", "🪨", 1, CardType.MAGIC, "MAGIC", dmg = 36, desc = "Earth blast dealing 36 Magic damage.", skill = OsrsSkill.MAGIC, lvl = 15),
        c("mag_20", "Ice Spike", "❄️", 1, CardType.MAGIC, "MAGIC", dmg = 22, shd = 18, desc = "Deal 22 Ice damage & gain +18 Frost Shield.", skill = OsrsSkill.MAGIC, lvl = 20),
        c("mag_25", "Fire Burst", "🔥💥", 2, CardType.MAGIC, "MAGIC", dmg = 45, desc = "Fire explosion dealing 45 Magic damage.", skill = OsrsSkill.MAGIC, lvl = 25),
        c("mag_30", "Air Wave", "🌀", 2, CardType.MAGIC, "MAGIC", dmg = 52, desc = "Whirlwind dealing 52 Magic damage.", skill = OsrsSkill.MAGIC, lvl = 30),
        c("mag_40", "Blood Blitz", "🩸", 2, CardType.MAGIC, "MAGIC", dmg = 42, heal = 22, desc = "Deal 42 Blood damage & heal +22 HP.", skill = OsrsSkill.MAGIC, lvl = 40),
        c("mag_45", "Lightning Bolt", "⚡", 2, CardType.MAGIC, "MAGIC", dmg = 62, desc = "Strike lightning dealing 62 Magic damage.", skill = OsrsSkill.MAGIC, lvl = 45),
        c("mag_50", "Ice Burst", "❄️💥", 2, CardType.MAGIC, "MAGIC", dmg = 70, desc = "Freezing ice burst dealing 70 Magic damage.", skill = OsrsSkill.MAGIC, lvl = 50),
        c("mag_60", "Fire Wave", "🔥🌊", 2, CardType.MAGIC, "MAGIC", dmg = 82, desc = "Raging fire wave dealing 82 Magic damage.", skill = OsrsSkill.MAGIC, lvl = 60),
        c("mag_70", "Ice Barrage", "🧊", 3, CardType.ULTIMATE, "MAGIC", dmg = 96, desc = "Ice barrage dealing 96 freezing Magic damage!", skill = OsrsSkill.MAGIC, lvl = 70),
        c("mag_75", "Meteor Swarm", "☄️", 3, CardType.ULTIMATE, "MAGIC", dmg = 110, desc = "Rain down meteors dealing 110 Magic damage!", skill = OsrsSkill.MAGIC, lvl = 75),
        c("mag_80", "Trident Surge", "🔱", 3, CardType.ULTIMATE, "MAGIC", dmg = 125, desc = "Trident of the seas dealing 125 Magic damage!", skill = OsrsSkill.MAGIC, lvl = 80),
        c("mag_85", "Sanguinesti Blast", "🩸✨", 3, CardType.ULTIMATE, "MAGIC", dmg = 140, heal = 25, desc = "Vampiric blast dealing 140 damage & +25 Heal!", skill = OsrsSkill.MAGIC, lvl = 85),
        c("mag_90", "Tumeken's Shadow", "🔮✨", 3, CardType.ULTIMATE, "MAGIC", dmg = 158, desc = "Tumeken's shadow beam dealing 158 Magic damage!", skill = OsrsSkill.MAGIC, lvl = 90),
        c("mag_99", "Archmage Disintegration", "⚡☄️", 3, CardType.ULTIMATE, "MAGIC", dmg = 180, desc = "Archmage disintegration dealing 180 lethal damage!", skill = OsrsSkill.MAGIC, lvl = 99)
    )

    val HITPOINTS_CARDS = listOf(
        c("hp_1", "Second Wind", "❤️", 1, CardType.CONSUMABLE, "ALL", heal = 25, desc = "Restore +25 HP to companion pet.", skill = OsrsSkill.HITPOINTS, lvl = 1),
        c("hp_5", "Vital Breath", "💓", 1, CardType.CONSUMABLE, "ALL", heal = 30, desc = "Deep breath restoring +30 HP.", skill = OsrsSkill.HITPOINTS, lvl = 5),
        c("hp_10", "Organic Essence", "🌱", 1, CardType.CONSUMABLE, "ALL", heal = 36, desc = "Organic energy restoring +36 HP.", skill = OsrsSkill.HITPOINTS, lvl = 10),
        c("hp_15", "Cellular Renewal", "🩺", 1, CardType.CONSUMABLE, "ALL", heal = 42, desc = "Cellular regeneration restoring +42 HP.", skill = OsrsSkill.HITPOINTS, lvl = 15),
        c("hp_20", "Heartbeat Pulse", "💖", 2, CardType.CONSUMABLE, "ALL", heal = 52, desc = "Rhythmic pulse restoring +52 HP.", skill = OsrsSkill.HITPOINTS, lvl = 20),
        c("hp_25", "Blood Warmth", "🩸", 2, CardType.CONSUMABLE, "ALL", heal = 60, desc = "Warm blood surge restoring +60 HP.", skill = OsrsSkill.HITPOINTS, lvl = 25),
        c("hp_30", "Vital Surge", "💖", 2, CardType.CONSUMABLE, "ALL", heal = 70, desc = "Powerful surge restoring +70 HP.", skill = OsrsSkill.HITPOINTS, lvl = 30),
        c("hp_35", "Stamina Fountain", "⛲", 2, CardType.CONSUMABLE, "ALL", heal = 80, desc = "Stamina refresh restoring +80 HP.", skill = OsrsSkill.HITPOINTS, lvl = 35),
        c("hp_40", "Invigorating Aura", "✨", 2, CardType.CONSUMABLE, "ALL", heal = 92, desc = "Invigorating magic restoring +92 HP.", skill = OsrsSkill.HITPOINTS, lvl = 40),
        c("hp_50", "Regeneration Surge", "💫", 2, CardType.CONSUMABLE, "ALL", heal = 106, desc = "Regeneration wave restoring +106 HP.", skill = OsrsSkill.HITPOINTS, lvl = 50),
        c("hp_60", "Titan Heart", "❤️🔥", 3, CardType.CONSUMABLE, "ALL", heal = 122, shd = 20, desc = "Titan heart restoring +122 HP & +20 Shield.", skill = OsrsSkill.HITPOINTS, lvl = 60),
        c("hp_70", "Phoenix Rebirth", "🔥❤️", 3, CardType.CONSUMABLE, "ALL", heal = 138, shd = 30, desc = "Phoenix rebirth healing +138 HP & +30 Shield!", skill = OsrsSkill.HITPOINTS, lvl = 70),
        c("hp_75", "Life Cascade", "🌊❤️", 3, CardType.CONSUMABLE, "ALL", heal = 152, desc = "Cascade of life force restoring +152 HP!", skill = OsrsSkill.HITPOINTS, lvl = 75),
        c("hp_80", "Elixir of Immortality", "🧪❤️", 3, CardType.CONSUMABLE, "ALL", heal = 168, desc = "Immortal elixir restoring +168 HP!", skill = OsrsSkill.HITPOINTS, lvl = 80),
        c("hp_85", "Sacred Blood Restoration", "🩸✨", 3, CardType.CONSUMABLE, "ALL", heal = 185, desc = "Sacred blood healing +185 HP!", skill = OsrsSkill.HITPOINTS, lvl = 85),
        c("hp_90", "Divine Vitality", "🌟❤️", 3, CardType.CONSUMABLE, "ALL", heal = 205, desc = "Divine vitality healing +205 HP!", skill = OsrsSkill.HITPOINTS, lvl = 90),
        c("hp_99", "Eternal Life Bloom", "👑🌸", 3, CardType.CONSUMABLE, "ALL", heal = 235, shd = 50, desc = "Eternal life bloom healing +235 HP & +50 Shield!", skill = OsrsSkill.HITPOINTS, lvl = 99)
    )

    val BLESSING_CARDS = listOf(
        c("pry_1", "Spirit Guard", "✨", 1, CardType.DEFENSE, "ALL", shd = 22, desc = "Bolster defenses for +22 Shield Block.", skill = OsrsSkill.MAGIC, lvl = 1),
        c("pry_5", "Thick Skin", "🛡️", 1, CardType.DEFENSE, "ALL", shd = 26, desc = "Harden skin for +26 Shield Block.", skill = OsrsSkill.MAGIC, lvl = 5),
        c("pry_10", "Burst of Strength", "💪", 1, CardType.ATTACK, "ALL", dmg = 28, desc = "Holy strength strike dealing 28 damage.", skill = OsrsSkill.MAGIC, lvl = 10),
        c("pry_15", "Clarity of Thought", "🧠", 1, CardType.DEFENSE, "ALL", shd = 34, desc = "Focus mind gaining +34 Shield Block.", skill = OsrsSkill.MAGIC, lvl = 15),
        c("pry_20", "Rock Skin", "🪨", 1, CardType.DEFENSE, "ALL", shd = 40, desc = "Rock armor gaining +40 Shield Block.", skill = OsrsSkill.MAGIC, lvl = 20),
        c("pry_25", "Divine Blessing", "🙏", 1, CardType.CONSUMABLE, "ALL", shd = 25, heal = 20, desc = "Heal +20 HP & gain +25 Shield Block.", skill = OsrsSkill.MAGIC, lvl = 25),
        c("pry_30", "Superhuman Strength", "💥", 2, CardType.ATTACK, "ALL", dmg = 48, desc = "Blessed strike dealing 48 damage.", skill = OsrsSkill.MAGIC, lvl = 30),
        c("pry_40", "Protect from Melee", "🛡️✨", 2, CardType.DEFENSE, "ALL", shd = 64, desc = "Holy protection granting +64 Shield.", skill = OsrsSkill.MAGIC, lvl = 40),
        c("pry_50", "Retribution Smite", "⚡", 2, CardType.ATTACK, "ALL", dmg = 62, desc = "Holy retribution strike dealing 62 damage.", skill = OsrsSkill.MAGIC, lvl = 50),
        c("pry_60", "Smite Strike", "⚡✨", 2, CardType.ATTACK, "ALL", dmg = 72, shd = 20, desc = "Smite strike dealing 72 damage & +20 Shield.", skill = OsrsSkill.MAGIC, lvl = 60),
        c("pry_70", "Chivalry Blessing", "⚔️🙏", 2, CardType.BUFF, "ALL", shd = 82, buff = 25, desc = "Chivalry aura +82 Shield & +25 Next Atk.", skill = OsrsSkill.MAGIC, lvl = 70),
        c("pry_77", "Rigour Aim", "🏹✨", 3, CardType.ATTACK, "RANGED", dmg = 98, desc = "Rigour blessed shot dealing 98 Ranged damage!", skill = OsrsSkill.MAGIC, lvl = 77),
        c("pry_80", "Augury Shield", "🔮🛡️", 3, CardType.DEFENSE, "ALL", shd = 115, desc = "Augury holy ward granting +115 Shield.", skill = OsrsSkill.MAGIC, lvl = 80),
        c("pry_85", "Piety Holy Wrath", "⚔️⚡", 3, CardType.ATTACK, "ALL", dmg = 128, desc = "Piety holy wrath dealing 128 damage!", skill = OsrsSkill.MAGIC, lvl = 85),
        c("pry_90", "Seren Radiance", "🌟", 3, CardType.CONSUMABLE, "ALL", shd = 142, heal = 45, desc = "Seren radiance +142 Shield & +45 Heal!", skill = OsrsSkill.MAGIC, lvl = 90),
        c("pry_95", "Holy Judgment", "⚡🙏", 3, CardType.ULTIMATE, "ALL", dmg = 162, desc = "Holy divine judgment dealing 162 damage!", skill = OsrsSkill.MAGIC, lvl = 95),
        c("pry_99", "Saradomon Invocation", "👑✨", 3, CardType.ULTIMATE, "ALL", shd = 185, heal = 60, desc = "Saradomin grace +185 Shield & +60 Heal!", skill = OsrsSkill.MAGIC, lvl = 99)
    )

    private fun generateHarvestingCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val shdVal = 12 + idx * 4
            val buffVal = 10 + idx * 5
            val dmgVal = 8 + idx * 4
            c(
                id = "harvesting_$lvl",
                title = if (idx < 5) "Timber Strike Lv $lvl" else if (idx < 10) "Briar Thorn Barrier Lv $lvl" else "Ancient Redwood Guard Lv $lvl",
                emoji = "🪓",
                cost = 0,
                type = CardType.BUFF,
                dmg = dmgVal,
                shd = shdVal,
                buff = buffVal,
                desc = "0-Cost Harvesting: Deals $dmgVal Nature Dmg, +$shdVal Timber Shield & +$buffVal Next Nature Damage!",
                skill = OsrsSkill.WOODCUTTING,
                lvl = lvl
            )
        }
    }

    private fun generateGemologyCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val shdVal = 15 + idx * 5
            val buffVal = 12 + idx * 6
            c(
                id = "gemology_$lvl",
                title = if (idx < 5) "Sapphire Carapace Lv $lvl" else if (idx < 10) "Ruby Core Armor Lv $lvl" else "Diamond Hardness Lv $lvl",
                emoji = "💎",
                cost = 0,
                type = CardType.BUFF,
                shd = shdVal,
                buff = buffVal,
                desc = "0-Cost Gemology: Hardens crystal armor for +$shdVal Gem Shield & +$buffVal Attack Damage!",
                skill = OsrsSkill.SMITHING,
                lvl = lvl
            )
        }
    }

    private fun generateForgingCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val shdVal = 10 + idx * 3
            val buffVal = 15 + idx * 7
            c(
                id = "forging_$lvl",
                title = if (idx < 5) "Flame Tempering Lv $lvl" else if (idx < 10) "Molten Blade Edge Lv $lvl" else "Infernal Forge Blast Lv $lvl",
                emoji = "⚒️",
                cost = 0,
                type = CardType.BUFF,
                shd = shdVal,
                buff = buffVal,
                desc = "0-Cost Forging: Heat tempers your weapons for +$shdVal Forge Shield & +$buffVal Bonus Fire Damage!",
                skill = OsrsSkill.SMITHING,
                lvl = lvl
            )
        }
    }

    private fun generateFishingCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val shdVal = 12 + idx * 4
            val buffVal = 14 + idx * 6
            c(
                id = "fishing_$lvl",
                title = if (idx < 5) "Fresh Catch Splash Lv $lvl" else if (idx < 10) "Tidal Wave Catch Lv $lvl" else "Oceanic Current Surge Lv $lvl",
                emoji = "🎣",
                cost = 0,
                type = CardType.BUFF,
                shd = shdVal,
                buff = buffVal,
                desc = "0-Cost Fishing: Harnesses aquatic tides for +$shdVal Shield & +$buffVal Bonus Water Damage!",
                skill = OsrsSkill.FISHING,
                lvl = lvl
            )
        }
    }

    private fun generateCookingCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val healVal = 15 + idx * 6
            val buffVal = 10 + idx * 5
            c(
                id = "cooking_$lvl",
                title = if (idx < 5) "Gourmet Meal Ration Lv $lvl" else if (idx < 10) "Hearty Stew Feast Lv $lvl" else "Chef's Master Banquet Lv $lvl",
                emoji = "🍳",
                cost = 0,
                type = CardType.CONSUMABLE,
                heal = healVal,
                buff = buffVal,
                desc = "0-Cost Cooking: Hearty meal restoring +$healVal HP & boosting food potency +$buffVal Next Atk!",
                skill = OsrsSkill.COOKING,
                lvl = lvl
            )
        }
    }

    private fun generateSummoningCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        val names = listOf("Spirit Wolf", "Dreadfowl", "Spirit Spider", "Spirit Terrorbird", "War Tortoise", "Pack Yak", "Steel Titan")
        return levels.mapIndexed { idx, lvl ->
            val companionName = names.getOrElse(idx / 3) { "Steel Titan" }
            val dmgVal = 12 + idx * 6
            val shdVal = 10 + idx * 5
            val healVal = if (idx % 2 == 0) 10 + idx * 3 else 0
            c(
                id = "summoning_$lvl",
                title = "Summon $companionName Lv $lvl",
                emoji = "🐺",
                cost = 0,
                type = CardType.BUFF,
                dmg = dmgVal,
                shd = shdVal,
                heal = healVal,
                desc = "0-Cost Summoning: Summons $companionName ally to assist in battle! Deals $dmgVal Dmg, +$shdVal Shield" + if (healVal > 0) " & +$healVal Heal!" else "!",
                skill = OsrsSkill.FIREMAKING,
                lvl = lvl
            )
        }
    }

    private fun generateHerbalismCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val healVal = 20 + idx * 8
            val buffVal = 12 + idx * 5
            c(
                id = "herbalism_$lvl",
                title = if (idx < 5) "Potent Herbal Brew Lv $lvl" else if (idx < 10) "Super Combat Elixir Lv $lvl" else "Saradomin Brew Tonic Lv $lvl",
                emoji = "🧪",
                cost = 0,
                type = CardType.CONSUMABLE,
                heal = healVal,
                buff = buffVal,
                desc = "0-Cost Herbalism: Brews a potent battle elixir for massive +$healVal HP Heal & +$buffVal Potion Buff!",
                skill = OsrsSkill.HERBLORE,
                lvl = lvl
            )
        }
    }

    private fun generateAgricultureCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val shdVal = 14 + idx * 5
            val buffVal = 10 + idx * 5
            val dmgVal = 10 + idx * 4
            c(
                id = "agriculture_$lvl",
                title = if (idx < 5) "Leeching Vines Lv $lvl" else if (idx < 10) "Crop Rot Debuff Lv $lvl" else "Weed Wither Entangle Lv $lvl",
                emoji = "🌱",
                cost = 0,
                type = CardType.BUFF,
                dmg = dmgVal,
                shd = shdVal,
                buff = buffVal,
                desc = "0-Cost Agriculture: Sprouts entangling vines dealing $dmgVal Dmg, weakening enemy power & +$shdVal Shield!",
                skill = OsrsSkill.FARMING,
                lvl = lvl
            )
        }
    }

    private fun generateBeastTrackingCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val dmgVal = 15 + idx * 6
            val shdVal = 12 + idx * 4
            c(
                id = "beasttracking_$lvl",
                title = if (idx < 5) "Snare Trap Lv $lvl" else if (idx < 10) "Pitfall Trap Lv $lvl" else "Steel Bear Trap Lv $lvl",
                emoji = "🐾",
                cost = 0,
                type = CardType.ATTACK,
                dmg = dmgVal,
                shd = shdVal,
                desc = "0-Cost Beast Tracking: Lays a trap dealing $dmgVal DoT Trap Damage & absorbing +$shdVal incoming hits!",
                skill = OsrsSkill.HUNTER,
                lvl = lvl
            )
        }
    }

    private fun generateBountyCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val dmgVal = 18 + idx * 7
            val buffVal = 12 + idx * 5
            c(
                id = "bounty_$lvl",
                title = if (idx < 5) "Monster Lore Lv $lvl" else if (idx < 10) "Bounty Stance Lv $lvl" else "Trophy Slayer Strike Lv $lvl",
                emoji = "💀",
                cost = 0,
                type = CardType.ATTACK,
                dmg = dmgVal,
                buff = buffVal,
                desc = "0-Cost Bounty: Exploits monster weakness for $dmgVal Slayer Dmg & +$buffVal Bounty Next Atk!",
                skill = OsrsSkill.SLAYER,
                lvl = lvl
            )
        }
    }

    private fun generateDexterityCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val shdVal = 18 + idx * 6
            val buffVal = 8 + idx * 4
            c(
                id = "dexterity_$lvl",
                title = if (idx < 5) "Nimble Step Lv $lvl" else if (idx < 10) "Sidestep Dodge Lv $lvl" else "Graceful Roll Lv $lvl",
                emoji = "🏃",
                cost = 0,
                type = CardType.DEFENSE,
                shd = shdVal,
                buff = buffVal,
                desc = "0-Cost Dexterity: Evasive dodge granting +$shdVal Evasion Shield Block & +$buffVal Speed Buff!",
                skill = OsrsSkill.AGILITY,
                lvl = lvl
            )
        }
    }

    private fun generateTrickeryCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val shdVal = 15 + idx * 5
            val buffVal = 12 + idx * 6
            c(
                id = "trickery_$lvl",
                title = if (idx < 5) "Smoke Bomb Blur Lv $lvl" else if (idx < 10) "Pocket Sand Blind Lv $lvl" else "Shadow Step Trick Lv $lvl",
                emoji = "🥷",
                cost = 0,
                type = CardType.DEFENSE,
                shd = shdVal,
                buff = buffVal,
                desc = "0-Cost Trickery: Lowers enemy accuracy with smoke bombs for +$shdVal Shield & +$buffVal Cheap Shot!",
                skill = OsrsSkill.THIEVING,
                lvl = lvl
            )
        }
    }

    private fun generateRunemakingCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val shdVal = 10 + idx * 4
            val buffVal = 16 + idx * 7
            c(
                id = "runemaking_$lvl",
                title = if (idx < 5) "Runic Channel Lv $lvl" else if (idx < 10) "Elemental Rune Surge Lv $lvl" else "Wrath Rune Infusion Lv $lvl",
                emoji = "🔮",
                cost = 0,
                type = CardType.BUFF,
                shd = shdVal,
                buff = buffVal,
                desc = "0-Cost Runemaking: Reduces spell energy cost & boosts magic power for +$shdVal Shield & +$buffVal Spell Dmg!",
                skill = OsrsSkill.RUNECRAFT,
                lvl = lvl
            )
        }
    }

    private fun generateWhittlingCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val dmgVal = 12 + idx * 5
            val buffVal = 14 + idx * 6
            val shdVal = 8 + idx * 3
            c(
                id = "whittling_$lvl",
                title = if (idx < 5) "Arrow Recycling Lv $lvl" else if (idx < 10) "Bowstring Tension Lv $lvl" else "Master Whittled Volley Lv $lvl",
                emoji = "🎯",
                cost = 0,
                type = CardType.ATTACK,
                dmg = dmgVal,
                shd = shdVal,
                buff = buffVal,
                desc = "0-Cost Whittling: Conserves arrows and crafts precision shafts for $dmgVal Bow Dmg, +$shdVal Shield & +$buffVal Next Shot!",
                skill = OsrsSkill.FLETCHING,
                lvl = lvl
            )
        }
    }

    private fun generateHutKeepingCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val shdVal = 20 + idx * 7
            val healVal = 10 + idx * 4
            c(
                id = "hutkeeping_$lvl",
                title = if (idx < 5) "Wooden Construct Lv $lvl" else if (idx < 10) "Oak Fortification Lv $lvl" else "Mahogany Construct Lv $lvl",
                emoji = "🛠️",
                cost = 0,
                type = CardType.DEFENSE,
                shd = shdVal,
                heal = healVal,
                desc = "0-Cost Hut-Keeping: Builds protective sanctuary constructs granting +$shdVal Construct Shield & +$healVal HP Regen!",
                skill = OsrsSkill.CONSTRUCTION,
                lvl = lvl
            )
        }
    }

    private fun generateNavigationCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val dmgVal = 20 + idx * 8
            val buffVal = 10 + idx * 5
            c(
                id = "navigation_$lvl",
                title = if (idx < 5) "Cannonball Piercer Lv $lvl" else if (idx < 10) "Anchor Crush Lv $lvl" else "Broadside Volley Lv $lvl",
                emoji = "⛵",
                cost = 0,
                type = CardType.ATTACK,
                dmg = dmgVal,
                buff = buffVal,
                desc = "0-Cost Navigation: Fires armor-piercing cannon shots dealing $dmgVal Piercing Dmg that bypasses enemy armor!",
                skill = OsrsSkill.SAILING,
                lvl = lvl
            )
        }
    }

    private fun generateDivinationCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val shdVal = 14 + idx * 5
            val buffVal = 12 + idx * 5
            c(
                id = "divination_$lvl",
                title = if (idx < 5) "Spirit Telepathy Lv $lvl" else if (idx < 10) "Cosmic Resonance Lv $lvl" else "Memory Weave Pulse Lv $lvl",
                emoji = "📱",
                cost = 0,
                type = CardType.BUFF,
                shd = shdVal,
                buff = buffVal,
                desc = "0-Cost Divination: Channels telepathic spirit energy for +$shdVal Spirit Shield & +$buffVal Mind Buff!",
                skill = OsrsSkill.DIVINATION,
                lvl = lvl
            )
        }
    }

    private fun generateAdventuringCards(): List<CombatCard> {
        val levels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 75, 80, 85, 90, 99)
        return levels.mapIndexed { idx, lvl ->
            val dmgVal = 16 + idx * 6
            val shdVal = 14 + idx * 5
            c(
                id = "adventuring_$lvl",
                title = if (idx < 5) "Relic Discovery Lv $lvl" else if (idx < 10) "Dungeon Relic Surge Lv $lvl" else "Dragon Relic Artifact Lv $lvl",
                emoji = "🗺️",
                cost = 0,
                type = CardType.ATTACK,
                dmg = dmgVal,
                shd = shdVal,
                desc = "0-Cost Adventuring: Unearths ancient dungeon relics dealing $dmgVal Relic Dmg & +$shdVal Foresight Shield!",
                skill = OsrsSkill.ADVENTURING,
                lvl = lvl
            )
        }
    }

    val WOODCUTTING_CARDS = generateHarvestingCards()
    val MINING_CARDS = generateGemologyCards()
    val SMITHING_CARDS = generateForgingCards()
    val FISHING_CARDS = generateFishingCards()
    val COOKING_CARDS = generateCookingCards()
    val FIREMAKING_CARDS = generateSummoningCards()
    val HERBLORE_CARDS = generateHerbalismCards()
    val FARMING_CARDS = generateAgricultureCards()
    val HUNTER_CARDS = generateBeastTrackingCards()
    val SLAYER_CARDS = generateBountyCards()
    val AGILITY_CARDS = generateDexterityCards()
    val THIEVING_CARDS = generateTrickeryCards()
    val RUNECRAFT_CARDS = generateRunemakingCards()
    val FLETCHING_CARDS = generateWhittlingCards()
    val CONSTRUCTION_CARDS = generateHutKeepingCards()
    val SAILING_CARDS = generateNavigationCards()
    val DIVINATION_CARDS = generateDivinationCards()
    val ADVENTURING_CARDS = generateAdventuringCards()

    val ALL_CARDS: List<CombatCard> = (
        ATTACK_CARDS + DEFENCE_CARDS + RANGED_CARDS + MAGIC_CARDS +
        HITPOINTS_CARDS + BLESSING_CARDS + WOODCUTTING_CARDS + MINING_CARDS +
        SMITHING_CARDS + FISHING_CARDS + COOKING_CARDS + FIREMAKING_CARDS +
        HERBLORE_CARDS + FARMING_CARDS + HUNTER_CARDS + SLAYER_CARDS +
        AGILITY_CARDS + THIEVING_CARDS + RUNECRAFT_CARDS + FLETCHING_CARDS +
        CONSTRUCTION_CARDS + SAILING_CARDS + DIVINATION_CARDS +
        ADVENTURING_CARDS
    )

    fun getUnlockedCardsForSkillMap(skillXpMap: Map<OsrsSkill, Long>): List<CombatCard> {
        return ALL_CARDS.filter { card ->
            val xp = skillXpMap[card.skill] ?: 0L
            val level = OsrsXpCalculator.getLevelForXp(xp)
            level >= card.reqLevel
        }
    }

    fun getDefaultDeckForStance(stance: String, skillXpMap: Map<OsrsSkill, Long> = emptyMap()): List<CombatCard> {
        val unlocked = if (skillXpMap.isNotEmpty()) getUnlockedCardsForSkillMap(skillXpMap) else ALL_CARDS.filter { it.reqLevel == 1 }
        val pool = if (unlocked.size >= 6) unlocked else ALL_CARDS.filter { it.reqLevel == 1 }
        val stanceFiltered = pool.filter { it.stance == stance || it.stance == "ALL" }
        val deck = mutableListOf<CombatCard>()
        deck.addAll(stanceFiltered)
        if (deck.size < 8) {
            deck.addAll(pool.take(8 - deck.size))
        }
        return deck.shuffled()
    }
}

data class SavedDeckLoadout(
    val id: String,
    val name: String,
    val iconEmoji: String = "🎴",
    val stance: String = "ALL", // "MELEE", "RANGED", "MAGIC", or "ALL"
    val cardIds: List<String> = emptyList(),
    val isPreset: Boolean = false,
    val description: String = ""
)

object ArchetypeDeckPresets {
    val PRESETS = listOf(
        SavedDeckLoadout(
            id = "preset_melee_warrior",
            name = "Melee Berserker",
            iconEmoji = "⚔️",
            stance = "MELEE",
            isPreset = true,
            description = "High physical damage with brutal cleaves, slashes, and defensive parries.",
            cardIds = listOf("atk_1", "atk_5", "atk_10", "atk_15", "atk_20", "def_1", "def_5", "def_15", "hp_1", "pray_1")
        ),
        SavedDeckLoadout(
            id = "preset_ranged_hunter",
            name = "Ranger Sharpshooter",
            iconEmoji = "🏹",
            stance = "RANGED",
            isPreset = true,
            description = "Rapid long-range volleys, poison darts, tracking instincts, and agility evasions.",
            cardIds = listOf("rng_1", "rng_5", "rng_10", "rng_15", "rng_20", "def_5", "agi_1", "hunt_1", "fletch_1", "hp_1")
        ),
        SavedDeckLoadout(
            id = "preset_magic_sorcerer",
            name = "Arcane Sorcerer",
            iconEmoji = "🧙‍♂️",
            stance = "MAGIC",
            isPreset = true,
            description = "Devastating elemental incantations, mystic rune channeling, and arcane shields.",
            cardIds = listOf("mag_1", "mag_5", "mag_10", "mag_15", "mag_20", "rc_1", "def_10", "pray_5", "hp_1", "adv_1")
        ),
        SavedDeckLoadout(
            id = "preset_bastion_tank",
            name = "Immortal Bastion",
            iconEmoji = "🛡️",
            stance = "ALL",
            isPreset = true,
            description = "Heavily fortified armor, massive shield barriers, divine prayers, and counter-attacks.",
            cardIds = listOf("def_1", "def_5", "def_10", "def_15", "def_20", "atk_1", "hp_5", "pray_1", "pray_10", "smith_1")
        ),
        SavedDeckLoadout(
            id = "preset_skiller_alchemist",
            name = "Grandmaster Skiller",
            iconEmoji = "🌿",
            stance = "ALL",
            isPreset = true,
            description = "Herblore concoctions, hearty cooked rations, thieving tricks, and resource surges.",
            cardIds = listOf("herb_1", "cook_1", "thiev_1", "farm_1", "fish_1", "wc_1", "min_1", "atk_1", "def_1", "hp_1")
        )
    )
}


