package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EarningDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT COUNT(*) FROM user_profile")
    fun getUsersCountFlow(): Flow<Int>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserSnapshot(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = 1 WHERE id = :taskId")
    suspend fun completeTask(taskId: Int)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)

    @Query("SELECT * FROM referrals ORDER BY timestamp DESC")
    fun getAllReferrals(): Flow<List<ReferralEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(referral: ReferralEntity)

    @Query("SELECT * FROM withdrawals ORDER BY timestamp DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity)

    @Query("UPDATE withdrawals SET status = :status WHERE id = :id")
    suspend fun updateWithdrawalStatus(id: Int, status: String)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM blog_websites")
    fun getAllBlogs(): Flow<List<BlogWebsiteEntity>>

    @Query("SELECT * FROM blog_websites")
    suspend fun getBlogsSnapshot(): List<BlogWebsiteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlog(blog: BlogWebsiteEntity)

    @Query("DELETE FROM blog_websites WHERE id = :blogId")
    suspend fun deleteBlog(blogId: Int)

    @Query("UPDATE blog_websites SET rewardCoins = :newReward WHERE rewardCoins = :oldReward")
    suspend fun updateBlogRewards(oldReward: Int, newReward: Int)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM withdrawals")
    suspend fun clearWithdrawals()

    @Query("DELETE FROM referrals")
    suspend fun clearReferrals()

    @Query("SELECT * FROM app_config WHERE id = 1")
    fun getAppConfigFlow(): Flow<AppConfigEntity?>

    @Query("SELECT * FROM app_config WHERE id = 1")
    suspend fun getAppConfigSnapshot(): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppConfig(config: AppConfigEntity)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteNotification(notificationId: Int)

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()

    @Query("SELECT * FROM chat_messages WHERE userEmail = :userEmail ORDER BY timestamp ASC")
    fun getMessagesForUserFlow(userEmail: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT DISTINCT userEmail FROM chat_messages")
    fun getActiveChatUsersFlow(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET isReadByAdmin = 1 WHERE userEmail = :userEmail")
    suspend fun markAsReadByAdmin(userEmail: String)

    @Query("UPDATE chat_messages SET isReadByUser = 1 WHERE userEmail = :userEmail")
    suspend fun markAsReadByUser(userEmail: String)

    @Query("DELETE FROM chat_messages WHERE userEmail = :userEmail")
    suspend fun deleteChatThread(userEmail: String)

    @Query("SELECT activityKey FROM device_completed_activities")
    fun getCompletedActivityKeysFlow(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviceActivity(activity: DeviceActivityEntity)

    @Query("DELETE FROM device_completed_activities WHERE completedAt < :timestamp")
    suspend fun clearCompletedActivitiesBefore(timestamp: Long)
}
