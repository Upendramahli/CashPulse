package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EarningViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: EarningViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var selectedTab by remember { mutableStateOf(0) }
                var currentSubScreen by remember { mutableStateOf("main") } // "main", "read_and_earn", "webview"
                var selectedArticle by remember { mutableStateOf<com.example.viewmodel.ArticleItem?>(null) }

                val user by viewModel.user.collectAsStateWithLifecycle()
                val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
                val tasks by viewModel.tasks.collectAsStateWithLifecycle()
                val referrals by viewModel.referrals.collectAsStateWithLifecycle()
                val withdrawals by viewModel.withdrawals.collectAsStateWithLifecycle()
                val transactions by viewModel.transactions.collectAsStateWithLifecycle()
                val blogs by viewModel.blogs.collectAsStateWithLifecycle(emptyList())
                val articles by viewModel.articles.collectAsStateWithLifecycle(emptyList())

                val isAdmin = user?.isAdmin == true

                val items = remember(isAdmin) {
                    if (isAdmin) {
                        listOf(
                            BottomNavTab("Home", Icons.Default.Home),
                            BottomNavTab("Refer", Icons.Default.Share),
                            BottomNavTab("Wallet", Icons.Default.AccountBalanceWallet),
                            BottomNavTab("Profile", Icons.Default.Person),
                            BottomNavTab("Admin", Icons.Default.AdminPanelSettings)
                        )
                    } else {
                        listOf(
                            BottomNavTab("Home", Icons.Default.Home),
                            BottomNavTab("Refer", Icons.Default.Share),
                            BottomNavTab("Wallet", Icons.Default.AccountBalanceWallet),
                            BottomNavTab("Profile", Icons.Default.Person)
                        )
                    }
                }

                LaunchedEffect(isAdmin) {
                    if (!isAdmin && selectedTab > 3) {
                        selectedTab = 0
                    }
                }

                if (!isUserLoggedIn) {
                    AuthScreen(viewModel = viewModel)
                } else {
                    if (currentSubScreen == "read_and_earn") {
                        ReadAndEarnScreen(
                            articles = articles,
                            viewModel = viewModel,
                            onSelectArticle = { article ->
                                selectedArticle = article
                                currentSubScreen = "webview"
                            }
                        )
                        androidx.activity.compose.BackHandler {
                            currentSubScreen = "main"
                        }
                    } else if (currentSubScreen == "webview" && selectedArticle != null) {
                        WebViewScreen(
                            blogTitle = selectedArticle!!.title,
                            blogUrl = selectedArticle!!.url,
                            rewardCoins = selectedArticle!!.rewardCoins,
                            viewModel = viewModel,
                            onBack = { currentSubScreen = "read_and_earn" }
                        )
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                NavigationBar {
                                    items.forEachIndexed { index, tab ->
                                        NavigationBarItem(
                                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                                            label = { Text(tab.title) },
                                            selected = selectedTab == index,
                                            onClick = { selectedTab = index }
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                when (selectedTab) {
                                    0 -> HomeScreen(
                                        user = user,
                                        tasks = tasks,
                                        viewModel = viewModel,
                                        onNavigateToWallet = { selectedTab = 2 },
                                        onNavigateToReadAndEarn = { currentSubScreen = "read_and_earn" }
                                    )
                                    1 -> ReferScreen(
                                        user = user,
                                        referrals = referrals,
                                        viewModel = viewModel
                                    )
                                    2 -> WalletScreen(
                                        user = user,
                                        withdrawals = withdrawals,
                                        transactions = transactions,
                                        viewModel = viewModel
                                    )
                                    3 -> ProfileScreen(
                                        user = user,
                                        viewModel = viewModel
                                    )
                                    4 -> {
                                        if (isAdmin) {
                                            AdminScreen(
                                                user = user,
                                                withdrawals = withdrawals,
                                                viewModel = viewModel
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavTab(val title: String, val icon: ImageVector)
