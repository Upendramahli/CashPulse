package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import com.example.data.UserEntity
import com.example.data.AppConfigEntity
import com.example.data.NotificationEntity
import com.example.viewmodel.EarningViewModel
import kotlin.random.Random
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: UserEntity?,
    tasks: List<TaskEntity>,
    viewModel: EarningViewModel,
    onNavigateToWallet: () -> Unit,
    onNavigateToReadAndEarn: () -> Unit
) {
    val completedKeys by viewModel.completedActivityKeys.collectAsStateWithLifecycle(emptyList())
    val incompleteTasks = remember(tasks, completedKeys) {
        tasks.filter { task ->
            !task.isCompleted && !completedKeys.contains("TASK_" + task.id)
        }
    }

    var showSpinDialog by remember { mutableStateOf(false) }
    var showScratchDialog by remember { mutableStateOf(false) }
    var spinResult by remember { mutableStateOf<Int?>(null) }
    var scratchResult by remember { mutableStateOf<Int?>(null) }
    
    val transactions by viewModel.transactions.collectAsStateWithLifecycle(emptyList())
    val checkedInToday = remember(transactions) {
        val todayStart = getStartOfToday()
        transactions.any { it.title == "Daily Check-in Reward" && it.timestamp >= todayStart }
    }
    var showCheckinSuccessDialog by remember { mutableStateOf(false) }
    var checkinWonCoins by remember { mutableStateOf(0) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val appConfigState by viewModel.appConfig.collectAsStateWithLifecycle(null)

    val currentCoins = user?.coins ?: 0
    val rupeesValue = currentCoins / 100.0

    LazyColumn(
        modifier = SystemWindowInsetsModifier().fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Welcome back,",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    fontSize = 14.dpToSp()
                                )
                                Text(
                                    text = user?.name ?: "User",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "VIP ID: ${user?.vipMemberId ?: "CP-123456"}",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                var showNotificationsDialog by remember { mutableStateOf(false) }
                                val notificationsList by viewModel.notifications.collectAsStateWithLifecycle(emptyList())

                                IconButton(
                                    onClick = { showNotificationsDialog = true },
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                ) {
                                    Box {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Notifications",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                        if (notificationsList.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Red)
                                                    .align(Alignment.TopEnd)
                                            )
                                        }
                                    }
                                }

                                if (showNotificationsDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showNotificationsDialog = false },
                                        title = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Notification Center", fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        text = {
                                            if (notificationsList.isEmpty()) {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("No notifications received yet.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                }
                                            } else {
                                                LazyColumn(
                                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    items(notificationsList) { item ->
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp)) {
                                                                Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                                Spacer(modifier = Modifier.height(4.dp))
                                                                Text(text = item.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            Button(onClick = { showNotificationsDialog = false }) {
                                                Text("Close")
                                            }
                                        }
                                    )
                                }

                                IconButton(
                                    onClick = onNavigateToWallet,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Total Earnings",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$currentCoins Coins",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Text(
                                    text = "≈ ₹%.2f".format(rupeesValue),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = onNavigateToWallet,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Withdraw", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Activities Header
        item {
            Text(
                text = "⚡ Instant Earning Activities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Read & Earn Feature Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToReadAndEarn() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "📖 Read & Earn Articles",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Explore trending blog posts & earn instant coins",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Available Tasks Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 High Reward Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${incompleteTasks.size} Available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Tasks List
        items(incompleteTasks) { task ->
            TaskItemCard(task = task, onComplete = { viewModel.completeTask(task) })
        }
    }

    // Spin & Win Dialog
    if (showSpinDialog) {
        AlertDialog(
            onDismissRequest = { showSpinDialog = false },
            title = { Text("🎡 Spin & Win Wheel") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (spinResult == null) {
                        Text("Tap spin to test your luck and win instant coins!")
                    } else {
                        Text(
                            "🎉 You Won +$spinResult Coins!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (spinResult == null) {
                            val rewards = listOf(50, 100, 200, 250, 500)
                            val prize = rewards[Random.nextInt(rewards.size)]
                            spinResult = prize
                            viewModel.spinWheelReward(prize)
                        } else {
                            showSpinDialog = false
                        }
                    }
                ) {
                    Text(if (spinResult == null) "Spin Now" else "Claim & Close")
                }
            },
            dismissButton = {
                if (spinResult != null) {
                    TextButton(onClick = { showSpinDialog = false }) { Text("Close") }
                }
            }
        )
    }

    // Scratch Card Dialog
    if (showScratchDialog) {
        AlertDialog(
            onDismissRequest = { showScratchDialog = false },
            title = { Text("🎁 Scratch & Win Card") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (scratchResult == null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clickable {
                                    val rewards = listOf(80, 120, 150, 300)
                                    val prize = rewards[Random.nextInt(rewards.size)]
                                    scratchResult = prize
                                    viewModel.scratchCardReward(prize)
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "Tap to Scratch!",
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            "🎊 Scratch Successful! +$scratchResult Coins Added!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                if (scratchResult != null) {
                    Button(onClick = { showScratchDialog = false }) { Text("Awesome") }
                }
            }
        )
    }

    // Daily Check-in Success Dialog
    if (showCheckinSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showCheckinSuccessDialog = false },
            title = { Text("📆 Daily Check-in Success") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎉 Congratulations!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You claimed your daily check-in bonus!",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "+$checkinWonCoins Coins",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Check in again tomorrow to keep earning!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showCheckinSuccessDialog = false }) {
                    Text("Awesome!")
                }
            }
        )
    }

    // 1. APP UPDATE ALERT DIALOG
    appConfigState?.let { config ->
        if (config.appVersionCode > 1) {
            var showUpdateDialog by remember(config.appVersionCode) { mutableStateOf(true) }

            if (showUpdateDialog) {
                AlertDialog(
                    onDismissRequest = {
                        if (!config.isUpdateMandatory) {
                            showUpdateDialog = false
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Update Available! (v${config.appVersionName})", fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column {
                            Text(text = config.updateMessage, fontSize = 14.sp)
                            if (config.isUpdateMandatory) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "This update is mandatory to continue using CashPulse.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(config.updateUrl.ifBlank { "https://play.google.com" }))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            }
                        ) {
                            Text("Update Now")
                        }
                    },
                    dismissButton = if (!config.isUpdateMandatory) {
                        {
                            TextButton(onClick = { showUpdateDialog = false }) {
                                Text("Later")
                            }
                        }
                    } else null
                )
            }
        }

        // 2. GLOBAL POPUP BROADCAST NOTIFICATION ALERT
        if (config.showNotification) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissGlobalNotification() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFFF9800))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(config.notificationTitle, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(text = config.notificationMessage, fontSize = 14.sp)
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissGlobalNotification() }
                    ) {
                        Text("Awesome!")
                    }
                }
            )
        }
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when(task.category) {
                                "Video" -> Icons.Default.PlayArrow
                                "Quiz" -> Icons.Default.Quiz
                                "Check-in" -> Icons.Default.CardGiftcard
                                else -> Icons.Default.LocalOffer
                            },
                            contentDescription = null,
                            tint = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${task.reward} Coins",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Button(
                onClick = onComplete,
                enabled = !task.isCompleted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (task.isCompleted) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (task.isCompleted) "Done" else "Claim",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Helper to convert Int to TextUnit safely
fun Int.dpToSp() = this.sp
fun androidx.compose.ui.unit.TextUnit.Companion.dpToSp() = 14.sp
@Composable
fun SystemWindowInsetsModifier(): Modifier = Modifier.navigationBarsPadding()

private fun getStartOfToday(): Long {
    val cal = java.util.Calendar.getInstance()
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
