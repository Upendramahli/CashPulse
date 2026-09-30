package com.example.data

import kotlinx.coroutines.flow.Flow

class EarningRepository(private val dao: EarningDao) {
    val user: Flow<UserEntity?> = dao.getUser()
    val usersCount: Flow<Int> = dao.getUsersCountFlow()

    suspend fun getUserSnapshot(): UserEntity? {
        return dao.getUserSnapshot()
    }
    val tasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val referrals: Flow<List<ReferralEntity>> = dao.getAllReferrals()
    val withdrawals: Flow<List<WithdrawalEntity>> = dao.getAllWithdrawals()
    val transactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val blogs: Flow<List<BlogWebsiteEntity>> = dao.getAllBlogs()

    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)
    
    suspend fun completeTask(taskId: Int) {
        dao.completeTask(taskId)
    }

    suspend fun requestWithdrawal(method: String, detail: String, coins: Int, rupees: Double) {
        dao.insertWithdrawal(WithdrawalEntity(method = method, accountDetail = detail, amountCoins = coins, amountRupees = rupees))
        dao.insertTransaction(TransactionEntity(title = "Withdrawal ($method)", coins = -coins, type = "WITHDRAWAL"))
    }

    suspend fun updateWithdrawalStatus(id: Int, status: String) {
        dao.updateWithdrawalStatus(id, status)
    }

    suspend fun insertReferral(friendName: String, coins: Int) {
        dao.insertReferral(ReferralEntity(friendName = friendName, earnedCoins = coins, friendEarning = 500, pendingCommission = 50))
        dao.insertTransaction(TransactionEntity(title = "Referral Bonus ($friendName)", coins = coins, type = "BONUS"))
    }

    suspend fun insertReferral(referral: ReferralEntity) {
        dao.insertReferral(referral)
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        dao.insertTransaction(transaction)
    }

    suspend fun addTask(title: String, description: String, reward: Int, category: String, actionUrl: String) {
        dao.insertTask(TaskEntity(title = title, description = description, reward = reward, category = category, actionUrl = actionUrl))
    }

    suspend fun deleteTask(taskId: Int) {
        dao.deleteTask(taskId)
    }

    suspend fun addBlogWebsite(title: String, url: String, rewardCoins: Int) {
        dao.insertBlog(BlogWebsiteEntity(title = title, url = url, rewardCoins = rewardCoins))
    }

    suspend fun deleteBlogWebsite(blogId: Int) {
        dao.deleteBlog(blogId)
    }

    suspend fun getBlogsSnapshot(): List<BlogWebsiteEntity> {
        return dao.getBlogsSnapshot()
    }

    suspend fun updateBlogRewards(oldReward: Int, newReward: Int) {
        dao.updateBlogRewards(oldReward, newReward)
    }

    suspend fun clearDatabaseForNewUser() {
        dao.clearTransactions()
        dao.clearWithdrawals()
        dao.clearReferrals()
    }

    suspend fun insertUser(user: UserEntity) {
        dao.insertUser(user)
    }

    val appConfig: Flow<AppConfigEntity?> = dao.getAppConfigFlow()
    val notifications: Flow<List<NotificationEntity>> = dao.getAllNotificationsFlow()

    suspend fun getAppConfigSnapshot(): AppConfigEntity? {
        return dao.getAppConfigSnapshot()
    }

    suspend fun updateAppConfig(config: AppConfigEntity) {
        dao.insertAppConfig(config)
    }

    suspend fun sendNotification(title: String, message: String) {
        dao.insertNotification(NotificationEntity(title = title, message = message))
        val current = dao.getAppConfigSnapshot() ?: AppConfigEntity()
        dao.insertAppConfig(current.copy(
            notificationTitle = title,
            notificationMessage = message,
            showNotification = true
        ))
    }

    suspend fun dismissGlobalNotification() {
        val current = dao.getAppConfigSnapshot() ?: return
        dao.insertAppConfig(current.copy(showNotification = false))
    }

    suspend fun deleteNotification(id: Int) {
        dao.deleteNotification(id)
    }

    fun getMessagesForUser(userEmail: String): Flow<List<ChatMessageEntity>> = dao.getMessagesForUserFlow(userEmail)
    val activeChatUsers: Flow<List<String>> = dao.getActiveChatUsersFlow()

    suspend fun insertChatMessage(message: ChatMessageEntity) {
        dao.insertChatMessage(message)
    }

    suspend fun markAsReadByAdmin(userEmail: String) {
        dao.markAsReadByAdmin(userEmail)
    }

    suspend fun markAsReadByUser(userEmail: String) {
        dao.markAsReadByUser(userEmail)
    }

    suspend fun deleteChatThread(userEmail: String) {
        dao.deleteChatThread(userEmail)
    }

    val completedActivityKeys: Flow<List<String>> = dao.getCompletedActivityKeysFlow()

    suspend fun insertDeviceActivity(activity: DeviceActivityEntity) {
        dao.insertDeviceActivity(activity)
    }

    suspend fun clearCompletedActivitiesBefore(timestamp: Long) {
        dao.clearCompletedActivitiesBefore(timestamp)
    }
}
