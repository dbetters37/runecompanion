with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i in range(3690, 3760):
    print(f"{i+1}: {lines[i]}", end='')
