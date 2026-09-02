with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

def print_method_lines(start_pat, end_pat):
    found = False
    for idx, l in enumerate(lines):
        if start_pat in l:
            found = True
        if found:
            print(f"{idx+1}: {l}", end='')
            if end_pat in l:
                break

print("=== chopTrees ===")
print_method_lines("fun chopTrees(targetTreeId: String? = null, isAfk: Boolean = false) {", "fun toggleAfkSawmill()")
