from pathlib import Path

path = Path("app/src/main/java/com/example/viewmodel/PetViewModel.kt")
data = path.read_bytes()
marker = b"val updatedContract = currentContract.copy(currentQty = newQty)"
index = data.find(marker)

if index == -1:
    raise SystemExit("Marker not found")

clean_prefix = data[: index + len(marker)]
completion = b"""
        val updated = _contractsMap.value.toMutableMap()
        updated[skill] = updatedContract
        _contractsMap.value = updated
    }
}
"""
path.write_bytes(clean_prefix + completion)
print("Cleaned corrupted tail successfully!")
