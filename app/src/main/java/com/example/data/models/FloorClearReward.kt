package com.example.data.models

data class FloorClearReward(
    val floorNumber: Int,
    val floorTitle: String,
    val pieceId: String,
    val pieceName: String,
    val slotName: String,
    val skill: OsrsSkill,
    val iconEmoji: String,
    val description: String,
    val isNewPiece: Boolean,
    val totalOwnedCount: Int,
    val totalPrizePoolCount: Int = 99
)
