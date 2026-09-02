import re

for tab_path in ['app/src/main/java/com/example/ui/tabs/TheGroveTab.kt', 'app/src/main/java/com/example/ui/tabs/ShamanPoolTab.kt', 'app/src/main/java/com/example/ui/tabs/SmithingTab.kt']:
    with open(tab_path, 'r', encoding='utf-8') as f:
        text = f.read()
    print(f"=== {tab_path} ===")
    for m in re.finditer(r'(toggleAfk|chopTrees|fishAtPoh|mineAtPoh|selectedGroveForestId|selectedSpiritPoolAreaId|selectedGemologyAreaId|selectTree|selectFish|selectOre)', text):
        start = max(0, m.start() - 60)
        end = min(len(text), m.end() + 100)
        print(text[start:end].replace('\n', ' '))
        print("---")
