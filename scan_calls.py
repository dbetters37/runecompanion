import os, re

kt_files = []
for root, dirs, files in os.walk('app/src/main/java'):
    for f in files:
        if f.endswith('.kt') and f != 'PetViewModel.kt':
            kt_files.append(os.path.join(root, f))

# Let's find all references to viewModel.xxx or vm.xxx
vm_calls = set()
for path in kt_files:
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    matches = re.findall(r'(?:viewModel|petViewModel|vm)\.([a-zA-Z0-9_]+)', content)
    for m in matches:
        vm_calls.add(m)

# Also find references in UI composables
print('Total unique calls to viewModel/vm:', len(vm_calls))

# Now check which ones are already defined in PetViewModel.kt
with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    vm_content = f.read()

missing = []
for call in sorted(vm_calls):
    if call not in vm_content:
        missing.append(call)

print(f'Missing {len(missing)} references in PetViewModel:')
for m in missing:
    print(' -', m)
