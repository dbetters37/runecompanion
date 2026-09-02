import re

with open("app/src/main/java/com/example/viewmodel/PetViewModel.kt", "r", encoding="utf-8") as f:
    code = f.read()

# 1. Ensure imports
if "import com.example.engine.AfkEngine" not in code:
    code = code.replace("package com.example.viewmodel\n", "package com.example.viewmodel\n\nimport com.example.engine.AfkEngine\nimport com.example.engine.AfkActivityType\n")

# 2. Add afkEngine property
if "val afkEngine: AfkEngine = AfkEngine" not in code:
    code = code.replace("class PetViewModel(application: Application) : AndroidViewModel(application) {", "class PetViewModel(application: Application) : AndroidViewModel(application) {\n    val afkEngine: AfkEngine = AfkEngine\n")

# Map of activity variable suffixes to AfkActivityType
activity_map = {
    "Campfire": "CAMPFIRE",
    "Cooking": "COOKING",
    "Fishing": "FISHING",
    "Mining": "MINING",
    "Smelting": "SMELTING",
    "Sawmill": "SAWMILL",
    "Woodcutting": "WOODCUTTING",
    "NailCrafting": "NAIL_CRAFTING",
    "StickCrafting": "STICK_CRAFTING",
    "ShaftCrafting": "SHAFT_CRAFTING",
    "FeatherCrafting": "FEATHER_CRAFTING",
    "BowstringCrafting": "BOWSTRING_CRAFTING",
    "ArrowtipCrafting": "ARROWTIP_CRAFTING",
    "TrapCrafting": "TRAP_CRAFTING",
    "Fletching": "FLETCHING",
    "SmithingAnvil": "SMITHING_ANVIL",
    "HerbCrushing": "HERB_CRUSHING",
    "PotionBrewing": "POTION_BREWING",
    "DruidAltar": "DRUID_ALTAR",
    "Slayer": "SLAYER",
    "Hunter": "HUNTER",
    "Boss": "BOSS",
    "Farming": "FARMING",
    "BoneBurying": "BONE_BURYING",
    "Sailing": "SAILING",
    "Runecrafting": "RUNECRAFTING",
    "Thieving": "THIEVING",
    "Catacombs": "CATACOMBS",
}

# 3. Replace all individual declarations with delegations to AfkEngine
for act, enum_val in activity_map.items():
    decl_pattern = rf"private\s+val\s+_isAfk{act}Active\s*=\s*MutableStateFlow\(false\)\s*(?:val\s+isAfk{act}Active:\s*StateFlow<Boolean>\s*=\s*_isAfk{act}Active\.asStateFlow\(\))?"
    replacement = f"val isAfk{act}Active: StateFlow<Boolean> get() = AfkEngine.isAfk{act}Active"
    code = re.sub(decl_pattern, replacement, code)

# Clean up any leftover duplicate declarations
code = re.sub(r"val isAfkHerbCleaningActive:\s*StateFlow<Boolean>\s*get\(\)\s*=\s*_isAfkHerbCrushingActive", "val isAfkHerbCleaningActive: StateFlow<Boolean> get() = AfkEngine.isAfkHerbCleaningActive", code)
code = re.sub(r"val isAfkSepulchreActive:\s*StateFlow<Boolean>\s*=\s*_isAfkCatacombsActive\.asStateFlow\(\)", "val isAfkSepulchreActive: StateFlow<Boolean> get() = AfkEngine.isAfkSepulchreActive", code)

# 4. Replace boolean assignments `_isAfk...Active.value = true/false`
for act, enum_val in activity_map.items():
    # _isAfkXActive.value = true -> AfkEngine.startActivity(AfkActivityType.X, pohPrefs)
    code = re.sub(rf"_isAfk{act}Active\.value\s*=\s*true", f"AfkEngine.startActivity(AfkActivityType.{enum_val}, pohPrefs)", code)
    # _isAfkXActive.value = false -> AfkEngine.stopAll(pohPrefs)
    code = re.sub(rf"_isAfk{act}Active\.value\s*=\s*false", f"AfkEngine.stopAll(pohPrefs)", code)
    # _isAfkXActive.value -> AfkEngine.isAfkXActive.value (or isAfkXActive.value)
    code = re.sub(rf"_isAfk{act}Active\.value", f"isAfk{act}Active.value", code)
    # _isAfkXActive -> AfkEngine.isAfkXActive
    code = re.sub(rf"_isAfk{act}Active", f"AfkEngine.isAfk{act}Active", code)

# 5. Replace toggle methods to use AfkEngine.toggleActivity
for act, enum_val in activity_map.items():
    # Check if there is a direct toggle like:
    # val nextState = !isAfkXActive.value
    # isAfkXActive.value = nextState
    pass

with open("app/src/main/java/com/example/viewmodel/PetViewModel.kt", "w", encoding="utf-8") as f:
    f.write(code)

print("Replacement complete.")
