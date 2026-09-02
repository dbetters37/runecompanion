with open('app/src/main/java/com/example/data/models/AdventuringModels.kt', 'r', encoding='utf-8') as f:
    text = f.read()

import re
for m in re.finditer(r'data class (GroveTree|SpiritFish|GemologyMineral|GroveForestArea|SpiritPoolArea|GemologyArea)[^{]*\{[^}]*\}', text):
    print(m.group(0))
    print("---")
