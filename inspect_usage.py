with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, l in enumerate(lines):
    if any(k in l for k in ['selectedGroveForestId', 'selectedSpiritPoolAreaId', 'selectedGemologyAreaId', 'isAfkWoodcuttingActive', 'isAfkFishingActive', 'isAfkMiningActive']):
        print(f"L{i+1}: {l.strip()}")
