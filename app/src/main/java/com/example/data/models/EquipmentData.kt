package com.example.data.models

object EquipmentData {

    val ORE_EQUIPMENT_ITEMS: List<InventoryItem> = listOf(
        // ==========================================
        // 1. BRONZE TIER (Beginner / Tier 1)
        // ==========================================
        InventoryItem(
            id = "item_bronze_sword",
            name = "Bronze Sword",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 10,
            defPowerBonus = 2,
            iconEmoji = "⚔️",
            costGp = 50L,
            description = "A basic shortsword forged from bronze ore."
        ),
        InventoryItem(
            id = "item_bronze_scimitar",
            name = "Bronze Scimitar",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 12,
            defPowerBonus = 1,
            iconEmoji = "🗡️",
            costGp = 60L,
            description = "A curved bronze slashing blade for fast strikes."
        ),
        InventoryItem(
            id = "item_bronze_dagger",
            name = "Bronze Dagger",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 8,
            defPowerBonus = 1,
            iconEmoji = "🗡️",
            costGp = 30L,
            description = "A lightweight thrusting dagger made of bronze."
        ),
        InventoryItem(
            id = "item_bronze_axe",
            name = "Bronze Hatchet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AXE,
            combatPowerBonus = 6,
            defPowerBonus = 1,
            iconEmoji = "🪓",
            costGp = 40L,
            description = "A bronze hatchet used for chopping timber and combat."
        ),
        InventoryItem(
            id = "item_bronze_full_helm",
            name = "Bronze Full Helm",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 1,
            defPowerBonus = 8,
            iconEmoji = "🪖",
            costGp = 50L,
            description = "A standard full helmet forged from bronze."
        ),
        InventoryItem(
            id = "item_bronze_platebody",
            name = "Bronze Platebody",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BODY,
            combatPowerBonus = 2,
            defPowerBonus = 16,
            iconEmoji = "🛡️",
            costGp = 120L,
            description = "Heavy bronze chest armor offering solid entry protection."
        ),
        InventoryItem(
            id = "item_bronze_platelegs",
            name = "Bronze Platelegs",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.LEGS,
            combatPowerBonus = 1,
            defPowerBonus = 12,
            iconEmoji = "🦵",
            costGp = 80L,
            description = "Bronze leg armor to protect the lower body."
        ),
        InventoryItem(
            id = "item_bronze_kiteshield",
            name = "Bronze Kiteshield",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 1,
            defPowerBonus = 12,
            iconEmoji = "🛡️",
            costGp = 75L,
            description = "A large bronze shield capable of blocking incoming hits."
        ),
        InventoryItem(
            id = "item_bronze_gauntlets",
            name = "Bronze Gauntlets",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.GLOVES,
            combatPowerBonus = 1,
            defPowerBonus = 6,
            iconEmoji = "🧤",
            costGp = 40L,
            description = "Sturdy bronze gauntlets protecting hands and wrists."
        ),
        InventoryItem(
            id = "item_bronze_boots",
            name = "Bronze Boots",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BOOTS,
            combatPowerBonus = 1,
            defPowerBonus = 6,
            iconEmoji = "👢",
            costGp = 40L,
            description = "Tough bronze boots providing foot protection."
        ),
        InventoryItem(
            id = "item_bronze_cape",
            name = "Bronze Cape",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.CAPE,
            combatPowerBonus = 1,
            defPowerBonus = 4,
            iconEmoji = "🧥",
            costGp = 30L,
            description = "A woven heraldic cape fastened with a bronze clasp."
        ),
        InventoryItem(
            id = "item_bronze_ring",
            name = "Bronze Ring",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.RING,
            combatPowerBonus = 3,
            defPowerBonus = 3,
            iconEmoji = "💍",
            costGp = 30L,
            description = "A simple bronze ring boosting combat and defense."
        ),
        InventoryItem(
            id = "item_bronze_amulet",
            name = "Bronze Amulet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 4,
            defPowerBonus = 4,
            iconEmoji = "📿",
            costGp = 35L,
            description = "A polished bronze pendant granting warrior power."
        ),

        // ==========================================
        // 2. IRON TIER (Tier 15)
        // ==========================================
        InventoryItem(
            id = "item_iron_sword",
            name = "Iron Sword",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 18,
            defPowerBonus = 4,
            iconEmoji = "⚔️",
            costGp = 120L,
            description = "A sturdy iron sword for reliable melee combat."
        ),
        InventoryItem(
            id = "item_iron_scimitar",
            name = "Iron Scimitar",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 22,
            defPowerBonus = 2,
            iconEmoji = "🗡️",
            costGp = 150L,
            description = "A curved iron scimitar with swift slashing speed."
        ),
        InventoryItem(
            id = "item_iron_dagger",
            name = "Iron Dagger",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 14,
            defPowerBonus = 2,
            iconEmoji = "🗡️",
            costGp = 80L,
            description = "A sharp iron dagger for close combat thrusts."
        ),
        InventoryItem(
            id = "item_iron_axe",
            name = "Iron Hatchet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AXE,
            combatPowerBonus = 12,
            defPowerBonus = 2,
            iconEmoji = "🪓",
            costGp = 100L,
            description = "An iron hatchet suitable for logging oak trees."
        ),
        InventoryItem(
            id = "item_iron_full_helm",
            name = "Iron Full Helm",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 2,
            defPowerBonus = 15,
            iconEmoji = "🪖",
            costGp = 120L,
            description = "A solid iron full helmet."
        ),
        InventoryItem(
            id = "item_iron_platebody",
            name = "Iron Platebody",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BODY,
            combatPowerBonus = 3,
            defPowerBonus = 28,
            iconEmoji = "🛡️",
            costGp = 300L,
            description = "Heavy iron chest armor forged for warrior defense."
        ),
        InventoryItem(
            id = "item_iron_platelegs",
            name = "Iron Platelegs",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.LEGS,
            combatPowerBonus = 2,
            defPowerBonus = 20,
            iconEmoji = "🦵",
            costGp = 200L,
            description = "Iron leg armor providing dependable protection."
        ),
        InventoryItem(
            id = "item_iron_kiteshield",
            name = "Iron Kiteshield",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 2,
            defPowerBonus = 20,
            iconEmoji = "🛡️",
            costGp = 180L,
            description = "A sturdy iron shield with superior deflection."
        ),
        InventoryItem(
            id = "item_iron_gauntlets",
            name = "Iron Gauntlets",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.GLOVES,
            combatPowerBonus = 2,
            defPowerBonus = 10,
            iconEmoji = "🧤",
            costGp = 100L,
            description = "Protective iron gauntlets for hand defense."
        ),
        InventoryItem(
            id = "item_iron_boots",
            name = "Iron Boots",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BOOTS,
            combatPowerBonus = 2,
            defPowerBonus = 10,
            iconEmoji = "👢",
            costGp = 100L,
            description = "Reinforced iron boots for rugged footing."
        ),
        InventoryItem(
            id = "item_iron_cape",
            name = "Iron Cape",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.CAPE,
            combatPowerBonus = 2,
            defPowerBonus = 7,
            iconEmoji = "🧥",
            costGp = 80L,
            description = "A sturdy cape pinned with an iron crest."
        ),
        InventoryItem(
            id = "item_iron_ring",
            name = "Iron Ring",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.RING,
            combatPowerBonus = 6,
            defPowerBonus = 5,
            iconEmoji = "💍",
            costGp = 80L,
            description = "A forged iron band granting combat focus."
        ),
        InventoryItem(
            id = "item_iron_amulet",
            name = "Iron Amulet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 7,
            defPowerBonus = 6,
            iconEmoji = "📿",
            costGp = 90L,
            description = "An iron amulet imbued with steady resilience."
        ),

        // ==========================================
        // 3. SILVER TIER (Tier 20)
        // ==========================================
        InventoryItem(
            id = "item_silver_sword",
            name = "Silver Sword",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 26,
            defPowerBonus = 6,
            iconEmoji = "⚔️",
            costGp = 250L,
            description = "A gleaming silver sword deadly against dark fiends."
        ),
        InventoryItem(
            id = "item_silver_scimitar",
            name = "Silver Scimitar",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 30,
            defPowerBonus = 3,
            iconEmoji = "🗡️",
            costGp = 300L,
            description = "A refined silver scimitar that slices with elegance."
        ),
        InventoryItem(
            id = "item_silver_dagger",
            name = "Silver Dagger",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 20,
            defPowerBonus = 3,
            iconEmoji = "🗡️",
            costGp = 160L,
            description = "A purified silver dagger for rapid strikes."
        ),
        InventoryItem(
            id = "item_silver_axe",
            name = "Silver Hatchet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AXE,
            combatPowerBonus = 18,
            defPowerBonus = 3,
            iconEmoji = "🪓",
            costGp = 200L,
            description = "A silver hatchet with a luminous edge."
        ),
        InventoryItem(
            id = "item_silver_full_helm",
            name = "Silver Full Helm",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 3,
            defPowerBonus = 22,
            iconEmoji = "🪖",
            costGp = 250L,
            description = "A polished silver helm repelling negative energies."
        ),
        InventoryItem(
            id = "item_silver_platebody",
            name = "Silver Platebody",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BODY,
            combatPowerBonus = 4,
            defPowerBonus = 40,
            iconEmoji = "🛡️",
            costGp = 600L,
            description = "A luminous silver platebody radiating protective aura."
        ),
        InventoryItem(
            id = "item_silver_platelegs",
            name = "Silver Platelegs",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.LEGS,
            combatPowerBonus = 3,
            defPowerBonus = 28,
            iconEmoji = "🦵",
            costGp = 400L,
            description = "Shining silver greaves protecting against physical trauma."
        ),
        InventoryItem(
            id = "item_silver_kiteshield",
            name = "Silver Kiteshield",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 3,
            defPowerBonus = 28,
            iconEmoji = "🛡️",
            costGp = 380L,
            description = "A mirror-finish silver shield with high resistance."
        ),
        InventoryItem(
            id = "item_silver_gauntlets",
            name = "Silver Gauntlets",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.GLOVES,
            combatPowerBonus = 3,
            defPowerBonus = 14,
            iconEmoji = "🧤",
            costGp = 200L,
            description = "Finely chased silver gauntlets."
        ),
        InventoryItem(
            id = "item_silver_boots",
            name = "Silver Boots",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BOOTS,
            combatPowerBonus = 3,
            defPowerBonus = 14,
            iconEmoji = "👢",
            costGp = 200L,
            description = "Comfortable boots lined with silver studs."
        ),
        InventoryItem(
            id = "item_silver_cape",
            name = "Silver Cape",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.CAPE,
            combatPowerBonus = 3,
            defPowerBonus = 10,
            iconEmoji = "🧥",
            costGp = 180L,
            description = "A silver silk mantle with defensive blessings."
        ),
        InventoryItem(
            id = "item_silver_ring",
            name = "Silver Ring",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.RING,
            combatPowerBonus = 10,
            defPowerBonus = 8,
            iconEmoji = "💍",
            costGp = 180L,
            description = "An enchanted silver ring empowering the wearer."
        ),
        InventoryItem(
            id = "item_silver_amulet",
            name = "Silver Amulet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 11,
            defPowerBonus = 9,
            iconEmoji = "📿",
            costGp = 200L,
            description = "A mystical silver talisman granting clarity and strength."
        ),

        // ==========================================
        // 4. STEEL TIER (Tier 30)
        // ==========================================
        InventoryItem(
            id = "item_steel_sword",
            name = "Steel Sword",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 35,
            defPowerBonus = 8,
            iconEmoji = "⚔️",
            costGp = 500L,
            description = "A heavy tempered steel sword forged for serious battle."
        ),
        InventoryItem(
            id = "item_steel_scimitar",
            name = "Steel Scimitar",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 42,
            defPowerBonus = 4,
            iconEmoji = "🗡️",
            costGp = 600L,
            description = "A razor-sharp steel scimitar."
        ),
        InventoryItem(
            id = "item_steel_dagger",
            name = "Steel Dagger",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 28,
            defPowerBonus = 4,
            iconEmoji = "🗡️",
            costGp = 300L,
            description = "A hardened steel dagger with a wicked tip."
        ),
        InventoryItem(
            id = "item_steel_axe",
            name = "Steel Hatchet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AXE,
            combatPowerBonus = 26,
            defPowerBonus = 4,
            iconEmoji = "🪓",
            costGp = 400L,
            description = "A steel hatchet felling Willow and Pine with ease."
        ),
        InventoryItem(
            id = "item_steel_full_helm",
            name = "Steel Full Helm",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 4,
            defPowerBonus = 30,
            iconEmoji = "🪖",
            costGp = 500L,
            description = "Tempered steel full helm with a visor."
        ),
        InventoryItem(
            id = "item_steel_platebody",
            name = "Steel Platebody",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BODY,
            combatPowerBonus = 5,
            defPowerBonus = 54,
            iconEmoji = "🛡️",
            costGp = 1200L,
            description = "Heavy steel platebody providing high physical protection."
        ),
        InventoryItem(
            id = "item_steel_platelegs",
            name = "Steel Platelegs",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.LEGS,
            combatPowerBonus = 4,
            defPowerBonus = 38,
            iconEmoji = "🦵",
            costGp = 800L,
            description = "Rigid steel leg guards designed to withstand heavy blows."
        ),
        InventoryItem(
            id = "item_steel_kiteshield",
            name = "Steel Kiteshield",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 4,
            defPowerBonus = 38,
            iconEmoji = "🛡️",
            costGp = 750L,
            description = "Heavy steel shield capable of halting mighty strikes."
        ),
        InventoryItem(
            id = "item_steel_gauntlets",
            name = "Steel Gauntlets",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.GLOVES,
            combatPowerBonus = 4,
            defPowerBonus = 18,
            iconEmoji = "🧤",
            costGp = 400L,
            description = "Articulated steel gauntlets."
        ),
        InventoryItem(
            id = "item_steel_boots",
            name = "Steel Boots",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BOOTS,
            combatPowerBonus = 4,
            defPowerBonus = 18,
            iconEmoji = "👢",
            costGp = 400L,
            description = "Heavy steel sabatons protecting feet and ankles."
        ),
        InventoryItem(
            id = "item_steel_cape",
            name = "Steel Cape",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.CAPE,
            combatPowerBonus = 4,
            defPowerBonus = 14,
            iconEmoji = "🧥",
            costGp = 350L,
            description = "A warrior cape weighted with steel rings."
        ),
        InventoryItem(
            id = "item_steel_ring",
            name = "Steel Ring",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.RING,
            combatPowerBonus = 14,
            defPowerBonus = 12,
            iconEmoji = "💍",
            costGp = 350L,
            description = "A heavy steel signet ring bolstering fortitude."
        ),
        InventoryItem(
            id = "item_steel_amulet",
            name = "Steel Amulet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 15,
            defPowerBonus = 13,
            iconEmoji = "📿",
            costGp = 400L,
            description = "A hardened steel medallion radiating battle grit."
        ),

        // ==========================================
        // 5. GOLD TIER (Tier 40)
        // ==========================================
        InventoryItem(
            id = "item_gold_sword",
            name = "Gold Sword",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 46,
            defPowerBonus = 11,
            iconEmoji = "⚔️",
            costGp = 1000L,
            description = "A lavish gilded sword enchanted with solar fury."
        ),
        InventoryItem(
            id = "item_gold_scimitar",
            name = "Gold Scimitar",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 54,
            defPowerBonus = 6,
            iconEmoji = "🗡️",
            costGp = 1200L,
            description = "A dazzling golden scimitar with swift radiant strikes."
        ),
        InventoryItem(
            id = "item_gold_dagger",
            name = "Gold Dagger",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 36,
            defPowerBonus = 6,
            iconEmoji = "🗡️",
            costGp = 600L,
            description = "An ornate gold dagger laced with solar magic."
        ),
        InventoryItem(
            id = "item_gold_axe",
            name = "Gold Hatchet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AXE,
            combatPowerBonus = 34,
            defPowerBonus = 6,
            iconEmoji = "🪓",
            costGp = 800L,
            description = "A golden hatchet possessing surprising keenness."
        ),
        InventoryItem(
            id = "item_gold_full_helm",
            name = "Gold Full Helm",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 5,
            defPowerBonus = 40,
            iconEmoji = "🪖",
            costGp = 1000L,
            description = "A majestic gilded full helm fit for a grand knight."
        ),
        InventoryItem(
            id = "item_gold_platebody",
            name = "Gold Platebody",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BODY,
            combatPowerBonus = 7,
            defPowerBonus = 70,
            iconEmoji = "🛡️",
            costGp = 2500L,
            description = "A gleaming golden platebody providing royal protection."
        ),
        InventoryItem(
            id = "item_gold_platelegs",
            name = "Gold Platelegs",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.LEGS,
            combatPowerBonus = 5,
            defPowerBonus = 50,
            iconEmoji = "🦵",
            costGp = 1600L,
            description = "Golden platelegs shimmering with solar enchantments."
        ),
        InventoryItem(
            id = "item_gold_kiteshield",
            name = "Gold Kiteshield",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 5,
            defPowerBonus = 50,
            iconEmoji = "🛡️",
            costGp = 1500L,
            description = "A lavish golden shield deflecting dark sorceries."
        ),
        InventoryItem(
            id = "item_gold_gauntlets",
            name = "Gold Gauntlets",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.GLOVES,
            combatPowerBonus = 5,
            defPowerBonus = 24,
            iconEmoji = "🧤",
            costGp = 800L,
            description = "Gilded battle gauntlets."
        ),
        InventoryItem(
            id = "item_gold_boots",
            name = "Gold Boots",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BOOTS,
            combatPowerBonus = 5,
            defPowerBonus = 24,
            iconEmoji = "👢",
            costGp = 800L,
            description = "Gilded boots inscribed with protective runes."
        ),
        InventoryItem(
            id = "item_gold_cape",
            name = "Gold Cape",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.CAPE,
            combatPowerBonus = 5,
            defPowerBonus = 18,
            iconEmoji = "🧥",
            costGp = 700L,
            description = "A majestic cloak spun with golden filament."
        ),
        InventoryItem(
            id = "item_gold_ring",
            name = "Gold Ring",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.RING,
            combatPowerBonus = 18,
            defPowerBonus = 16,
            iconEmoji = "💍",
            costGp = 700L,
            description = "A regal golden band radiating warmth and defense."
        ),
        InventoryItem(
            id = "item_gold_amulet",
            name = "Gold Amulet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 20,
            defPowerBonus = 18,
            iconEmoji = "📿",
            costGp = 800L,
            description = "A golden pendant blessing the champion with fortitude."
        ),

        // ==========================================
        // 6. OPALITE (MITHRIL) TIER (Tier 50)
        // ==========================================
        InventoryItem(
            id = "item_mithril_sword",
            name = "Opalite Sword",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 60,
            defPowerBonus = 14,
            iconEmoji = "⚔️",
            costGp = 2000L,
            description = "An ethereal opalite blade lighter than air yet harder than steel."
        ),
        InventoryItem(
            id = "item_mithril_scimitar",
            name = "Opalite Scimitar",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 70,
            defPowerBonus = 8,
            iconEmoji = "🗡️",
            costGp = 2400L,
            description = "A swift opalite scimitar gleaming with cyan luminescence."
        ),
        InventoryItem(
            id = "item_mithril_dagger",
            name = "Opalite Dagger",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 48,
            defPowerBonus = 8,
            iconEmoji = "🗡️",
            costGp = 1200L,
            description = "A piercing opalite stiletto."
        ),
        InventoryItem(
            id = "item_mithril_axe",
            name = "Opalite Hatchet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AXE,
            combatPowerBonus = 44,
            defPowerBonus = 8,
            iconEmoji = "🪓",
            costGp = 1600L,
            description = "A lightweight opalite hatchet for felling Cedar and Maple."
        ),
        InventoryItem(
            id = "item_mithril_full_helm",
            name = "Opalite Full Helm",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 7,
            defPowerBonus = 52,
            iconEmoji = "🪖",
            costGp = 2000L,
            description = "A shimmering cyan opalite full helmet."
        ),
        InventoryItem(
            id = "item_mithril_platebody",
            name = "Opalite Platebody",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BODY,
            combatPowerBonus = 9,
            defPowerBonus = 90,
            iconEmoji = "🛡️",
            costGp = 5000L,
            description = "Mastercrafted opalite armor providing exceptional defense."
        ),
        InventoryItem(
            id = "item_mithril_platelegs",
            name = "Opalite Platelegs",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.LEGS,
            combatPowerBonus = 7,
            defPowerBonus = 64,
            iconEmoji = "🦵",
            costGp = 3200L,
            description = "Reinforced opalite platelegs with unmatched agility."
        ),
        InventoryItem(
            id = "item_mithril_kiteshield",
            name = "Opalite Kiteshield",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 7,
            defPowerBonus = 64,
            iconEmoji = "🛡️",
            costGp = 3000L,
            description = "A resonant opalite shield that absorbs impacts effortlessly."
        ),
        InventoryItem(
            id = "item_mithril_gauntlets",
            name = "Opalite Gauntlets",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.GLOVES,
            combatPowerBonus = 7,
            defPowerBonus = 30,
            iconEmoji = "🧤",
            costGp = 1600L,
            description = "Flexible opalite gauntlets."
        ),
        InventoryItem(
            id = "item_mithril_boots",
            name = "Opalite Boots",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BOOTS,
            combatPowerBonus = 7,
            defPowerBonus = 30,
            iconEmoji = "👢",
            costGp = 1600L,
            description = "Feather-light opalite boots."
        ),
        InventoryItem(
            id = "item_mithril_cape",
            name = "Opalite Cape",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.CAPE,
            combatPowerBonus = 7,
            defPowerBonus = 24,
            iconEmoji = "🧥",
            costGp = 1400L,
            description = "An iridescent cape clasped with an opalite talisman."
        ),
        InventoryItem(
            id = "item_mithril_ring",
            name = "Opalite Ring",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.RING,
            combatPowerBonus = 24,
            defPowerBonus = 20,
            iconEmoji = "💍",
            costGp = 1400L,
            description = "An opalite band pulsing with azure energy."
        ),
        InventoryItem(
            id = "item_mithril_amulet",
            name = "Opalite Amulet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 26,
            defPowerBonus = 22,
            iconEmoji = "📿",
            costGp = 1600L,
            description = "An ancient opalite medallion focusing champion prowess."
        ),

        // ==========================================
        // 7. AMETHYST (ADAMANT) TIER (Tier 70)
        // ==========================================
        InventoryItem(
            id = "item_adamant_sword",
            name = "Amethyst Sword",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 78,
            defPowerBonus = 18,
            iconEmoji = "⚔️",
            costGp = 4500L,
            description = "A heavy crystalline amethyst broadsword with devastating cutting force."
        ),
        InventoryItem(
            id = "item_adamant_scimitar",
            name = "Amethyst Scimitar",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 90,
            defPowerBonus = 10,
            iconEmoji = "🗡️",
            costGp = 5500L,
            description = "A curved amethyst scimitar infused with deep violet mana."
        ),
        InventoryItem(
            id = "item_adamant_dagger",
            name = "Amethyst Dagger",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 62,
            defPowerBonus = 10,
            iconEmoji = "🗡️",
            costGp = 2800L,
            description = "A crystalline dagger capable of punching through iron shells."
        ),
        InventoryItem(
            id = "item_adamant_axe",
            name = "Amethyst Hatchet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AXE,
            combatPowerBonus = 58,
            defPowerBonus = 10,
            iconEmoji = "🪓",
            costGp = 3500L,
            description = "A heavy amethyst forester axe capable of felling Yew trees."
        ),
        InventoryItem(
            id = "item_adamant_full_helm",
            name = "Amethyst Full Helm",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 9,
            defPowerBonus = 68,
            iconEmoji = "🪖",
            costGp = 4500L,
            description = "An impenetrable amethyst helmet encasing the warrior's head."
        ),
        InventoryItem(
            id = "item_adamant_platebody",
            name = "Amethyst Platebody",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BODY,
            combatPowerBonus = 12,
            defPowerBonus = 115,
            iconEmoji = "🛡️",
            costGp = 11000L,
            description = "Colossal amethyst armor providing immense defensive fortitude."
        ),
        InventoryItem(
            id = "item_adamant_platelegs",
            name = "Amethyst Platelegs",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.LEGS,
            combatPowerBonus = 9,
            defPowerBonus = 82,
            iconEmoji = "🦵",
            costGp = 7000L,
            description = "Heavy amethyst greaves offering masterwork defense."
        ),
        InventoryItem(
            id = "item_adamant_kiteshield",
            name = "Amethyst Kiteshield",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 9,
            defPowerBonus = 82,
            iconEmoji = "🛡️",
            costGp = 6500L,
            description = "A fortified amethyst shield deflecting colossal strikes."
        ),
        InventoryItem(
            id = "item_adamant_gauntlets",
            name = "Amethyst Gauntlets",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.GLOVES,
            combatPowerBonus = 9,
            defPowerBonus = 38,
            iconEmoji = "🧤",
            costGp = 3500L,
            description = "Crystal-plated amethyst gauntlets."
        ),
        InventoryItem(
            id = "item_adamant_boots",
            name = "Amethyst Boots",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BOOTS,
            combatPowerBonus = 9,
            defPowerBonus = 38,
            iconEmoji = "👢",
            costGp = 3500L,
            description = "Heavy amethyst boots anchoring the warrior firmly."
        ),
        InventoryItem(
            id = "item_adamant_cape",
            name = "Amethyst Cape",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.CAPE,
            combatPowerBonus = 9,
            defPowerBonus = 32,
            iconEmoji = "🧥",
            costGp = 3000L,
            description = "A deep violet mantle trimmed with crystalline thread."
        ),
        InventoryItem(
            id = "item_adamant_ring",
            name = "Amethyst Ring",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.RING,
            combatPowerBonus = 32,
            defPowerBonus = 26,
            iconEmoji = "💍",
            costGp = 3000L,
            description = "An amethyst band vibrating with intense crystalline power."
        ),
        InventoryItem(
            id = "item_adamant_amulet",
            name = "Amethyst Amulet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 35,
            defPowerBonus = 28,
            iconEmoji = "📿",
            costGp = 3500L,
            description = "A cut amethyst pendant granting unmatched resilience."
        ),

        // ==========================================
        // 8. AETHERITE (RUNE) TIER (Tier 85)
        // ==========================================
        InventoryItem(
            id = "item_rune_sword",
            name = "Aetherite Sword",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 98,
            defPowerBonus = 24,
            iconEmoji = "⚔️",
            costGp = 10000L,
            description = "A masterwork longsword forged from celestial Aetherite."
        ),
        InventoryItem(
            id = "item_rune_scimitar",
            name = "Aetherite Scimitar",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 115,
            defPowerBonus = 14,
            iconEmoji = "🗡️",
            costGp = 12000L,
            description = "A mythical curved saber humming with elemental spirit energy."
        ),
        InventoryItem(
            id = "item_rune_dagger",
            name = "Aetherite Dagger",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 80,
            defPowerBonus = 14,
            iconEmoji = "🗡️",
            costGp = 6000L,
            description = "A deadly aetherite dagger piercing mythical armor."
        ),
        InventoryItem(
            id = "item_rune_axe",
            name = "Aetherite Hatchet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AXE,
            combatPowerBonus = 75,
            defPowerBonus = 14,
            iconEmoji = "🪓",
            costGp = 8000L,
            description = "A masterwork hatchet slicing effortlessly through Ironwood and Magic trees."
        ),
        InventoryItem(
            id = "item_rune_full_helm",
            name = "Aetherite Full Helm",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 12,
            defPowerBonus = 88,
            iconEmoji = "🪖",
            costGp = 10000L,
            description = "Aetherite full helm engraved with high guild crests."
        ),
        InventoryItem(
            id = "item_rune_platebody",
            name = "Aetherite Platebody",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BODY,
            combatPowerBonus = 16,
            defPowerBonus = 145,
            iconEmoji = "🛡️",
            costGp = 25000L,
            description = "The pinnacle of mortal smithing, delivering supreme physical defense."
        ),
        InventoryItem(
            id = "item_rune_platelegs",
            name = "Aetherite Platelegs",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.LEGS,
            combatPowerBonus = 12,
            defPowerBonus = 105,
            iconEmoji = "🦵",
            costGp = 16000L,
            description = "Heavy aetherite leg armor offering supreme resilience."
        ),
        InventoryItem(
            id = "item_rune_kiteshield",
            name = "Aetherite Kiteshield",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 12,
            defPowerBonus = 105,
            iconEmoji = "🛡️",
            costGp = 15000L,
            description = "A towering aetherite shield bearing ancient warding sigils."
        ),
        InventoryItem(
            id = "item_rune_gauntlets",
            name = "Aetherite Gauntlets",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.GLOVES,
            combatPowerBonus = 12,
            defPowerBonus = 48,
            iconEmoji = "🧤",
            costGp = 8000L,
            description = "Aetherite gauntlets augmenting strike power and defense."
        ),
        InventoryItem(
            id = "item_rune_boots",
            name = "Aetherite Boots",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BOOTS,
            combatPowerBonus = 12,
            defPowerBonus = 48,
            iconEmoji = "👢",
            costGp = 8000L,
            description = "Aetherite boots offering unrivaled stability in battle."
        ),
        InventoryItem(
            id = "item_rune_cape",
            name = "Aetherite Cape",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.CAPE,
            combatPowerBonus = 12,
            defPowerBonus = 42,
            iconEmoji = "🧥",
            costGp = 7000L,
            description = "A majestic celestial cape woven with aether threads."
        ),
        InventoryItem(
            id = "item_rune_ring",
            name = "Aetherite Ring",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.RING,
            combatPowerBonus = 42,
            defPowerBonus = 34,
            iconEmoji = "💍",
            costGp = 7000L,
            description = "A glowing aetherite ring channeling raw cosmic power."
        ),
        InventoryItem(
            id = "item_rune_amulet",
            name = "Aetherite Amulet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 46,
            defPowerBonus = 36,
            iconEmoji = "📿",
            costGp = 8000L,
            description = "An aetherite pendant radiating heroic majesty."
        ),

        // ==========================================
        // 9. DRAGON TIER (Pinnacle / Tier 95+)
        // ==========================================
        InventoryItem(
            id = "item_dragon_sword",
            name = "Dragon Sword",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 125,
            defPowerBonus = 30,
            iconEmoji = "⚔️",
            costGp = 35000L,
            description = "A crimson blade forged in ancient dragonfire."
        ),
        InventoryItem(
            id = "item_dragon_scimitar",
            name = "Dragon Scimitar",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 145,
            defPowerBonus = 18,
            iconEmoji = "🗡️",
            costGp = 42000L,
            description = "A ferocious dragon spirit scimitar delivering devastating slash attacks."
        ),
        InventoryItem(
            id = "item_dragon_dagger",
            name = "Dragon Dagger",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.WEAPON,
            combatPowerBonus = 105,
            defPowerBonus = 18,
            iconEmoji = "🗡️",
            costGp = 20000L,
            description = "A vicious dagger glowing with draconic poison and fire."
        ),
        InventoryItem(
            id = "item_dragon_axe",
            name = "Dragon Hatchet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AXE,
            combatPowerBonus = 98,
            defPowerBonus = 18,
            iconEmoji = "🪓",
            costGp = 25000L,
            description = "The ultimate woodcutting hatchet imbued with dragon strength."
        ),
        InventoryItem(
            id = "item_dragon_full_helm",
            name = "Dragon Full Helm",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.HEAD,
            combatPowerBonus = 16,
            defPowerBonus = 112,
            iconEmoji = "🪖",
            costGp = 35000L,
            description = "A menacing dragon metal helm with ornate horns."
        ),
        InventoryItem(
            id = "item_dragon_platebody",
            name = "Dragon Platebody",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BODY,
            combatPowerBonus = 22,
            defPowerBonus = 180,
            iconEmoji = "🛡️",
            costGp = 80000L,
            description = "Legendary crimson dragon platebody conferring godly defense."
        ),
        InventoryItem(
            id = "item_dragon_platelegs",
            name = "Dragon Platelegs",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.LEGS,
            combatPowerBonus = 16,
            defPowerBonus = 130,
            iconEmoji = "🦵",
            costGp = 50000L,
            description = "Pinnacle dragon metal leg armor providing near-invincible protection."
        ),
        InventoryItem(
            id = "item_dragon_kiteshield",
            name = "Dragon Kiteshield",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.SHIELD,
            combatPowerBonus = 16,
            defPowerBonus = 130,
            iconEmoji = "🛡️",
            costGp = 48000L,
            description = "A massive dragon metal shield repelling catastrophic monster attacks."
        ),
        InventoryItem(
            id = "item_dragon_gauntlets",
            name = "Dragon Gauntlets",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.GLOVES,
            combatPowerBonus = 16,
            defPowerBonus = 60,
            iconEmoji = "🧤",
            costGp = 25000L,
            description = "Draconic gauntlets imbued with devastating physical power."
        ),
        InventoryItem(
            id = "item_dragon_boots",
            name = "Dragon Boots",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.BOOTS,
            combatPowerBonus = 16,
            defPowerBonus = 60,
            iconEmoji = "👢",
            costGp = 25000L,
            description = "Fierce dragon metal boots dropped by spiritual elites."
        ),
        InventoryItem(
            id = "item_dragon_cape",
            name = "Dragon Cape",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.CAPE,
            combatPowerBonus = 16,
            defPowerBonus = 55,
            iconEmoji = "🧥",
            costGp = 22000L,
            description = "A blazing crimson cloak forged from dragon scales."
        ),
        InventoryItem(
            id = "item_dragon_ring",
            name = "Dragon Ring",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.RING,
            combatPowerBonus = 55,
            defPowerBonus = 45,
            iconEmoji = "💍",
            costGp = 22000L,
            description = "A legendary dragonstone ring overflowing with draconic power."
        ),
        InventoryItem(
            id = "item_dragon_amulet",
            name = "Dragon Amulet",
            category = ItemCategory.EQUIPMENT,
            equipmentSlot = EquipmentSlot.AMULET,
            combatPowerBonus = 60,
            defPowerBonus = 50,
            iconEmoji = "📿",
            costGp = 25000L,
            description = "A priceless dragon talisman granting immense power."
        )
    )

}
