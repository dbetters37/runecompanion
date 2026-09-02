with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i in range(4880, 4940):
    print(f"{i+1}: {lines[i]}", end='')
