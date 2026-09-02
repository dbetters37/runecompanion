with open('app/src/main/java/com/example/data/models/AdventuringModels.kt', 'r') as f:
    text = f.read()

# find GROVE_FOREST_AREAS, SPIRIT_POOL_AREAS, GEMOLOGY_AREAS
import re
print("=== GROVE_FOREST_AREAS ===")
for m in re.finditer(r'val GROVE_FOREST_AREAS[^\n]*\n', text):
    start = m.start()
    print(text[start:start+2500])

print("=== SPIRIT_POOL_AREAS ===")
for m in re.finditer(r'val SPIRIT_POOL_AREAS[^\n]*\n', text):
    start = m.start()
    print(text[start:start+2500])

print("=== GEMOLOGY_AREAS ===")
for m in re.finditer(r'val GEMOLOGY_AREAS[^\n]*\n', text):
    start = m.start()
    print(text[start:start+2500])

