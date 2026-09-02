with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    text = f.read()

import re

for fn in ['chopTrees', 'fishAtPohPond', 'mineAtPohQuarry']:
    print(f"================ {fn} ================")
    pos = text.find(f"fun {fn}")
    if pos != -1:
        # print up to 120 lines
        lines = text[pos:pos+4000].split('\n')
        for i, l in enumerate(lines[:90]):
            print(f"{i+1}: {l}")

