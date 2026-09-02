with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

def print_range(start, end):
    print(f'=== Range {start}-{end} ===')
    for i in range(max(0, start-1), min(len(lines), end)):
        print(f'{i+1}: {lines[i]}', end='')

print_range(3350, 3370)
print_range(10350, 10420)
