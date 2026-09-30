package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class,
        ReferralEntity::class,
        WithdrawalEntity::class,
        TransactionEntity::class,
        BlogWebsiteEntity::class,
        AppConfigEntity::class,
        NotificationEntity::class,
        ChatMessageEntity::class,
        DeviceActivityEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun earningDao(): EarningDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cashpulse_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.earningDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: EarningDao) {
                dao.insertUser(UserEntity(id = 1, coins = 100, totalEarned = 100, totalReferrals = 0))
                
                val blogs = listOf(
                    BlogWebsiteEntity(title = "Market Trend Pro", url = "https://markettrendpro.blogspot.com/", rewardCoins = 50),
                    BlogWebsiteEntity(title = "Loan Guide Pro Hub", url = "https://loanguideprohub.blogspot.com/", rewardCoins = 50),
                    BlogWebsiteEntity(title = "Tech Stack Hero", url = "https://techstackhero.blogspot.com/?m=1", rewardCoins = 50),
                    BlogWebsiteEntity(title = "Home Grow Garden", url = "https://homegrowgarden.blogspot.com/", rewardCoins = 50)
                )
                blogs.forEach { dao.insertBlog(it) }

                // Start with a clean list of referrals, withdrawals and transactions to let users test 100% fresh!
                val txns = listOf(
                    TransactionEntity(title = "Welcome Bonus (Direct Website Download)", coins = 100, type = "BONUS", timestamp = System.currentTimeMillis())
                )
                txns.forEach { dao.insertTransaction(it) }
            }
        }
    }
}
