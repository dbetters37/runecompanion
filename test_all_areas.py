with open('app/src/main/java/com/example/data/models/AdventuringModels.kt', 'r', encoding='utf-8') as f:
    text = f.read()

import re

# Let's inspect dropChancePercent in all GroveForestAreas, SpiritPoolAreas, GemologyAreas
print("Checking AdventuringModels.kt...")
with open('app/src/main/java/com/example/data/models/AdventuringModels.kt', 'r') as f:
    lines = f.readlines()

for i, l in enumerate(lines):
    if 'GroveForestArea(' in l or 'SpiritPoolArea(' in l or 'GemologyArea(' in l:
        print(f"L{i+1}: {l.strip()}")
