with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i in range(3310, 3360):
    if i < len(lines):
        print(f"{i+1}: {lines[i]}", end='')
