with open('app/src/main/java/com/example/data/models/AdventuringModels.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

def print_lines(start_str, end_str):
    recording = False
    for idx, line in enumerate(lines):
        if start_str in line:
            recording = True
        if recording:
            print(f"L{idx+1}: {line}", end='')
            if end_str in line and idx > 0 and start_str not in line:
                break

print("=== GROVE_FOREST_AREAS ===")
print_lines("val GROVE_FOREST_AREAS =", "val GEMOLOGY_AREAS =")

