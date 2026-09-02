with open("app/src/main/java/com/example/viewmodel/PetViewModel.kt", "r", encoding="utf-8") as f:
    text = f.read()

# 1. Add imports if missing
if "import com.example.engine.AfkEngine" not in text:
    text = text.replace("package com.example.viewmodel\n", "package com.example.viewmodel\n\nimport com.example.engine.AfkEngine\nimport com.example.engine.AfkActivityType\n")

# 2. Add afkEngine property
if "val afkEngine: AfkEngine = AfkEngine" not in text:
    text = text.replace("class PetViewModel(application: Application) : AndroidViewModel(application) {", "class PetViewModel(application: Application) : AndroidViewModel(application) {\n    val afkEngine: AfkEngine = AfkEngine\n")

with open("app/src/main/java/com/example/viewmodel/PetViewModel.kt", "w", encoding="utf-8") as f:
    f.write(text)

print("Updated imports and afkEngine property")
