package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alex Johnson",
    val email: String = "alex.johnson@cashpulse.com",
    val coins: Int = 100,
    val referralCode: String = "PULSE2026",
    val totalEarned: Int = 100,
    val totalReferrals: Int = 0,
    val isAdmin: Boolean = false,
    val referralCommission: Int = 0, // total commission settled to user account so far
    val lastSettlementDay: String = "", // formatted "yyyy-MM-dd" when morning commission was last auto-credited
    val vipMemberId: String = "CP-123456"
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val reward: Int,
    val category: String, // "Offer", "Video", "Quiz", "Check-in"
    val isCompleted: Boolean = false,
    val actionUrl: String = ""
)

@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val friendName: String,
    val earnedCoins: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val friendEarning: Int = 0, // friend's total dynamic earnings
    val commissionEarned: Int = 0, // 10% commission settled to user
    val pendingCommission: Int = 0 // 10% commission waiting for morning payout settlement!
)

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val method: String, // "UPI", "Paytm", "Bank"
    val accountDetail: String,
    val amountCoins: Int,
    val amountRupees: Double,
    val status: String = "Pending", // "Pending", "Success", "Rejected"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val coins: Int,
    val type: String, // "EARN", "WITHDRAWAL", "BONUS"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "blog_websites")
data class BlogWebsiteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val rewardCoins: Int = 100
)

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val appVersionCode: Int = 1,
    val appVersionName: String = "1.0.0",
    val isUpdateMandatory: Boolean = false,
    val updateUrl: String = "",
    val updateMessage: String = "",
    val notificationTitle: String = "",
    val notificationMessage: String = "",
    val showNotification: Boolean = false
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,      // Thread owner email
    val senderEmail: String,    // Email of actual sender
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isReadByAdmin: Boolean = false,
    val isReadByUser: Boolean = false
)

@Entity(tableName = "device_completed_activities")
data class DeviceActivityEntity(
    @PrimaryKey val activityKey: String, // Article URL or "TASK_" + taskId
    val completedAt: Long = System.currentTimeMillis()
)
