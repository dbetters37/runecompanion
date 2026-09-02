import re

with open('app/src/main/java/com/example/data/models/GroveData.kt', 'r', encoding='utf-8') as f:
    print("=== GroveData.kt ===")
    print(f.read()[:2000])

