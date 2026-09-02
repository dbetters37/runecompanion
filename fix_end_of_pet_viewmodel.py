with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8', errors='ignore') as f:
    lines = f.readlines()

# Keep lines up to 10494
clean_lines = lines[:10494]
clean_lines.append("        return normTarget.contains(normEntity) || normEntity.contains(normTarget)\n")
clean_lines.append("    }\n")
clean_lines.append("}\n")

with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'w', encoding='utf-8') as f:
    f.writelines(clean_lines)

print("Truncated garbage and properly closed PetViewModel.kt")
