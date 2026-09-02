import re

for path in ['app/src/main/java/com/example/ui/tabs/TheGroveTab.kt', 'app/src/main/java/com/example/ui/tabs/ShamanPoolTab.kt', 'app/src/main/java/com/example/ui/tabs/SmithingTab.kt']:
    with open(path, 'r') as f:
        content = f.read()
    print(f"=== {path} ===")
    data_classes = re.findall(r'(?:data class|object|enum class|class)\s+[A-Za-z0-9_]+[^{]*\{[^}]*\}', content)
    for dc in data_classes[:5]:
        print(dc[:300])
        print("---")
    
    # look for area lists or models
    lines = content.split('\n')
    for i, line in enumerate(lines):
        if 'val ' in line and ('Area' in line or 'areas' in line or 'Grove' in line or 'Pool' in line or 'Quarry' in line or 'Mine' in line or 'CANOPY' in line or 'Sylvan' in line):
            print(f"L{i+1}: {line}")
            for j in range(i, min(i+25, len(lines))):
                print(f"  {lines[j]}")
            break

