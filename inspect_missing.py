import os, re

kt_files = []
for root, dirs, files in os.walk('app/src/main/java'):
    for f in files:
        if f.endswith('.kt') and f != 'PetViewModel.kt':
            kt_files.append(os.path.join(root, f))

with open('scan_calls.py') as f:
    # Get missing list
    pass

missing = [
    "buyHusbandryLivestock", "claimContractReward", "consumeSkillEffigy", "craftSkillEffigy",
    "craftTroughSlosh", "deleteLoadout", "depositAllAvailableCropsToTrough", "equipLoadout",
    "equipStrongestGear", "equipmentLoadouts", "feedHusbandryTrough", "forgeArmorAtAnvil",
    "forgeEquipmentAtAnvil", "getNpcFavorXp", "getRequiredXpForFavorLevel", "isNpcSessionMinimized",
    "minimizeNpcForSession", "normalizeNpcId", "openSeedPouch", "renameHusbandryLivestock",
    "requestFarmingContract", "saveCurrentLoadout", "sellOrDismissLivestock", "stopPickpocketing",
    "toggleAfkThieving", "transmuteItemToEnergy", "withdrawHusbandryChestRewards"
]

for m in missing:
    print(f'=== {m} ===')
    for path in kt_files:
        with open(path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        for idx, line in enumerate(lines):
            if m in line:
                start = max(0, idx - 2)
                end = min(len(lines), idx + 3)
                print(f'{path}:{idx+1}')
                for l in lines[start:end]:
                    print('   ', l.strip())
                print('---')
