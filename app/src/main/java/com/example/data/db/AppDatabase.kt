package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PetEntity::class,
        SkillXpEntity::class,
        InventoryEntity::class,
        QuestEntity::class,
        ActivityLogEntity::class,
        EquippedEntity::class,
        BankEntity::class,
        PetSkillXpEntity::class,
        IndividualPetEntity::class,
        QuestProgressEntity::class,
        NpcEntity::class,
        FavorContractEntity::class,
        NpcFavorProgressEntity::class,
        FavorHistoryEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun petDao(): PetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "osrs_pet_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
