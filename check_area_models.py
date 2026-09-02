with open('app/src/main/java/com/example/data/models/AdventuringModels.kt', 'r', encoding='utf-8') as f:
    text = f.read()

import re

for area_type in ['GROVE_FOREST_AREAS', 'SPIRIT_POOL_AREAS', 'GEMOLOGY_AREAS']:
    print(f"================== {area_type} ==================")
    pos = text.find(f"val {area_type}")
    if pos != -1:
        end_pos = text.find("val ", pos + 10)
        if end_pos == -1: end_pos = pos + 4000
        print(text[pos:end_pos])

