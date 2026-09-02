with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

def print_section(start, end):
    print(f"=== Lines {start} to {end} ===")
    for i in range(start-1, min(end, len(lines))):
        print(f"{i+1}: {lines[i]}", end='')

print_section(9250, 9370)
print_section(9470, 9590)
