with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    text = f.read()

import re

# Find all occurrences of settlePendingAfkTime or afk loop
for m in re.finditer(r'fun settlePendingAfkTime', text):
    start = m.start()
    print("=== settlePendingAfkTime ===")
    print(text[start:start+3000])

