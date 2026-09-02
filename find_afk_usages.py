import re, os

for root, dirs, files in os.walk('app/src/main/java'):
    for f in files:
        if f.endswith('.kt'):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as fh:
                content = fh.read()
                matches = re.findall(r'.*(?:isAfkWoodcuttingActive|isAfkFishingActive|isAfkMiningActive|WOODCUTTING|FISHING|MINING).*', content)
                matching_lines = [l.strip() for l in matches if any(k in l for k in ['AfkActivityType.WOODCUTTING', 'AfkActivityType.FISHING', 'AfkActivityType.MINING', 'toggleAfkGroveHarvest', 'toggleAfkShamanPoolFishing', 'toggleAfkGemologyMining'])]
                if matching_lines:
                    print(f"=== {path} ({len(matching_lines)} matches) ===")
                    for ml in matching_lines[:10]:
                        print(" ", ml)

