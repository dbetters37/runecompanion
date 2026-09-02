with open('app/src/main/java/com/example/ui/tabs/TheGroveTab.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, l in enumerate(lines):
    if 'chopTrees' in l or 'selectedTreeId' in l or 'setSelectedTreeId' in l:
        print(f"L{i+1}: {l.strip()}")
        for j in range(max(0, i-3), min(len(lines), i+8)):
            print(f"  {j+1}: {lines[j].strip()}")
        print("---")
