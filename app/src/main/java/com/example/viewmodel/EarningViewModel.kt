package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.EarningRepository
import com.example.data.TaskEntity
import com.example.data.UserEntity
import com.example.data.ReferralEntity
import com.example.data.WithdrawalEntity
import com.example.data.TransactionEntity
import com.example.data.AppConfigEntity
import com.example.data.NotificationEntity
import com.example.data.ChatMessageEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class EarningViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EarningRepository

    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val isUserLoggedIn = MutableStateFlow(firebaseAuth.currentUser != null)

    val user: StateFlow<UserEntity?>
    val usersCount: StateFlow<Int>
    val appConfig: StateFlow<AppConfigEntity?>
    val notifications: StateFlow<List<NotificationEntity>>
    val activeChatUsers: StateFlow<List<String>>
    val completedActivityKeys: StateFlow<List<String>>
    val tasks: StateFlow<List<TaskEntity>>
    val referrals: StateFlow<List<ReferralEntity>>
    val withdrawals: StateFlow<List<WithdrawalEntity>>
    val transactions: StateFlow<List<TransactionEntity>>
    val blogs: StateFlow<List<com.example.data.BlogWebsiteEntity>>
    val articles = MutableStateFlow<List<ArticleItem>>(emptyList())
    val isFetchingArticles = MutableStateFlow(false)

    private val client = okhttp3.OkHttpClient()

    init {
        val dao = AppDatabase.getDatabase(application).earningDao()
        repository = EarningRepository(dao)

        user = repository.user
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        usersCount = repository.usersCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

        tasks = repository.tasks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        referrals = repository.referrals
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        withdrawals = repository.withdrawals
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        transactions = repository.transactions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        blogs = repository.blogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        appConfig = repository.appConfig
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        notifications = repository.notifications
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeChatUsers = repository.activeChatUsers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        completedActivityKeys = repository.completedActivityKeys
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // Daily Midnight Reset: purge completed activities done before today's 12:00 AM midnight
            repository.clearCompletedActivitiesBefore(getStartOfToday())

            if (repository.getAppConfigSnapshot() == null) {
                repository.updateAppConfig(AppConfigEntity(id = 1, appVersionCode = 1, appVersionName = "1.0.0"))
            }
            // Automatically upgrade any existing 100-coin blogs to 50 coins to sync immediately
            repository.updateBlogRewards(100, 50)

            // Self-healing database correction: clean up any duplicate blog websites with the exact same URL to fix past double inserts!
            val currentBlogs = repository.getBlogsSnapshot()
            val seenUrls = mutableSetOf<String>()
            currentBlogs.forEach { blog ->
                val normalizedUrl = blog.url.trim().lowercase()
                if (seenUrls.contains(normalizedUrl)) {
                    repository.deleteBlogWebsite(blog.id)
                } else {
                    seenUrls.add(normalizedUrl)
                }
            }

            // Self-healing: if the database has 0 blogs (which can happen on upgrade or migration clearing),
            // wait 1500ms to avoid asynchronous race conditions with AppDatabase DatabaseCallback onCreate,
            // then check and populate the default blogs safely!
            kotlinx.coroutines.delay(1500)
            val snapshot = repository.getBlogsSnapshot()
            if (snapshot.isEmpty()) {
                repository.addBlogWebsite("Market Trend Pro", "https://markettrendpro.blogspot.com/", 50)
                repository.addBlogWebsite("Loan Guide Pro Hub", "https://loanguideprohub.blogspot.com/", 50)
                repository.addBlogWebsite("Tech Stack Hero", "https://techstackhero.blogspot.com/?m=1", 50)
                repository.addBlogWebsite("Home Grow Garden", "https://homegrowgarden.blogspot.com/", 50)
            }

            // Trigger Automatic Morning Settlement Check
            kotlinx.coroutines.delay(500)
            checkAndAutoSettleReferralCommissions()
        }

        viewModelScope.launch {
            blogs.collect { list ->
                if (list.isNotEmpty()) {
                    // Filter duplicates in the emitted blogs list as an extra layer of defense
                    val uniqueBlogs = list.distinctBy { it.url.trim().lowercase() }
                    fetchAllArticles(uniqueBlogs)
                }
            }
        }
    }

    private fun parseBloggerJson(jsonStr: String, blogTitle: String, rewardCoins: Int): List<ArticleItem> {
        val list = mutableListOf<ArticleItem>()
        try {
            val root = org.json.JSONObject(jsonStr)
            val feed = root.optJSONObject("feed") ?: return emptyList()
            val entries = feed.optJSONArray("entry") ?: return emptyList()
            for (i in 0 until entries.length()) {
                val entry = entries.getJSONObject(i)
                val titleObj = entry.optJSONObject("title")
                val title = titleObj?.optString("\$t") ?: "Untitled Article"
                
                val links = entry.optJSONArray("link")
                var postUrl = ""
                if (links != null) {
                    for (j in 0 until links.length()) {
                        val linkObj = links.getJSONObject(j)
                        if (linkObj.optString("rel") == "alternate") {
                            postUrl = linkObj.optString("href")
                            break
                        }
                    }
                }
                if (postUrl.isNotEmpty()) {
                    list.add(
                        ArticleItem(
                            title = title,
                            url = postUrl,
                            blogTitle = blogTitle,
                            rewardCoins = rewardCoins
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun fetchAllArticles(blogsList: List<com.example.data.BlogWebsiteEntity>) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            isFetchingArticles.value = true
            val allArticles = mutableListOf<ArticleItem>()
            blogsList.forEach { blog ->
                var sanitizedUrl = blog.url.trim()
                if (sanitizedUrl.contains("?")) {
                    // Extract base url before query parameter
                    sanitizedUrl = sanitizedUrl.substringBefore("?")
                }
                val feedUrl = if (sanitizedUrl.endsWith("/")) {
                    "${sanitizedUrl}feeds/posts/default?alt=json"
                } else {
                    "${sanitizedUrl}/feeds/posts/default?alt=json"
                }
                try {
                    val request = okhttp3.Request.Builder().url(feedUrl).build()
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val bodyStr = response.body?.string()
                            if (!bodyStr.isNullOrEmpty()) {
                                val parsed = parseBloggerJson(bodyStr, blog.title, blog.rewardCoins)
                                allArticles.addAll(parsed)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // Defensively remove any duplicate articles by their lowercase URL to guarantee 100% unique items!
            articles.value = allArticles.distinctBy { it.url.trim().lowercase() }
            isFetchingArticles.value = false
        }
    }

    fun completeTask(task: TaskEntity) {
        viewModelScope.launch {
            if (!task.isCompleted) {
                repository.completeTask(task.id)
                user.value?.let { curr ->
                    val newCoins = curr.coins + task.reward
                    val newEarned = curr.totalEarned + task.reward
                    repository.updateUser(curr.copy(coins = newCoins, totalEarned = newEarned))
                }
                repository.insertTransaction(TransactionEntity(title = "Task: ${task.title}", coins = task.reward, type = "EARN"))
            }
            repository.insertDeviceActivity(com.example.data.DeviceActivityEntity(activityKey = "TASK_" + task.id))
        }
    }

    fun canClaimDailyCheckin(): Boolean {
        val txs = transactions.value
        val todayStart = getStartOfToday()
        return !txs.any { it.title == "Daily Check-in Reward" && it.timestamp >= todayStart }
    }

    private fun getStartOfToday(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun claimDailyCheckin(onClaimed: (Int) -> Unit = {}) {
        viewModelScope.launch {
            if (!canClaimDailyCheckin()) return@launch
            
            val txs = transactions.value
            val totalCheckins = txs.count { it.title == "Daily Check-in Reward" }
            
            // Exactly on every 7th checkin, give 50 to 100 coins. All other days, give hamesha less than 50 coins!
            val reward = if ((totalCheckins + 1) % 7 == 0) {
                kotlin.random.Random.nextInt(50, 101)
            } else {
                kotlin.random.Random.nextInt(15, 46)
            }
            
            user.value?.let { curr ->
                val newCoins = curr.coins + reward
                val newEarned = curr.totalEarned + reward
                repository.updateUser(curr.copy(coins = newCoins, totalEarned = newEarned))
                repository.insertTransaction(
                    TransactionEntity(
                        title = "Daily Check-in Reward",
                        coins = reward,
                        type = "BONUS"
                    )
                )
                onClaimed(reward)
            }
        }
    }

    fun spinWheelReward(rewardCoins: Int) {
        viewModelScope.launch {
            user.value?.let { curr ->
                val newCoins = curr.coins + rewardCoins
                val newEarned = curr.totalEarned + rewardCoins
                repository.updateUser(curr.copy(coins = newCoins, totalEarned = newEarned))
                repository.insertTransaction(TransactionEntity(title = "Spin & Win Bonus", coins = rewardCoins, type = "BONUS"))
            }
        }
    }

    fun scratchCardReward(rewardCoins: Int) {
        viewModelScope.launch {
            user.value?.let { curr ->
                val newCoins = curr.coins + rewardCoins
                val newEarned = curr.totalEarned + rewardCoins
                repository.updateUser(curr.copy(coins = newCoins, totalEarned = newEarned))
                repository.insertTransaction(TransactionEntity(title = "Scratch Card Reward", coins = rewardCoins, type = "BONUS"))
            }
        }
    }

    fun requestWithdrawal(method: String, detail: String, coins: Int) {
        viewModelScope.launch {
            user.value?.let { curr ->
                if (curr.coins >= coins) {
                    val newCoins = curr.coins - coins
                    repository.updateUser(curr.copy(coins = newCoins))
                    val rupees = coins / 100.0 // 100 coins = ₹1
                    repository.requestWithdrawal(method, detail, coins, rupees)
                }
            }
        }
    }

    fun addReferral(friendName: String) {
        viewModelScope.launch {
            val reward = 500
            repository.insertReferral(friendName, reward)
            user.value?.let { curr ->
                val newCoins = curr.coins + reward
                val newEarned = curr.totalEarned + reward
                val newRefs = curr.totalReferrals + 1
                repository.updateUser(curr.copy(coins = newCoins, totalEarned = newEarned, totalReferrals = newRefs))
            }
        }
    }

    fun simulateFriendEarning(ref: ReferralEntity, amount: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val commission = (amount * 0.10).toInt()
            val updated = ref.copy(
                friendEarning = ref.friendEarning + amount,
                pendingCommission = ref.pendingCommission + commission
            )
            repository.insertReferral(updated)
        }
    }

    fun checkAndAutoSettleReferralCommissions() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val currUser = repository.user.first() ?: return@launch
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            
            // Check if we already auto-settled today. If not, auto-settle!
            if (currUser.lastSettlementDay != todayDate) {
                val referralsSnapshot = repository.referrals.first()
                var totalCommission = 0
                val updatedList = referralsSnapshot.map { ref ->
                    if (ref.pendingCommission > 0) {
                        totalCommission += ref.pendingCommission
                        ref.copy(
                            commissionEarned = ref.commissionEarned + ref.pendingCommission,
                            pendingCommission = 0
                        )
                    } else {
                        ref
                    }
                }
                
                // Settle and credit user coins
                val newCoins = currUser.coins + totalCommission
                val newEarned = currUser.totalEarned + totalCommission
                val newCommission = currUser.referralCommission + totalCommission
                
                repository.updateUser(
                    currUser.copy(
                        coins = newCoins,
                        totalEarned = newEarned,
                        referralCommission = newCommission,
                        lastSettlementDay = todayDate
                    )
                )
                
                if (totalCommission > 0) {
                    updatedList.forEach { repository.insertReferral(it) }
                    repository.insertTransaction(
                        TransactionEntity(
                            title = "Daily Auto-Settled Morning Commission (10%)",
                            coins = totalCommission,
                            type = "EARN"
                        )
                    )
                }
            }
        }
    }

    fun simulateMorningSettlement(onSettled: (Int) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val currUser = repository.user.first() ?: return@launch
            // Reset last settlement date to force automatic check to run
            repository.updateUser(currUser.copy(lastSettlementDay = ""))
            kotlinx.coroutines.delay(100)
            
            val referralsSnapshot = repository.referrals.first()
            var totalCommission = 0
            val updatedList = referralsSnapshot.map { ref ->
                if (ref.pendingCommission > 0) {
                    totalCommission += ref.pendingCommission
                    ref.copy(
                        commissionEarned = ref.commissionEarned + ref.pendingCommission,
                        pendingCommission = 0
                    )
                } else {
                    ref
                }
            }
            
            val freshUser = repository.user.first() ?: return@launch
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val newCoins = freshUser.coins + totalCommission
            val newEarned = freshUser.totalEarned + totalCommission
            val newCommission = freshUser.referralCommission + totalCommission
            
            repository.updateUser(
                freshUser.copy(
                    coins = newCoins,
                    totalEarned = newEarned,
                    referralCommission = newCommission,
                    lastSettlementDay = todayDate
                )
            )
            
            if (totalCommission > 0) {
                updatedList.forEach { repository.insertReferral(it) }
                repository.insertTransaction(
                    TransactionEntity(
                        title = "Daily Auto-Settled Morning Commission (10%)",
                        coins = totalCommission,
                        type = "EARN"
                    )
                )
            }
            onSettled(totalCommission)
        }
    }

    // Admin methods
    fun adminAddCoins(amount: Int) {
        viewModelScope.launch {
            user.value?.let { curr ->
                val newCoins = curr.coins + amount
                val newEarned = curr.totalEarned + amount
                repository.updateUser(curr.copy(coins = newCoins, totalEarned = newEarned))
                repository.insertTransaction(TransactionEntity(title = "Admin Grant", coins = amount, type = "BONUS"))
            }
        }
    }

    fun adminSetCoins(amount: Int) {
        viewModelScope.launch {
            user.value?.let { curr ->
                val difference = amount - curr.coins
                repository.updateUser(curr.copy(coins = amount))
                repository.insertTransaction(TransactionEntity(title = "Admin Balance Set", coins = difference, type = "BONUS"))
            }
        }
    }

    fun adminUpdateWithdrawalStatus(withdrawalId: Int, status: String) {
        viewModelScope.launch {
            repository.updateWithdrawalStatus(withdrawalId, status)
        }
    }

    fun setAdminMode(isAdmin: Boolean) {
        viewModelScope.launch {
            user.value?.let { curr ->
                val isEmailAdmin = curr.email.trim().lowercase() == "upendrawithmasti@gmail.com"
                repository.updateUser(curr.copy(isAdmin = isEmailAdmin && isAdmin))
            }
        }
    }

    fun rewardForReadingBlog(blogTitle: String, articleUrl: String, reward: Int) {
        viewModelScope.launch {
            user.value?.let { curr ->
                val newCoins = curr.coins + reward
                val newEarned = curr.totalEarned + reward
                repository.updateUser(curr.copy(coins = newCoins, totalEarned = newEarned))
                repository.insertTransaction(TransactionEntity(title = "Read Article: $blogTitle", coins = reward, type = "EARN"))
            }
            repository.insertDeviceActivity(com.example.data.DeviceActivityEntity(activityKey = articleUrl))
        }
    }

    fun adminAddBlog(title: String, url: String, reward: Int) {
        viewModelScope.launch {
            repository.addBlogWebsite(title, url, reward)
        }
    }

    fun adminDeleteBlog(blogId: Int) {
        viewModelScope.launch {
            repository.deleteBlogWebsite(blogId)
        }
    }

    fun adminAddTask(title: String, description: String, reward: Int, category: String, actionUrl: String) {
        viewModelScope.launch {
            repository.addTask(title, description, reward, category, actionUrl)
        }
    }

    fun adminDeleteTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun simulateUserRegistration(name: String, email: String, appliedReferralCode: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.clearDatabaseForNewUser()
            
            // Check if user specified any referral code
            val hasReferral = appliedReferralCode.isNotBlank()
            val welcomeBonus = if (hasReferral) 500 else 100
            
            // Generate a fresh random referral code for the newly registered user
            val allowedChars = ('A'..'Z') + ('0'..'9')
            val randomRefCode = (1..8).map { allowedChars.random() }.joinToString("")
            
            val randomVipId = "CP-" + (100000..999999).random().toString()
            val newUser = UserEntity(
                id = 1,
                name = name.ifBlank { "User ${randomRefCode.take(4)}" },
                email = email.ifBlank { "user.${randomRefCode.lowercase()}@example.com" },
                coins = welcomeBonus,
                totalEarned = welcomeBonus,
                referralCode = randomRefCode,
                totalReferrals = 0,
                isAdmin = email.trim().lowercase() == "upendrawithmasti@gmail.com",
                referralCommission = 0,
                lastSettlementDay = "",
                vipMemberId = randomVipId
            )
            
            repository.insertUser(newUser)
            
            val welcomeTitle = if (hasReferral) {
                "Welcome Bonus (Referred by: $appliedReferralCode)"
            } else {
                "Welcome Bonus (Direct Download)"
            }
            repository.insertTransaction(
                TransactionEntity(title = welcomeTitle, coins = welcomeBonus, type = "BONUS")
            )
        }
    }

    fun firebaseLogin(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val fbUser = authResult.user
                val emailStr = fbUser?.email ?: email
                val nameStr = emailStr.substringBefore("@")
                
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    val existingUser = repository.getUserSnapshot()
                    val isMasterAdmin = emailStr.trim().lowercase() == "upendrawithmasti@gmail.com"
                    val coinsToSet = if (existingUser != null && existingUser.email == emailStr) existingUser.coins else 100
                    val earnedToSet = if (existingUser != null && existingUser.email == emailStr) existingUser.totalEarned else 100
                    val vipIdToSet = if (existingUser != null && existingUser.email == emailStr && existingUser.vipMemberId.isNotBlank() && existingUser.vipMemberId != "CP-123456") {
                        existingUser.vipMemberId
                    } else {
                        "CP-" + (100000..999999).random().toString()
                    }
                    
                    val userToInsert = UserEntity(
                        id = 1,
                        name = nameStr,
                        email = emailStr,
                        coins = coinsToSet,
                        totalEarned = earnedToSet,
                        isAdmin = isMasterAdmin,
                        vipMemberId = vipIdToSet
                    )
                    repository.insertUser(userToInsert)
                    isUserLoggedIn.value = true
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onSuccess()
                    }
                }
            }
            .addOnFailureListener {
                onFailure(it.localizedMessage ?: "Login failed")
            }
    }

    fun firebaseSignup(email: String, password: String, referCodeApplied: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val fbUser = authResult.user
                val emailStr = fbUser?.email ?: email
                val nameStr = emailStr.substringBefore("@")
                
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    repository.clearDatabaseForNewUser()
                    
                    val hasReferral = referCodeApplied.isNotBlank()
                    val welcomeBonus = if (hasReferral) 500 else 100
                    val isMasterAdmin = emailStr.trim().lowercase() == "upendrawithmasti@gmail.com"
                    
                    val allowedChars = ('A'..'Z') + ('0'..'9')
                    val randomRefCode = (1..8).map { allowedChars.random() }.joinToString("")
                    val randomVipId = "CP-" + (100000..999999).random().toString()
                    
                    val newUser = UserEntity(
                        id = 1,
                        name = nameStr,
                        email = emailStr,
                        coins = welcomeBonus,
                        totalEarned = welcomeBonus,
                        referralCode = randomRefCode,
                        totalReferrals = 0,
                        isAdmin = isMasterAdmin,
                        referralCommission = 0,
                        lastSettlementDay = "",
                        vipMemberId = randomVipId
                    )
                    repository.insertUser(newUser)
                    
                    val welcomeTitle = if (hasReferral) {
                        "Welcome Bonus (Referred by: $referCodeApplied)"
                    } else {
                        "Welcome Bonus (Direct Download)"
                    }
                    repository.insertTransaction(
                        TransactionEntity(title = welcomeTitle, coins = welcomeBonus, type = "BONUS")
                    )
                    
                    isUserLoggedIn.value = true
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onSuccess()
                    }
                }
            }
            .addOnFailureListener {
                onFailure(it.localizedMessage ?: "Signup failed")
            }
    }

    fun firebaseForgotPassword(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it.localizedMessage ?: "Password reset request failed")
            }
    }

    fun firebaseLogout() {
        firebaseAuth.signOut()
        isUserLoggedIn.value = false
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.insertUser(UserEntity(id = 1, coins = 0, totalEarned = 0, email = ""))
        }
    }

    fun adminSetAppUpdate(versionCode: Int, versionName: String, message: String, url: String, mandatory: Boolean) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val current = repository.getAppConfigSnapshot() ?: AppConfigEntity()
            repository.updateAppConfig(current.copy(
                appVersionCode = versionCode,
                appVersionName = versionName,
                updateMessage = message,
                updateUrl = url,
                isUpdateMandatory = mandatory
            ))
        }
    }

    fun adminSendNotification(title: String, message: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.sendNotification(title, message)
        }
    }

    fun dismissGlobalNotification() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.dismissGlobalNotification()
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.deleteNotification(id)
        }
    }

    fun getChatMessagesForUser(userEmail: String): Flow<List<ChatMessageEntity>> {
        return repository.getMessagesForUser(userEmail)
    }

    fun sendUserMessage(userEmail: String, messageText: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.insertChatMessage(
                ChatMessageEntity(
                    userEmail = userEmail,
                    senderEmail = userEmail,
                    messageText = messageText
                )
            )
        }
    }

    fun sendAdminMessage(userEmail: String, messageText: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.insertChatMessage(
                ChatMessageEntity(
                    userEmail = userEmail,
                    senderEmail = "admin",
                    messageText = messageText
                )
            )
        }
    }

    fun markChatAsReadByAdmin(userEmail: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.markAsReadByAdmin(userEmail)
        }
    }

    fun markChatAsReadByUser(userEmail: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.markAsReadByUser(userEmail)
        }
    }

    fun clearUserChatThread(userEmail: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.deleteChatThread(userEmail)
        }
    }
}

data class ArticleItem(
    val title: String,
    val url: String,
    val blogTitle: String,
    val rewardCoins: Int
)
