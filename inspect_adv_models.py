with open('app/src/main/java/com/example/data/models/AdventuringModels.kt', 'r') as f:
    text = f.read()

import re
print("Length of AdventuringModels.kt:", len(text))
for line in text.split('\n'):
    if any(k in line for k in ['Grove', 'Forest', 'Pool', 'Gemology', 'Quarry', 'Sylvan', 'Tree', 'Fish', 'Ore', 'chance', 'Chance', 'percentage', 'weight', 'Weight']):
        print(line[:120])
