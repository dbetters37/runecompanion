import re

found = []
for root, dirs, files in __import__('os').walk('app/src/main/java'):
    for f in files:
        if f.endswith('.kt'):
            p = __import__('os').path.join(root, f)
            with open(p, 'r') as fh:
                if 'AdventuringStoryData' in fh.read():
                    found.append(p)

print("Files containing AdventuringStoryData:", found)
