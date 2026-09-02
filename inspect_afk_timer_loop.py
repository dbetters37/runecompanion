with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    text = f.read()

import re
idx = text.find('AfkActivityType.WOODCUTTING ->')
if idx != -1:
    print("=== Found AfkActivityType.WOODCUTTING ===")
    print(text[idx-200:idx+1500])

