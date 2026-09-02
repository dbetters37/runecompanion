with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    text = f.read()

import re

for term in ['toggleAfkGroveHarvest', 'toggleAfkShamanPoolFishing', 'toggleAfkGemologyMining', 'chopTree', 'fishAtPool', 'mineMineral', 'processAfkHarvestTick', 'runAfkLoop', 'afkHarvest', '_selectedTreeId', '_selectedFishId', '_selectedOreId']:
    matches = [m.start() for m in re.finditer(re.escape(term), text)]
    print(f"=== Term '{term}' ({len(matches)} matches) ===")
    for idx in matches[:2]:
        start = max(0, idx - 50)
        end = min(len(text), idx + 250)
        print(text[start:end])
        print("---")
