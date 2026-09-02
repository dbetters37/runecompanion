with open('app/src/main/java/com/example/data/models/AdventuringModels.kt', 'r', encoding='utf-8') as f:
    text = f.read()

import re

def check_areas(block_name):
    print(f"=== {block_name} ===")
    start = text.find(f"val {block_name}")
    end = text.find("val ", start + 20)
    if end == -1: end = len(text)
    block = text[start:end]
    
    # find each area name and its items
    area_matches = re.findall(r'name\s*=\s*"([^"]+)",[\s\S]*?(?:choppableTrees|catchableFish|minerals)\s*=\s*listOf\(([\s\S]*?)\)\s*(?:\)|,)', block)
    for name, items in area_matches:
        print(f"Area: {name}")
        # parse items
        item_entries = re.findall(r'(?:GroveTree|SpiritFish|GemologyMineral)\("([^"]+)",\s*"([^"]+)",\s*"([^"]+)",\s*(\d+),\s*(\d+)L,\s*([^,\)]+)', items)
        if not item_entries:
            # try with dropChancePercent or description
            item_entries_2 = re.findall(r'(?:GroveTree|SpiritFish|GemologyMineral)\(([\s\S]*?)\)', items)
            for raw in item_entries_2:
                print(f"  raw item: {raw.strip()}")
        else:
            for item in item_entries:
                print(f"  Item: {item[1]} (req: {item[3]}, xp: {item[4]}, chance/extra: {item[5].strip()})")

check_areas("GROVE_FOREST_AREAS")
check_areas("SPIRIT_POOL_AREAS")
check_areas("GEMOLOGY_AREAS")

