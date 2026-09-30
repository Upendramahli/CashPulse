package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserEntity
import com.example.data.WithdrawalEntity
import com.example.data.TransactionEntity
import com.example.viewmodel.EarningViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    user: UserEntity?,
    withdrawals: List<WithdrawalEntity>,
    transactions: List<TransactionEntity>,
    viewModel: EarningViewModel
) {
    var selectedMethod by remember { mutableStateOf("UPI") }
    var selectedCoins by remember { mutableStateOf("10000") }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Detailed field state variables
    var upiId by remember { mutableStateOf("") }
    var bankHolderName by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var bankAccountNo by remember { mutableStateOf("") }
    var bankIfscCode by remember { mutableStateOf("") }
    var redeemContactInfo by remember { mutableStateOf("") }

    val referrals by viewModel.referrals.collectAsStateWithLifecycle(emptyList())

    val currentCoins = user?.coins ?: 0
    val rupeesValue = currentCoins / 100.0

    LazyColumn(
        modifier = SystemWindowInsetsModifier().fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Available Wallet Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$currentCoins Coins",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Real Cash Value: ₹%.2f (Rate: 100 Coins = ₹1)".format(rupeesValue),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Requirements Card
        item {
            val totalReferralsCount = referrals.size
            val qualifyingReferralsCount = referrals.count { it.friendEarning >= 10000 }
            val isRequirementsMet = totalReferralsCount >= 5 && qualifyingReferralsCount >= 5

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRequirementsMet) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) 
                    else 
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isRequirementsMet) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isRequirementsMet) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isRequirementsMet) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isRequirementsMet) "Withdrawal Requirements Unlocked!" else "Withdrawal Requirements Locked",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (isRequirementsMet) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    }
                    
                    Text(
                        text = "To withdraw, you must refer at least 5 friends, and each of those 5 friends must earn at least ₹100 (10,000 Coins) in their lifetime.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Rule 1 Checklist Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (totalReferralsCount >= 5) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (totalReferralsCount >= 5) Color(0xFF4CAF50) else Color(0xFFE57373),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Refer minimum 5 friends",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "$totalReferralsCount / 5",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (totalReferralsCount >= 5) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    }

                    // Rule 2 Checklist Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (qualifyingReferralsCount >= 5) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (qualifyingReferralsCount >= 5) Color(0xFF4CAF50) else Color(0xFFE57373),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "5 referred friends earning >= ₹100",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "$qualifyingReferralsCount / 5",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (qualifyingReferralsCount >= 5) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    }

                    if (!isRequirementsMet && referrals.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Friend Status Progress:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        referrals.forEach { friend ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (friend.friendEarning >= 10000) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                                        contentDescription = null,
                                        tint = if (friend.friendEarning >= 10000) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = friend.friendName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${friend.friendEarning} / 10000 Coins (${if (friend.friendEarning >= 10000) "Ready" else "Needs ${10000 - friend.friendEarning}"})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (friend.friendEarning >= 10000) Color(0xFF4CAF50) else Color(0xFFFFD54F)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Withdrawal Form Header
        item {
            Text(
                text = "💸 Request Withdrawal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Select Payout Method", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("UPI", "Bank", "Redeem Code").forEach { method ->
                            FilterChip(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                label = { Text(method, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when(method) {
                                            "UPI" -> Icons.Default.Smartphone
                                            "Bank" -> Icons.Default.AccountBalance
                                            else -> Icons.Default.CardGiftcard
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    when (selectedMethod) {
                        "UPI" -> {
                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it },
                                label = { Text("Enter UPI ID (e.g. user@ybl)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        "Bank" -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = bankHolderName,
                                    onValueChange = { bankHolderName = it },
                                    label = { Text("Account Holder Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = bankName,
                                    onValueChange = { bankName = it },
                                    label = { Text("Bank Name (e.g. State Bank of India)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = bankAccountNo,
                                    onValueChange = { bankAccountNo = it },
                                    label = { Text("Bank Account Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = bankIfscCode,
                                    onValueChange = { bankIfscCode = it },
                                    label = { Text("IFSC Code") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                        else -> {
                            OutlinedTextField(
                                value = redeemContactInfo,
                                onValueChange = { redeemContactInfo = it },
                                label = { Text("Enter Email or WhatsApp Number") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Text(text = "Select Amount (Min. 10000 Coins = ₹100)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("10000", "20000", "50000").forEach { amount ->
                            FilterChip(
                                selected = selectedCoins == amount,
                                onClick = { selectedCoins = amount },
                                label = { Text("$amount Coins") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    val totalReferralsCount = referrals.size
                    val qualifyingReferralsCount = referrals.count { it.friendEarning >= 10000 }
                    val isReferralRequirementsMet = totalReferralsCount >= 5 && qualifyingReferralsCount >= 5

                    val todayStartTimestamp: Long = run {
                        val cal = java.util.Calendar.getInstance()
                        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        cal.set(java.util.Calendar.MINUTE, 0)
                        cal.set(java.util.Calendar.SECOND, 0)
                        cal.set(java.util.Calendar.MILLISECOND, 0)
                        cal.timeInMillis
                    }
                    val hasWithdrawnToday = withdrawals.any { it.timestamp >= todayStartTimestamp }
                    val isRequirementsMet = isReferralRequirementsMet && !hasWithdrawnToday

                    if (hasWithdrawnToday) {
                        Text(
                            text = "❌ Daily Limit Reached: You can only withdraw once per day. Please try again tomorrow!",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (errorMessage.isNotEmpty()) {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }

                    if (showSuccessMessage) {
                        Text(text = "✓ Withdrawal requested successfully! Status: Pending", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val coins = selectedCoins.toIntOrNull() ?: 0
                            val finalDetails = when(selectedMethod) {
                                "UPI" -> upiId.trim()
                                "Bank" -> {
                                    if (bankHolderName.isNotBlank() && bankName.isNotBlank() && bankAccountNo.isNotBlank() && bankIfscCode.isNotBlank()) {
                                        "Name: $bankHolderName, Bank: $bankName, A/C: $bankAccountNo, IFSC: $bankIfscCode"
                                    } else {
                                        ""
                                    }
                                }
                                else -> redeemContactInfo.trim()
                            }

                            if (hasWithdrawnToday) {
                                errorMessage = "❌ Daily Limit Reached: You have already made a withdrawal today."
                            } else if (!isReferralRequirementsMet) {
                                errorMessage = "🔒 Cannot withdraw! Please fulfill all referral requirements first."
                            } else if (finalDetails.isBlank()) {
                                errorMessage = "Please fill in all the required payout information."
                            } else if (currentCoins < coins) {
                                errorMessage = "Insufficient coin balance!"
                            } else if (coins < 10000) {
                                errorMessage = "Minimum withdrawal amount is 10000 Coins (₹100)."
                            } else {
                                errorMessage = ""
                                viewModel.requestWithdrawal(selectedMethod, finalDetails, coins)
                                showSuccessMessage = true
                                upiId = ""
                                bankHolderName = ""
                                bankName = ""
                                bankAccountNo = ""
                                bankIfscCode = ""
                                redeemContactInfo = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isRequirementsMet,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRequirementsMet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            contentColor = if (isRequirementsMet) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = when {
                                hasWithdrawnToday -> "🔒 Limit Reached: 1 Payout/Day"
                                !isReferralRequirementsMet -> "🔒 Locked (Check Requirements Above)"
                                else -> "Withdraw ₹${(selectedCoins.toIntOrNull() ?: 0) / 100.0} Now"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Transactions & Payouts Header
        item {
            Text(
                text = "📜 Withdrawal & Activity History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Transactions List
        items(transactions) { txn ->
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when(txn.type) {
                                "EARN" -> MaterialItemColorEarn()
                                "WITHDRAWAL" -> MaterialItemColorWithdraw()
                                else -> MaterialItemColorBonus()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when(txn.type) {
                                        "EARN" -> Icons.Default.ArrowDownward
                                        "WITHDRAWAL" -> Icons.Default.ArrowUpward
                                        else -> Icons.Default.CardGiftcard
                                    },
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column {
                            Text(text = txn.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(txn.timestamp)), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }

                    Text(
                        text = "${if (txn.coins > 0) "+" else ""}${txn.coins} Coins",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (txn.coins > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun MaterialItemColorEarn() = Color(0xFF4CAF50)
@Composable
fun MaterialItemColorWithdraw() = Color(0xFFE53935)
@Composable
fun MaterialItemColorBonus() = Color(0xFFFF9800)
