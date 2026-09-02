for path in ['app/src/main/java/com/example/ui/tabs/TheGroveTab.kt', 'app/src/main/java/com/example/ui/tabs/ShamanPoolTab.kt', 'app/src/main/java/com/example/ui/tabs/SmithingTab.kt', 'app/src/main/java/com/example/engine/AfkEngine.kt']:
    with open(path, 'r') as f:
        print(f"=== {path} ===")
        content = f.read()
        for line in content.split('\n')[:40]:
            print(line)
        print("...")
