with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'rb') as f:
    raw_file = f.read()

# Let's find where 'val updatedContract = currentContract.copy(currentQty = newQty)\n' ends
marker = b'val updatedContract = currentContract.copy(currentQty = newQty)\n'
pos = raw_file.find(marker)
if pos != -1:
    tail_bytes = raw_file[pos + len(marker):]
    print('Tail bytes len:', len(tail_bytes))
    print('First 100 tail bytes:', tail_bytes[:100])
