with open('app/src/main/java/com/example/data/models/AdventuringModels.kt', 'r', encoding='utf-8') as f:
    text = f.read()

import re

# Check all GemologyMineral, GroveTree, SpiritFish occurrences and print them
for m in re.finditer(r'(GroveTree|SpiritFish|GemologyMineral)\(([^)]+)\)', text):
    call_type = m.group(1)
    args = m.group(2)
    # Check dropChancePercent
    # GemologyMineral has reqLevel, xp, dropChancePercent, description...
    print(f"{call_type}: {args.strip()[:80]}")

