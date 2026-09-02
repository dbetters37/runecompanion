with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    text = f.read()

import re

for term in ['selectedGroveForestId', 'selectedSpiritPoolAreaId', 'selectedGemologyAreaId', 'processAfkWoodcutting', 'processAfkFishing', 'processAfkMining', 'processAfkTick', 'processAfkGathering']:
    matches = [m.start() for m in re.finditer(re.escape(term), text)]
    print(f"Term '{term}' found at {len(matches)} places")
    for idx in matches[:3]:
        start = max(0, idx - 100)
        end = min(len(text), idx + 300)
        print(f"--- Around index {idx} ---")
        print(text[start:end])

