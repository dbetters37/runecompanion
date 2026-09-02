with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'r', encoding='utf-8') as f:
    text = f.read()

marker = 'val updatedContract = currentContract.copy(currentQty = newQty)'
idx = text.find(marker)
if idx != -1:
    clean_prefix = text[:idx + len(marker)]
    # complete the method progressSkillContract
    clean_prefix += """
        val updated = _contractsMap.value.toMutableMap()
        updated[skill] = updatedContract
        _contractsMap.value = updated
    }
}
"""
    with open('app/src/main/java/com/example/viewmodel/PetViewModel.kt', 'w', encoding='utf-8') as f:
        f.write(clean_prefix)
    print("Cleaned corrupted tail successfully!")
else:
    print("Marker not found!")
