with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    text = f.read()

def print_function(name):
    print(f"================ {name} ================")
    idx = 0
    while True:
        pos = text.find(f"fun {name}", idx)
        if pos == -1:
            break
        print(text[pos:pos+1500])
        print("-----------------------------------------")
        idx = pos + len(name) + 4

for fn in ['chopTrees', 'fishAtPohPond', 'mineAtPohQuarry', 'toggleAfkGroveHarvest', 'toggleAfkShamanPoolFishing', 'toggleAfkGemologyMining']:
    print_function(fn)

