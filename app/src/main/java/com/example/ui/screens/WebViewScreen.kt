package com.example.ui.screens

import android.annotation.SuppressLint
import android.net.http.SslError
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.viewmodel.EarningViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    blogTitle: String,
    blogUrl: String,
    rewardCoins: Int,
    viewModel: EarningViewModel,
    onBack: () -> Unit
) {
    var hasClaimed by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var activeAdUrl by remember { mutableStateOf<String?>(null) }

    // 7-second Verification States for 6 Questions
    var isVerifying by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(7) }
    var currentVerifyingStep by remember { mutableStateOf(-1) }
    val completedStepsSet = remember { mutableStateListOf<Int>() }
    var completedStepsCount by remember { mutableStateOf(0) }

    var showSuccessToast by remember { mutableStateOf(false) }
    var successToastMessage by remember { mutableStateOf("") }

    var countdownJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    fun startVerification(stepIndex: Int) {
        if (completedStepsSet.contains(stepIndex)) return // Already verified!

        // Cancel any existing countdown job
        countdownJob?.cancel()

        isVerifying = true
        countdown = 7
        currentVerifyingStep = stepIndex

        countdownJob = coroutineScope.launch {
            try {
                while (countdown > 0) {
                    delay(1000)
                    countdown--
                }
                // Successful verification!
                completedStepsSet.add(stepIndex)
                completedStepsCount = completedStepsSet.size
                isVerifying = false
                successToastMessage = "✅ Step $stepIndex Verification Done!"
                showSuccessToast = true
                delay(2500)
                showSuccessToast = false
            } catch (e: Exception) {
                // Cancelled
            }
        }
    }

    // Intercept system Back button to navigate pichhe in WebView history instead of exiting
    androidx.activity.compose.BackHandler {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = blogTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(text = "Reading & Earning • Reward: +$rewardCoins Coins", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (webViewRef?.canGoBack() == true) {
                            webViewRef?.goBack()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val isClaimable = completedStepsCount >= 6
                    Button(
                        onClick = {
                            if (!hasClaimed && isClaimable) {
                                hasClaimed = true
                                val activeUrl = webViewRef?.url ?: blogUrl
                                
                                fun normalizeUrlStr(u: String): String {
                                    return u.trim().lowercase().substringBefore("?").removeSuffix("/")
                                }
                                
                                val normalizedActive = normalizeUrlStr(activeUrl)
                                val matchedArticle = viewModel.articles.value.find { 
                                    normalizeUrlStr(it.url) == normalizedActive 
                                }
                                
                                val finalUrl = matchedArticle?.url ?: activeUrl
                                val finalTitle = matchedArticle?.title ?: blogTitle
                                val finalCoins = matchedArticle?.rewardCoins ?: rewardCoins
                                
                                viewModel.rewardForReadingBlog(finalTitle, finalUrl, finalCoins)
                            }
                        },
                        enabled = !hasClaimed && isClaimable,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasClaimed) {
                                Color(0xFF4CAF50)
                            } else if (isClaimable) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Gray
                            },
                            disabledContainerColor = if (hasClaimed) Color(0xFF4CAF50) else Color.Gray
                        ),
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (hasClaimed) Icons.Default.CheckCircle else Icons.Default.MonetizationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasClaimed) "Claimed" else "Claim +$rewardCoins",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // In-app WebView without top URL bar, keeping all internal links inside the app
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewRef = this
                        
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        
                        // Crucial configurations for Monetag, Adsterra and other Ad networks
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.loadsImagesAutomatically = true
                        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            settings.safeBrowsingEnabled = false
                        }
                        settings.mediaPlaybackRequiresUserGesture = false
                        
                        // Force Desktop Site mode
                        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.setSupportZoom(true)
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        
                        // Ensure popups and blank target links load in the same webview
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.setSupportMultipleWindows(true)
                        
                        // Accept third party cookies (critical for ad tracking redirects)
                        android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        // Bind Android Bridge JavaScript interface for real-time quiz options monitoring
                        addJavascriptInterface(WebAppInterface { optionIndex, stepIndex ->
                            post {
                                if (stepIndex in 1..6) {
                                    startVerification(stepIndex)
                                }
                            }
                        }, "AndroidBridge")

                        webChromeClient = object : android.webkit.WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                if (newProgress > 5) {
                                    // Inject as early as possible to guarantee absolute Desktop Mode from millisecond 0!
                                    view?.evaluateJavascript(
                                        """
                                        (function() {
                                            var lockViewport = function() {
                                                var meta = document.querySelector('meta[name="viewport"]');
                                                if (!meta) {
                                                    meta = document.createElement('meta');
                                                    meta.name = 'viewport';
                                                    document.getElementsByTagName('head')[0].appendChild(meta);
                                                }
                                                // Calculate perfect scale factor dynamically using physical screen width (always reliable)
                                                var scale = window.screen.width / 1200;
                                                meta.content = 'width=1200, initial-scale=' + scale + ', minimum-scale=' + scale + ', maximum-scale=3.0, user-scalable=yes';
                                                
                                                // Lock html/body elements to desktop width and hide horizontal scrollbars
                                                var style = document.getElementById('desktop-style-lock');
                                                if (!style) {
                                                    style = document.createElement('style');
                                                    style.id = 'desktop-style-lock';
                                                    style.innerHTML = 'html, body { overflow-x: hidden !important; width: 1200px !important; min-width: 1200px !important; max-width: 1200px !important; }';
                                                    document.head.appendChild(style);
                                                }
                                            };
                                            
                                            // Execute immediately
                                            lockViewport();
                                            
                                            // Continuous lock every 100ms for 3 seconds to prevent any dynamic blogger scripts from resetting viewport layout
                                            var intervalId = setInterval(lockViewport, 100);
                                            setTimeout(function() { clearInterval(intervalId); }, 3000);
                                        })();
                                        """.trimIndent(),
                                        null
                                    )
                                }
                            }

                            override fun onCreateWindow(
                                view: WebView?,
                                isDialog: Boolean,
                                isUserGesture: Boolean,
                                resultMsg: android.os.Message?
                            ): Boolean {
                                val transport = resultMsg?.obj as? WebView.WebViewTransport
                                if (transport != null) {
                                    // Create a temporary WebView to catch the popup URL request
                                    val tempWebView = WebView(ctx).apply {
                                        webViewClient = object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                                val url = request?.url?.toString()
                                                if (url != null) {
                                                    // Load the ad link inside the App's Dialog overlay!
                                                    // This ensures we stay 100% inside our application natively.
                                                    try {
                                                        activeAdUrl = url
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                                return true
                                            }

                                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                                if (url != null) {
                                                    try {
                                                        activeAdUrl = url
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                                return true
                                            }
                                        }
                                    }
                                    transport.webView = tempWebView
                                    resultMsg.sendToTarget()
                                    return true
                                }
                                return false
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            private fun handleUrlOverride(view: WebView?, url: String?): Boolean {
                                if (url == null || view == null) return false
                                
                                val blogUri = android.net.Uri.parse(blogUrl)
                                val clickedUri = android.net.Uri.parse(url)
                                
                                // If host is different from the original article blog host, it's an ad or external site!
                                if (clickedUri.host != null && clickedUri.host != blogUri.host) {
                                    // Load ad/external click inside the App's Dialog WebView instead of Chrome.
                                    // This prevents the main WebView from navigating away or reloading!
                                    try {
                                        activeAdUrl = url
                                        
                                        // Dual-Redundancy check: if the JS bridge is still loading or has any delay, 
                                        // auto start verification countdown for the next uncompleted step on URL click!
                                        val nextStep = (1..6).firstOrNull { !completedStepsSet.contains(it) }
                                        if (nextStep != null && !isVerifying) {
                                            post {
                                                startVerification(nextStep)
                                            }
                                        }
                                        return true // Intercepted successfully!
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                
                                if (url.startsWith("http://") || url.startsWith("https://")) {
                                    // Return false to let standard blog internal navigation load natively
                                    return false
                                } else {
                                    // Handle custom schemes (intent://, market://, tg://, whatsapp://, upi://, paytm://, etc.)
                                    try {
                                        val intent = android.content.Intent.parseUri(url, android.content.Intent.URI_INTENT_SCHEME)
                                        intent.addCategory(android.content.Intent.CATEGORY_BROWSABLE)
                                        intent.setComponent(null)
                                        intent.setSelector(null)
                                        ctx.startActivity(intent)
                                        return true
                                    } catch (e: Exception) {
                                        try {
                                            val intent = android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse(url)
                                            )
                                            ctx.startActivity(intent)
                                            return true
                                        } catch (ex: Exception) {
                                            ex.printStackTrace()
                                        }
                                    }
                                }
                                return false
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                return handleUrlOverride(view, url)
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return handleUrlOverride(view, request?.url?.toString())
                            }

                            @SuppressLint("WebViewClientOnReceivedSslError")
                            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                // Allow SSL errors to handle temporary ad redirect certificate issues
                                handler?.proceed()
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                // Final lock on full screen absolute Desktop mode scaling and absolute zero horizontal scroll
                                view?.evaluateJavascript(
                                    """
                                    (function() {
                                        var lockViewport = function() {
                                            var meta = document.querySelector('meta[name="viewport"]');
                                            if (!meta) {
                                                meta = document.createElement('meta');
                                                meta.name = 'viewport';
                                                document.getElementsByTagName('head')[0].appendChild(meta);
                                            }
                                            var scale = window.screen.width / 1200;
                                            meta.content = 'width=1200, initial-scale=' + scale + ', minimum-scale=' + scale + ', maximum-scale=3.0, user-scalable=yes';
                                            
                                            var style = document.getElementById('desktop-style-lock');
                                            if (!style) {
                                                style = document.createElement('style');
                                                style.id = 'desktop-style-lock';
                                                style.innerHTML = 'html, body { overflow-x: hidden !important; width: 1200px !important; min-width: 1200px !important; max-width: 1200px !important; }';
                                                document.head.appendChild(style);
                                            }
                                        };
                                        
                                        lockViewport();
                                        var intervalId = setInterval(lockViewport, 100);
                                        setTimeout(function() { clearInterval(intervalId); }, 3000);
                                    })();
                                    """.trimIndent(),
                                    null
                                )
                            }
                        }
                        loadUrl(blogUrl)
                    }
                },
                update = { webView ->
                    // Keep view updated if needed
                }
            )

            // Step Counter overlay floating badge at the top
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd),
                colors = CardDefaults.cardColors(
                    containerColor = if (completedStepsCount >= 6) Color(0xFF4CAF50) else MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Verification: $completedStepsCount / 6 Steps Done",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (completedStepsCount >= 6) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Beautiful Success Toast Banner
            if (showSuccessToast) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = successToastMessage,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }

    // Full-Screen Ad WebView Dialog Container completely INSIDE the app!
    if (activeAdUrl != null) {
        Dialog(
            onDismissRequest = {
                // If they cancel early, reset ad url and cancel countdown
                if (countdown == 0) {
                    activeAdUrl = null
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = countdown == 0,
                dismissOnClickOutside = false
            )
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Step Verification Ad", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (countdown > 0) {
                                    Text("Please view the ad to complete verification", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("Verification Complete!", fontSize = 10.sp, color = Color(0xFF4CAF50))
                                }
                            }
                        },
                        navigationIcon = {
                            if (countdown == 0) {
                                IconButton(onClick = { activeAdUrl = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Ad")
                                }
                            }
                        },
                        actions = {
                            if (countdown > 0) {
                                Text(
                                    text = "Verify in ${countdown}s",
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 16.dp),
                                    fontSize = 13.sp
                                )
                            } else {
                                Button(
                                    onClick = { activeAdUrl = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    modifier = Modifier.padding(end = 8.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Close & Claim Step", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    )
                }
            ) { innerPad ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPad)
                        .background(Color.White)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        // Let all internal ad links load natively inside this dialog overlay webview!
                                        return false
                                    }
                                    
                                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                        return false
                                    }
                                }
                                loadUrl(activeAdUrl!!)
                            }
                        }
                    )
                    
                    // Show circular timer at the bottom center of the screen
                    if (countdown > 0) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xDD000000)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    progress = { countdown / 7f },
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp,
                                    color = Color.Red,
                                    trackColor = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Verifying Step $currentVerifyingStep: stay ${countdown}s",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Android Javascript Bridge to monitor blogger quiz clicks natively
class WebAppInterface(
    private val onQuizOptionClicked: (optionIndex: Int, stepIndex: Int) -> Unit
) {
    @android.webkit.JavascriptInterface
    fun onQuizOptionClickedWithIndex(optionIndex: Int, questionIndex: Int) {
        onQuizOptionClicked(optionIndex, questionIndex)
    }

    @android.webkit.JavascriptInterface
    fun onQuizOptionClicked(optionIndex: Int) {
        onQuizOptionClicked(optionIndex, -1)
    }
}
