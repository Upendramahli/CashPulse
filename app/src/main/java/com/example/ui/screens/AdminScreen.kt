package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserEntity
import com.example.data.WithdrawalEntity
import com.example.data.BlogWebsiteEntity
import com.example.data.AppConfigEntity
import com.example.data.NotificationEntity
import com.example.viewmodel.EarningViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    user: UserEntity?,
    withdrawals: List<WithdrawalEntity>,
    viewModel: EarningViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("📊 Wallets", "📚 Blogs", "🎯 Tasks", "📥 Payouts", "📢 Broadcast", "💬 Chats")

    var adminMessage by remember { mutableStateOf("") }
    
    // Add Blog Dialog states
    var showAddBlogDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("") }
    var newReward by remember { mutableStateOf("100") }

    // Edit Blog Dialog states
    var showEditBlogDialog by remember { mutableStateOf(false) }
    var selectedBlogToEdit by remember { mutableStateOf<BlogWebsiteEntity?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editUrl by remember { mutableStateOf("") }
    var editReward by remember { mutableStateOf("100") }

    // Add Task Dialog states
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var taskReward by remember { mutableStateOf("150") }
    var taskCategory by remember { mutableStateOf("Offer") }
    var taskActionUrl by remember { mutableStateOf("") }

    val blogs by viewModel.blogs.collectAsStateWithLifecycle(emptyList())
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(emptyList())
    val usersCount by viewModel.usersCount.collectAsStateWithLifecycle(1)

    Column(
        modifier = SystemWindowInsetsModifier()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Red Admin Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Admin Control Panel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Text(
                    text = "Configure reward systems, authorize withdrawals, and manage platform databases",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }

        // 📈 APP CORE ANALYSIS PANEL
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📈 Live App Analytics & Overall Financials",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "REAL-TIME",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Grid 1: Users & Active Activity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Total Users", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("$usersCount Accounts", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Active Users", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("1 Active (You)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Grid 2: Coins Earned & Total Paid out
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                val coinsSum = user?.totalEarned ?: 0
                                Text("Total Earned", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("$coinsSum Coins", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                val totalPaidSum = withdrawals.filter { it.status == "Success" }.sumOf { it.amountRupees }
                                Text("Total Paid", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("₹${"%.2f".format(totalPaidSum)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Grid 3: Pending Queue Stats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Pending, contentDescription = null, tint = Color(0xFFEF6C00), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Pending Approvals", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                val pendingAmt = withdrawals.filter { it.status == "Pending" }.sumOf { it.amountRupees }
                                Text("₹${"%.2f".format(pendingAmt)} Pending", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF6C00))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${withdrawals.count { it.status == "Pending" }} Payout Requests",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Tabs for Admin Sections
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { 
                        selectedTab = index
                        adminMessage = "" 
                    },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Content Switcher
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> {
                    // WALLET & BALANCE CONTROL TAB
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "💰 User Wallet & Balance Editor",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Display currently active user under management
                                    user?.let { u ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = u.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text(text = u.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                                    Text(text = "VIP ID: ${u.vipMemberId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(text = "${u.coins} Coins", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                                    Text(text = "₹${"%.2f".format(u.coins / 100.0)}", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    var coinInput by remember { mutableStateOf("") }

                                    OutlinedTextField(
                                        value = coinInput,
                                        onValueChange = { coinInput = it },
                                        label = { Text("Enter Amount (Coins)") },
                                        placeholder = { Text("e.g. 5000") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val amt = coinInput.toIntOrNull()
                                                if (amt != null) {
                                                    viewModel.adminSetCoins(amt)
                                                    adminMessage = "✓ User main balance set to $amt Coins!"
                                                    coinInput = ""
                                                } else {
                                                    adminMessage = "⚠ Please enter a valid number of coins!"
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Set Balance", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                val amt = coinInput.toIntOrNull()
                                                if (amt != null) {
                                                    viewModel.adminAddCoins(amt)
                                                    adminMessage = "✓ Successfully added +$amt Coins to user balance!"
                                                    coinInput = ""
                                                } else {
                                                    adminMessage = "⚠ Please enter a valid number of coins!"
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.AddCircle, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Add Coins", fontSize = 12.sp)
                                        }
                                    }

                                    if (adminMessage.isNotEmpty()) {
                                        Text(
                                            text = adminMessage,
                                            color = if (adminMessage.startsWith("✓")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("📝 Admin Quick Simulation Info", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = "Modify the active logged-in user balance directly! You can add/subtract coins instantly to test daily tasks, minimum cashout limits, and payout systems.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // READ & EARN BLOG MANAGEMENT TAB
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📚 Active Blog Sites (${blogs.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = { showAddBlogDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Blog", fontSize = 11.sp)
                                }
                            }
                        }

                        if (blogs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No Read & Earn blogs added. Click Add Blog above!", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                        }

                        items(blogs) { blog ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = blog.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = blog.url, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Reward: ${blog.rewardCoins} Coins", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                selectedBlogToEdit = blog
                                                editTitle = blog.title
                                                editUrl = blog.url
                                                editReward = blog.rewardCoins.toString()
                                                showEditBlogDialog = true
                                            }
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                        }

                                        IconButton(
                                            onClick = { viewModel.adminDeleteBlog(blog.id) }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // CUSTOM HIGH REWARD TASKS MANAGEMENT TAB
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎯 Active Custom Tasks (${tasks.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = { showAddTaskDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Task", fontSize = 11.sp)
                                }
                            }
                        }

                        if (tasks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No custom tasks added yet. Click Add Task!", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                        }

                        items(tasks) { task ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                modifier = Modifier.padding(end = 6.dp)
                                            ) {
                                                Text(
                                                    text = task.category,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(text = task.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = task.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        Text(text = "Reward: +${task.reward} Coins", fontSize = 11.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = { viewModel.adminDeleteTask(task.id) }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // WITHDRAWAL PAYOUT REQUESTS TAB
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "📥 Payout Requests Queue",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (withdrawals.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No payout withdrawal requests yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                        }

                        items(withdrawals) { withdrawal ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${withdrawal.method}: ${withdrawal.accountDetail}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when(withdrawal.status) {
                                                "Success" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                "Rejected" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                                else -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                            }
                                        ) {
                                            Text(
                                                text = withdrawal.status,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when(withdrawal.status) {
                                                    "Success" -> Color(0xFF2E7D32)
                                                    "Rejected" -> MaterialTheme.colorScheme.error
                                                    else -> Color(0xFFEF6C00)
                                                }
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Payout Amount: ${withdrawal.amountCoins} Coins (₹${withdrawal.amountRupees})",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )

                                    if (withdrawal.status == "Pending") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.adminUpdateWithdrawalStatus(withdrawal.id, "Success") },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Text("Approve", fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = { viewModel.adminUpdateWithdrawalStatus(withdrawal.id, "Rejected") },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(vertical = 6.dp)
                                            ) {
                                                Text("Reject", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // BROADCAST, APP UPDATES & NOTIFICATIONS TAB
                    val appConfigState by viewModel.appConfig.collectAsStateWithLifecycle(null)
                    val notificationsList by viewModel.notifications.collectAsStateWithLifecycle(emptyList())

                    var upVersionCode by remember { mutableStateOf("2") }
                    var upVersionName by remember { mutableStateOf("1.1.0") }
                    var upMessage by remember { mutableStateOf("We have added exciting new reading blogs and tasks! Update now to start earning.") }
                    var upUrl by remember { mutableStateOf("https://cashpulse.com/download") }
                    var isUpMandatory by remember { mutableStateOf(false) }

                    var notiTitle by remember { mutableStateOf("") }
                    var notiMessage by remember { mutableStateOf("") }
                    var showNotiSuccess by remember { mutableStateOf("") }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section 1: Push / Publish App Update
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("📱 Push New App Update", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }

                                    appConfigState?.let { config ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("Current Live Version in App:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                Text("Version Code: ${config.appVersionCode} | Version Name: ${config.appVersionName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                if (config.updateUrl.isNotEmpty()) {
                                                    Text("Download URL: ${config.updateUrl}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = upVersionCode,
                                            onValueChange = { upVersionCode = it },
                                            label = { Text("Ver Code") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )
                                        OutlinedTextField(
                                            value = upVersionName,
                                            onValueChange = { upVersionName = it },
                                            label = { Text("Ver Name") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = upMessage,
                                        onValueChange = { upMessage = it },
                                        label = { Text("Update Alert Message") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 3
                                    )

                                    OutlinedTextField(
                                        value = upUrl,
                                        onValueChange = { upUrl = it },
                                        label = { Text("APK Download / Update URL") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isUpMandatory,
                                            onCheckedChange = { isUpMandatory = it }
                                        )
                                        Text("Force Update (Mandatory for all users)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }

                                    Button(
                                        onClick = {
                                            val vc = upVersionCode.toIntOrNull() ?: 2
                                            viewModel.adminSetAppUpdate(vc, upVersionName, upMessage, upUrl, isUpMandatory)
                                            showNotiSuccess = "✓ App Update configuration successfully updated live!"
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Publish Update Configuration")
                                    }
                                }
                            }
                        }

                        // Section 2: Send Real-Time Notifications
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFFF9800))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("🔔 Send Broad-Scale Notification", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }

                                    OutlinedTextField(
                                        value = notiTitle,
                                        onValueChange = { notiTitle = it },
                                        label = { Text("Notification Title") },
                                        placeholder = { Text("e.g. ₹50 Instant Referral Bonus!") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = notiMessage,
                                        onValueChange = { notiMessage = it },
                                        label = { Text("Notification Body Message") },
                                        placeholder = { Text("e.g. Share your referral code with friends & claim ₹50 instantly!") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 3
                                    )

                                    Button(
                                        onClick = {
                                            if (notiTitle.isNotBlank() && notiMessage.isNotBlank()) {
                                                viewModel.adminSendNotification(notiTitle, notiMessage)
                                                showNotiSuccess = "✓ Notification broadcasted successfully!"
                                                notiTitle = ""
                                                notiMessage = ""
                                            } else {
                                                showNotiSuccess = "⚠ Please fill out all fields first!"
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Send Push Broadcast")
                                    }

                                    if (showNotiSuccess.isNotEmpty()) {
                                        Text(
                                            text = showNotiSuccess,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (showNotiSuccess.startsWith("✓")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        // Section 3: History of Sent Notifications
                        item {
                            Text(
                                text = "📋 Sent Notifications History (${notificationsList.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (notificationsList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No broadcast notifications sent yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                        }

                        items(notificationsList) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = item.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteNotification(item.id) }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
                5 -> {
                    // LIVE CHAT SUPPORT ADMINISTRATION PANEL
                    val activeUsersList by viewModel.activeChatUsers.collectAsStateWithLifecycle(emptyList())
                    var selectedUserForChat by remember { mutableStateOf<String?>(null) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SupportAgent, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "💬 Customer Chat Support Desk",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (activeUsersList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No support tickets or active chats initiated yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                        }

                        items(activeUsersList) { userEmail ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = userEmail, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(text = "Click to chat / reply", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = { selectedUserForChat = userEmail },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Reply", fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { viewModel.clearUserChatThread(userEmail) }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Ticket", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- ADMIN REPLY CHAT THREAD OVERLAY DIALOG ---
                    selectedUserForChat?.let { targetUserEmail ->
                        val messagesList by viewModel.getChatMessagesForUser(targetUserEmail).collectAsStateWithLifecycle(emptyList())
                        var replyText by remember { mutableStateOf("") }

                        // Mark chat as read by admin when opened
                        LaunchedEffect(messagesList.size) {
                            viewModel.markChatAsReadByAdmin(targetUserEmail)
                        }

                        AlertDialog(
                            onDismissRequest = { selectedUserForChat = null },
                            title = {
                                Column {
                                    Text("Support Thread: $targetUserEmail", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Real-Time Support Channel", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth().height(320.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(messagesList) { msg ->
                                            val isAdminMessage = msg.senderEmail == "admin"
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = if (isAdminMessage) Arrangement.End else Arrangement.Start
                                            ) {
                                                Card(
                                                    shape = RoundedCornerShape(
                                                        topStart = 12.dp,
                                                        topEnd = 12.dp,
                                                        bottomStart = if (isAdminMessage) 12.dp else 0.dp,
                                                        bottomEnd = if (isAdminMessage) 0.dp else 12.dp
                                                    ),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isAdminMessage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                                    ),
                                                    modifier = Modifier.widthIn(max = 200.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        Text(
                                                            text = msg.messageText,
                                                            fontSize = 11.sp,
                                                            color = if (isAdminMessage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = replyText,
                                            onValueChange = { replyText = it },
                                            placeholder = { Text("Type reply...", fontSize = 12.sp) },
                                            modifier = Modifier.weight(1f),
                                            maxLines = 2,
                                            shape = RoundedCornerShape(12.dp),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                                        )
                                        IconButton(
                                            onClick = {
                                                if (replyText.isNotBlank()) {
                                                    viewModel.sendAdminMessage(targetUserEmail, replyText.trim())
                                                    replyText = ""
                                                }
                                            },
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                                .size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "Send",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {},
                            dismissButton = {
                                TextButton(onClick = { selectedUserForChat = null }) {
                                    Text("Back to Tickets")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Blog Dialog
    if (showAddBlogDialog) {
        AlertDialog(
            onDismissRequest = { showAddBlogDialog = false },
            title = { Text("Add Read & Earn Website") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Blog Title (e.g. Daily Tech Hub)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("Website URL (https://...)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newReward,
                        onValueChange = { newReward = it },
                        label = { Text("Reward Coins") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val reward = newReward.toIntOrNull() ?: 100
                    if (newTitle.isNotBlank() && newUrl.isNotBlank()) {
                        viewModel.adminAddBlog(newTitle, newUrl, reward)
                        newTitle = ""
                        newUrl = ""
                        showAddBlogDialog = false
                    }
                }) {
                    Text("Add Website")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBlogDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Blog Dialog
    if (showEditBlogDialog && selectedBlogToEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditBlogDialog = false },
            title = { Text("Edit Read & Earn Website") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Blog Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editUrl,
                        onValueChange = { editUrl = it },
                        label = { Text("Website URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editReward,
                        onValueChange = { editReward = it },
                        label = { Text("Reward Coins") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val reward = editReward.toIntOrNull() ?: 100
                    if (editTitle.isNotBlank() && editUrl.isNotBlank()) {
                        viewModel.adminDeleteBlog(selectedBlogToEdit!!.id)
                        viewModel.adminAddBlog(editTitle, editUrl, reward)
                        showEditBlogDialog = false
                    }
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBlogDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Custom High Reward Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title (e.g. Join Telegram Channel)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        label = { Text("Task Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskReward,
                        onValueChange = { taskReward = it },
                        label = { Text("Reward Coins") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskCategory,
                        onValueChange = { taskCategory = it },
                        label = { Text("Category (Offer / Video / Quiz)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskActionUrl,
                        onValueChange = { taskActionUrl = it },
                        label = { Text("Action URL (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val reward = taskReward.toIntOrNull() ?: 150
                    if (taskTitle.isNotBlank() && taskDescription.isNotBlank()) {
                        viewModel.adminAddTask(taskTitle, taskDescription, reward, taskCategory, taskActionUrl)
                        taskTitle = ""
                        taskDescription = ""
                        taskReward = "150"
                        taskCategory = "Offer"
                        taskActionUrl = ""
                        showAddTaskDialog = false
                    }
                }) {
                    Text("Add Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
            }
        )
    }
}
